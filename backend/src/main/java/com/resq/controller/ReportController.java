package com.resq.controller;

import com.resq.dto.ApiResponse;
import com.resq.dto.ReportSummaryResponse;
import com.resq.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getSummary() {
        ReportSummaryResponse summary = reportService.getSummaryMetrics();
        return ResponseEntity.ok(ApiResponse.ok("Operational metrics summary retrieved.", summary));
    }
}
