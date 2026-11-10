package com.resq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resq.algorithm.PathResult;
import com.resq.dto.DispatchResponse;
import com.resq.exception.AllocationException;
import com.resq.exception.ResourceNotFoundException;
import com.resq.model.*;
import com.resq.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final DispatchRepository dispatchRepository;
    private final EmergencyRepository emergencyRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RouteService routeService;
    private final AmbulanceService ambulanceService;
    private final HospitalService hospitalService;
    private final ObjectMapper objectMapper;

    public DispatchService(DispatchRepository dispatchRepository,
                           EmergencyRepository emergencyRepository,
                           AmbulanceRepository ambulanceRepository,
                           HospitalRepository hospitalRepository,
                           StatusHistoryRepository statusHistoryRepository,
                           RouteService routeService,
                           AmbulanceService ambulanceService,
                           HospitalService hospitalService,
                           ObjectMapper objectMapper) {
        this.dispatchRepository = dispatchRepository;
        this.emergencyRepository = emergencyRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.routeService = routeService;
        this.ambulanceService = ambulanceService;
        this.hospitalService = hospitalService;
        this.objectMapper = objectMapper;
    }

    /**
     * Transactional Ambulance and Hospital Allocation Algorithm
     */
    @Transactional
    public DispatchResponse allocateEmergency(Long emergencyId) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with ID: " + emergencyId));

        if (!"PENDING".equalsIgnoreCase(emergency.getStatus())) {
            throw new AllocationException("Emergency " + emergency.getReferenceCode() +
                    " is not in PENDING state (Current state: " + emergency.getStatus() + ")");
        }

        // 1. Fetch available ambulances and find nearest qualified candidate
        List<Ambulance> availableAmbulances = ambulanceRepository.findByAvailableTrue();
        if (availableAmbulances.isEmpty()) {
            throw new AllocationException("CRITICAL ALERT: No ambulances are currently available in the fleet!");
        }

        Ambulance bestAmbulance = null;
        PathResult bestPathToEmg = null;
        double minAmbulanceDistance = Double.POSITIVE_INFINITY;

        for (Ambulance amb : availableAmbulances) {
            if (ambulanceService.isAmbulanceSuitable(amb, emergency)) {
                PathResult pathResult = routeService.calculateShortestRoute(amb.getCurrentNodeId(), emergency.getLocationNodeId());
                if (pathResult.isReachable()) {
                    if (pathResult.getTotalDistanceKm() < minAmbulanceDistance) {
                        minAmbulanceDistance = pathResult.getTotalDistanceKm();
                        bestAmbulance = amb;
                        bestPathToEmg = pathResult;
                    } else if (pathResult.getTotalDistanceKm() == minAmbulanceDistance && bestAmbulance != null) {
                        // Deterministic tie-breaker: vehicle code alphabetical
                        if (amb.getVehicleCode().compareTo(bestAmbulance.getVehicleCode()) < 0) {
                            bestAmbulance = amb;
                            bestPathToEmg = pathResult;
                        }
                    }
                }
            }
        }

        if (bestAmbulance == null) {
            throw new AllocationException("CRITICAL ALERT: No suitable available ambulance with required equipment can reach location "
                    + emergency.getLocationNodeId());
        }

        // 2. Identify required medical facility and select nearest eligible hospital with beds
        String requiredFacility = hospitalService.getRequiredFacility(emergency);
        List<Hospital> hospitals = hospitalRepository.findAll();

        Hospital bestHospital = null;
        PathResult bestPathToHosp = null;
        double minHospitalDistance = Double.POSITIVE_INFINITY;

        // Pass 1: Strict facility match + available beds > 0
        for (Hospital hosp : hospitals) {
            if (hosp.isEligibleFor(requiredFacility)) {
                PathResult pathResult = routeService.calculateShortestRoute(emergency.getLocationNodeId(), hosp.getLocationNodeId());
                if (pathResult.isReachable() && pathResult.getTotalDistanceKm() < minHospitalDistance) {
                    minHospitalDistance = pathResult.getTotalDistanceKm();
                    bestHospital = hosp;
                    bestPathToHosp = pathResult;
                }
            }
        }

        // Pass 2: Fallback to General Emergency if specialized department is unavailable
        if (bestHospital == null && !requiredFacility.equalsIgnoreCase("General Emergency")) {
            for (Hospital hosp : hospitals) {
                if (hosp.isEligibleFor("General Emergency")) {
                    PathResult pathResult = routeService.calculateShortestRoute(emergency.getLocationNodeId(), hosp.getLocationNodeId());
                    if (pathResult.isReachable() && pathResult.getTotalDistanceKm() < minHospitalDistance) {
                        minHospitalDistance = pathResult.getTotalDistanceKm();
                        bestHospital = hosp;
                        bestPathToHosp = pathResult;
                    }
                }
            }
        }

        if (bestHospital == null) {
            throw new AllocationException("CRITICAL ALERT: No hospital with available beds and capacity found for " + requiredFacility);
        }

        // 3. Compute telemetry metrics (average speed 40 km/h)
        double totalDist = bestPathToEmg.getTotalDistanceKm() + bestPathToHosp.getTotalDistanceKm();
        double etaMinutes = (bestPathToEmg.getTotalDistanceKm() / 40.0) * 60.0;

        // 4. Serialize routes to JSON strings
        String routeEmgJson;
        String routeHospJson;
        try {
            routeEmgJson = objectMapper.writeValueAsString(bestPathToEmg.getPathNodes());
            routeHospJson = objectMapper.writeValueAsString(bestPathToHosp.getPathNodes());
        } catch (JsonProcessingException e) {
            routeEmgJson = "[]";
            routeHospJson = "[]";
        }

        // 5. Persist Dispatch record
        Dispatch dispatch = new Dispatch(
                emergency.getId(),
                bestAmbulance.getId(),
                bestHospital.getId(),
                routeEmgJson,
                routeHospJson,
                totalDist,
                etaMinutes,
                "AMBULANCE_ASSIGNED",
                LocalDateTime.now()
        );
        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        // 6. Update Ambulance state
        bestAmbulance.setAvailable(false);
        bestAmbulance.setStatus("ASSIGNED");
        bestAmbulance.setCurrentAssignment("DISPATCH-" + savedDispatch.getId());
        ambulanceRepository.save(bestAmbulance);

        // 7. Update Emergency state
        emergency.setStatus("ASSIGNED");
        emergencyRepository.save(emergency);

        // 8. Log initial status history
        StatusHistory history = new StatusHistory(
                savedDispatch.getId(),
                "AMBULANCE_ASSIGNED",
                LocalDateTime.now(),
                "Ambulance " + bestAmbulance.getId() + " allocated. Shortest route calculated: " +
                        String.join(" -> ", bestPathToEmg.getPathNodes())
        );
        statusHistoryRepository.save(history);

        log.info("Dispatch created ID {}: Emergency {} -> Ambulance {} -> Hospital {}",
                savedDispatch.getId(), emergency.getReferenceCode(), bestAmbulance.getId(), bestHospital.getName());

        return buildDispatchResponse(savedDispatch, emergency, bestAmbulance, bestHospital);
    }

    @Transactional
    public DispatchResponse updateDispatchStatus(Long dispatchId, String newStatus, String note) {
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with ID: " + dispatchId));

        Emergency emergency = emergencyRepository.findById(dispatch.getEmergencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with ID: " + dispatch.getEmergencyId()));

        Ambulance ambulance = ambulanceRepository.findById(dispatch.getAmbulanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with ID: " + dispatch.getAmbulanceId()));

        Hospital hospital = hospitalRepository.findById(dispatch.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + dispatch.getHospitalId()));

        String normalizedStatus = newStatus.trim().toUpperCase();
        dispatch.setStatus(normalizedStatus);

        switch (normalizedStatus) {
            case "EN_ROUTE_TO_EMERGENCY" -> {
                ambulance.setStatus("EN_ROUTE");
                if (note == null) note = "Ambulance en route to emergency location " + emergency.getLocationNodeId();
            }
            case "ON_SCENE" -> {
                ambulance.setStatus("ON_SCENE");
                ambulance.setCurrentNodeId(emergency.getLocationNodeId());
                if (note == null) note = "Ambulance arrived at scene " + emergency.getLocationNodeId();
            }
            case "EN_ROUTE_TO_HOSPITAL" -> {
                ambulance.setStatus("TRANSPORTING");
                if (note == null) note = "Transporting patient to " + hospital.getName();
            }
            case "COMPLETED" -> {
                dispatch.setCompletedAt(LocalDateTime.now());
                ambulance.setAvailable(true);
                ambulance.setStatus("AVAILABLE");
                ambulance.setCurrentNodeId(hospital.getLocationNodeId());
                ambulance.setCurrentAssignment(null);
                emergency.setStatus("RESOLVED");
                if (hospital.getAvailableBeds() > 0) {
                    hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);
                    hospitalRepository.save(hospital);
                }
                if (note == null) note = "Patient delivered to hospital. Mission completed successfully.";
            }
            case "CANCELLED" -> {
                dispatch.setCompletedAt(LocalDateTime.now());
                ambulance.setAvailable(true);
                ambulance.setStatus("AVAILABLE");
                ambulance.setCurrentAssignment(null);
                emergency.setStatus("CANCELLED");
                if (note == null) note = "Dispatch was cancelled.";
            }
            default -> {
                // Keep status as provided
            }
        }

        dispatchRepository.save(dispatch);
        ambulanceRepository.save(ambulance);
        emergencyRepository.save(emergency);

        StatusHistory history = new StatusHistory(dispatch.getId(), normalizedStatus, LocalDateTime.now(), note);
        statusHistoryRepository.save(history);

        return buildDispatchResponse(dispatch, emergency, ambulance, hospital);
    }

    @Transactional(readOnly = true)
    public List<DispatchResponse> getAllDispatches() {
        return dispatchRepository.findAll().stream()
                .map(this::buildResponseFromDispatch)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DispatchResponse getDispatchById(Long id) {
        Dispatch d = dispatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with ID: " + id));
        return buildResponseFromDispatch(d);
    }

    private DispatchResponse buildResponseFromDispatch(Dispatch d) {
        Emergency e = emergencyRepository.findById(d.getEmergencyId()).orElse(null);
        Ambulance a = ambulanceRepository.findById(d.getAmbulanceId()).orElse(null);
        Hospital h = hospitalRepository.findById(d.getHospitalId()).orElse(null);
        return buildDispatchResponse(d, e, a, h);
    }

    private DispatchResponse buildDispatchResponse(Dispatch d, Emergency e, Ambulance a, Hospital h) {
        DispatchResponse res = new DispatchResponse();
        res.setId(d.getId());
        res.setEmergencyId(d.getEmergencyId());
        res.setAmbulanceId(d.getAmbulanceId());
        res.setHospitalId(d.getHospitalId());
        res.setTotalDistanceKm(d.getTotalDistanceKm());
        res.setEstimatedResponseMinutes(d.getEstimatedResponseMinutes());
        res.setStatus(d.getStatus());
        res.setAssignedAt(d.getAssignedAt());
        res.setCompletedAt(d.getCompletedAt());

        if (e != null) {
            res.setEmergencyRefCode(e.getReferenceCode());
            res.setEmergencyType(e.getEmergencyType());
            res.setEmergencySeverity(e.getSeverity());
            res.setEmergencyLocation(e.getLocationNodeId());
        }

        if (a != null) {
            res.setAmbulanceVehicleCode(a.getVehicleCode());
        }

        if (h != null) {
            res.setHospitalName(h.getName());
            res.setHospitalLocation(h.getLocationNodeId());
        }

        try {
            if (d.getRouteEmergencyJson() != null) {
                res.setRouteToEmergency(objectMapper.readValue(d.getRouteEmergencyJson(), new TypeReference<List<String>>() {}));
            } else {
                res.setRouteToEmergency(Collections.emptyList());
            }

            if (d.getRouteHospitalJson() != null) {
                res.setRouteToHospital(objectMapper.readValue(d.getRouteHospitalJson(), new TypeReference<List<String>>() {}));
            } else {
                res.setRouteToHospital(Collections.emptyList());
            }
        } catch (Exception ex) {
            res.setRouteToEmergency(Collections.emptyList());
            res.setRouteToHospital(Collections.emptyList());
        }

        return res;
    }
}
