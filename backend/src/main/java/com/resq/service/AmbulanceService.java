package com.resq.service;

import com.resq.dto.AmbulanceResponse;
import com.resq.exception.ResourceNotFoundException;
import com.resq.model.Ambulance;
import com.resq.model.Emergency;
import com.resq.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    @Transactional(readOnly = true)
    public List<AmbulanceResponse> getAllAmbulances(Boolean availableOnly) {
        List<Ambulance> list;
        if (Boolean.TRUE.equals(availableOnly)) {
            list = ambulanceRepository.findByAvailableTrue();
        } else {
            list = ambulanceRepository.findAll();
        }
        return list.stream()
                .map(AmbulanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Ambulance getAmbulanceEntity(String id) {
        return ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with ID: " + id));
    }

    @Transactional
    public void updateAmbulanceStatus(String id, String status, Boolean available, String currentNodeId, String currentAssignment) {
        Ambulance amb = getAmbulanceEntity(id);
        if (status != null) amb.setStatus(status);
        if (available != null) amb.setAvailable(available);
        if (currentNodeId != null) amb.setCurrentNodeId(currentNodeId);
        amb.setCurrentAssignment(currentAssignment);
        ambulanceRepository.save(amb);
    }

    public boolean isAmbulanceSuitable(Ambulance amb, Emergency emg) {
        if (!Boolean.TRUE.equals(amb.getAvailable())) {
            return false;
        }

        String emgType = emg.getEmergencyType();
        if (emgType == null) return true;

        if (emgType.equalsIgnoreCase("Cardiac Emergency")) {
            // Requires ALS and Defibrillator / Cardiac Monitor
            boolean isAls = amb.getAmbulanceType().toUpperCase().contains("ADVANCED") ||
                            amb.getAmbulanceType().equalsIgnoreCase("ALS");
            return isAls && (amb.hasEquipmentItem("Defibrillator") || amb.hasEquipmentItem("Cardiac Monitor"));
        } else if (emgType.equalsIgnoreCase("Fire Injury")) {
            // Requires Oxygen
            return amb.hasEquipmentItem("Oxygen");
        } else if (emgType.equalsIgnoreCase("Road Accident")) {
            // Requires ALS or BLS (Patient Transport alone not suitable for serious crash)
            return !amb.getAmbulanceType().equalsIgnoreCase("PATIENT_TRANSPORT");
        }
        return true;
    }
}
