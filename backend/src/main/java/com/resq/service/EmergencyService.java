package com.resq.service;

import com.resq.dto.EmergencyCreateRequest;
import com.resq.dto.EmergencyResponse;
import com.resq.exception.ResourceNotFoundException;
import com.resq.model.Emergency;
import com.resq.repository.EmergencyRepository;
import com.resq.repository.RoadNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final RoadNodeRepository roadNodeRepository;

    public EmergencyService(EmergencyRepository emergencyRepository, RoadNodeRepository roadNodeRepository) {
        this.emergencyRepository = emergencyRepository;
        this.roadNodeRepository = roadNodeRepository;
    }

    @Transactional
    public EmergencyResponse createEmergency(EmergencyCreateRequest request) {
        if (!roadNodeRepository.existsById(request.getLocationNodeId())) {
            throw new ResourceNotFoundException("Location node '" + request.getLocationNodeId() + "' does not exist in the road network");
        }

        String refCode = "EMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Emergency emergency = new Emergency(
                refCode,
                request.getLocationNodeId().trim(),
                request.getEmergencyType().trim(),
                request.getSeverity().trim().toUpperCase(),
                request.getDescription() != null ? request.getDescription().trim() : "",
                "PENDING",
                LocalDateTime.now()
        );

        Emergency saved = emergencyRepository.save(emergency);
        return EmergencyResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EmergencyResponse> getAllEmergencies(String status, String severity) {
        List<Emergency> all;
        if ("PENDING".equalsIgnoreCase(status)) {
            all = emergencyRepository.findPendingEmergenciesSortedByPriority();
        } else if (status != null && !status.isBlank()) {
            all = emergencyRepository.findByStatus(status.toUpperCase());
        } else {
            all = emergencyRepository.findAll();
            // Sort by priority logic
            all.sort((e1, e2) -> e1.compareTo(e2));
        }

        if (severity != null && !severity.isBlank()) {
            all = all.stream()
                    .filter(e -> e.getSeverity().equalsIgnoreCase(severity))
                    .collect(Collectors.toList());
        }

        return all.stream()
                .map(EmergencyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Emergency getEmergencyEntity(Long id) {
        return emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public EmergencyResponse getEmergencyById(Long id) {
        return EmergencyResponse.fromEntity(getEmergencyEntity(id));
    }

    @Transactional
    public void updateEmergencyStatus(Long id, String status) {
        Emergency emergency = getEmergencyEntity(id);
        emergency.setStatus(status);
        emergencyRepository.save(emergency);
    }
}
