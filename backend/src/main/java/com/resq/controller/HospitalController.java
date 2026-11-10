package com.resq.controller;

import com.resq.dto.ApiResponse;
import com.resq.dto.HospitalResponse;
import com.resq.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> getHospitals() {
        List<HospitalResponse> list = hospitalService.getAllHospitals();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + list.size() + " hospital facility record(s).", list));
    }
}
