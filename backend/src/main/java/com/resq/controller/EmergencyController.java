package com.resq.controller;

import com.resq.dto.ApiResponse;
import com.resq.dto.EmergencyCreateRequest;
import com.resq.dto.EmergencyResponse;
import com.resq.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergencies")
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmergencyResponse>> createEmergency(@Valid @RequestBody EmergencyCreateRequest request) {
        EmergencyResponse created = emergencyService.createEmergency(request);
        return new ResponseEntity<>(
                ApiResponse.ok("Emergency incident logged and queued into priority triage.", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmergencyResponse>>> getEmergencies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<EmergencyResponse> list = emergencyService.getAllEmergencies(status, severity);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + list.size() + " emergency incident(s).", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyResponse>> getEmergencyById(@PathVariable Long id) {
        EmergencyResponse response = emergencyService.getEmergencyById(id);
        return ResponseEntity.ok(ApiResponse.ok("Emergency found.", response));
    }
}
