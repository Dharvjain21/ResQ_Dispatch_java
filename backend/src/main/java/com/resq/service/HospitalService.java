package com.resq.service;

import com.resq.dto.HospitalResponse;
import com.resq.exception.ResourceNotFoundException;
import com.resq.model.Emergency;
import com.resq.model.Hospital;
import com.resq.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(HospitalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Hospital getHospitalEntity(String id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));
    }

    public String getRequiredFacility(Emergency emg) {
        if (emg == null || emg.getEmergencyType() == null) return "General Emergency";
        String type = emg.getEmergencyType();
        if (type.equalsIgnoreCase("Cardiac Emergency")) {
            return "Cardiac Care";
        } else if (type.equalsIgnoreCase("Fire Injury")) {
            return "Burn Care";
        } else if (type.equalsIgnoreCase("Road Accident")) {
            return "Trauma Care";
        }
        return "General Emergency";
    }

    @Transactional
    public void decrementBeds(String hospitalId) {
        Hospital hospital = getHospitalEntity(hospitalId);
        if (hospital.getAvailableBeds() > 0) {
            hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);
            hospitalRepository.save(hospital);
        }
    }

    @Transactional
    public void incrementBeds(String hospitalId) {
        Hospital hospital = getHospitalEntity(hospitalId);
        if (hospital.getAvailableBeds() < hospital.getTotalBeds()) {
            hospital.setAvailableBeds(hospital.getAvailableBeds() + 1);
            hospitalRepository.save(hospital);
        }
    }
}
