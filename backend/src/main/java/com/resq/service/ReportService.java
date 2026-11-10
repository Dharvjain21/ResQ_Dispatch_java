package com.resq.service;

import com.resq.dto.ReportSummaryResponse;
import com.resq.model.Emergency;
import com.resq.model.Hospital;
import com.resq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final EmergencyRepository emergencyRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final DispatchRepository dispatchRepository;

    public ReportService(EmergencyRepository emergencyRepository,
                         AmbulanceRepository ambulanceRepository,
                         HospitalRepository hospitalRepository,
                         DispatchRepository dispatchRepository) {
        this.emergencyRepository = emergencyRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.dispatchRepository = dispatchRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummaryMetrics() {
        ReportSummaryResponse summary = new ReportSummaryResponse();

        long totalEmg = emergencyRepository.count();
        long pendingEmg = emergencyRepository.countByStatus("PENDING");
        long resolvedEmg = emergencyRepository.countByStatus("RESOLVED");
        long assignedEmg = emergencyRepository.countByStatus("ASSIGNED");
        long criticalEmg = emergencyRepository.countBySeverity("CRITICAL");

        summary.setTotalEmergencies(totalEmg);
        summary.setPendingEmergencies(pendingEmg);
        summary.setResolvedEmergencies(resolvedEmg);
        summary.setActiveEmergencies(pendingEmg + assignedEmg);
        summary.setCriticalEmergencies(criticalEmg);

        long totalAmbs = ambulanceRepository.count();
        long availAmbs = ambulanceRepository.countByAvailableTrue();
        summary.setTotalAmbulances(totalAmbs);
        summary.setAvailableAmbulances(availAmbs);
        summary.setBusyAmbulances(totalAmbs - availAmbs);

        List<Hospital> hospitals = hospitalRepository.findAll();
        summary.setTotalHospitals(hospitals.size());
        long totalAvailableBeds = hospitals.stream()
                .mapToLong(h -> h.getAvailableBeds() != null ? h.getAvailableBeds() : 0)
                .sum();
        summary.setAvailableBeds(totalAvailableBeds);

        long completedDispatches = dispatchRepository.countByStatus("COMPLETED");
        summary.setCompletedDispatches(completedDispatches);

        long activeDispatches = dispatchRepository.findAll().stream()
                .filter(d -> !"COMPLETED".equalsIgnoreCase(d.getStatus()) && !"CANCELLED".equalsIgnoreCase(d.getStatus()))
                .count();
        summary.setActiveDispatches(activeDispatches);

        Double avgResponse = dispatchRepository.calculateAverageResponseTime();
        summary.setAverageResponseTimeMinutes(avgResponse != null ? Math.round(avgResponse * 10.0) / 10.0 : 0.0);

        // Breakdown by type & severity
        List<Emergency> allEmg = emergencyRepository.findAll();
        Map<String, Long> byType = new HashMap<>();
        Map<String, Long> bySeverity = new HashMap<>();

        for (Emergency e : allEmg) {
            byType.put(e.getEmergencyType(), byType.getOrDefault(e.getEmergencyType(), 0L) + 1);
            bySeverity.put(e.getSeverity(), bySeverity.getOrDefault(e.getSeverity(), 0L) + 1);
        }

        summary.setEmergenciesByType(byType);
        summary.setEmergenciesBySeverity(bySeverity);

        return summary;
    }
}
