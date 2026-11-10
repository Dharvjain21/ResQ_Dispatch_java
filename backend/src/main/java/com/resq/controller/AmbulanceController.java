package com.resq.controller;

import com.resq.dto.AmbulanceResponse;
import com.resq.dto.ApiResponse;
import com.resq.service.AmbulanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
@CrossOrigin(origins = "*")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    public AmbulanceController(AmbulanceService ambulanceService) {
        this.ambulanceService = ambulanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AmbulanceResponse>>> getAmbulances(
            @RequestParam(required = false) Boolean available) {
        List<AmbulanceResponse> list = ambulanceService.getAllAmbulances(available);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + list.size() + " ambulance unit(s).", list));
    }
}
