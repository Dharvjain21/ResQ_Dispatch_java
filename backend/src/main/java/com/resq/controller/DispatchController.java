package com.resq.controller;

import com.resq.dto.ApiResponse;
import com.resq.dto.DispatchResponse;
import com.resq.dto.StatusUpdateRequest;
import com.resq.service.DispatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatches")
@CrossOrigin(origins = "*")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/{emergencyId}/allocate")
    public ResponseEntity<ApiResponse<DispatchResponse>> allocateEmergency(@PathVariable Long emergencyId) {
        DispatchResponse dispatch = dispatchService.allocateEmergency(emergencyId);
        return new ResponseEntity<>(
                ApiResponse.ok("Optimal ambulance and hospital allocated via Dijkstra shortest-path algorithm.", dispatch),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DispatchResponse>>> getAllDispatches() {
        List<DispatchResponse> list = dispatchService.getAllDispatches();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + list.size() + " dispatch operation(s).", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispatchResponse>> getDispatchById(@PathVariable Long id) {
        DispatchResponse dispatch = dispatchService.getDispatchById(id);
        return ResponseEntity.ok(ApiResponse.ok("Dispatch retrieved.", dispatch));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DispatchResponse>> updateDispatchStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        DispatchResponse updated = dispatchService.updateDispatchStatus(id, request.getStatus(), request.getNote());
        return ResponseEntity.ok(ApiResponse.ok("Dispatch status updated to: " + updated.getStatus(), updated));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<DispatchResponse>> completeDispatch(@PathVariable Long id) {
        DispatchResponse completed = dispatchService.updateDispatchStatus(id, "COMPLETED", "Dispatch completed via rapid control action.");
        return ResponseEntity.ok(ApiResponse.ok("Mission completed. Ambulance returned to available status.", completed));
    }
}
