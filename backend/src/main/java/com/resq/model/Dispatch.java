package com.resq.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Dispatch Mission Entity connecting an Emergency with allocated Ambulance and Hospital.
 */
@Entity
@Table(name = "dispatches", indexes = {
    @Index(name = "idx_disp_status", columnList = "status"),
    @Index(name = "idx_disp_emg", columnList = "emergency_id"),
    @Index(name = "idx_disp_amb", columnList = "ambulance_id")
})
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emergency_id", nullable = false)
    private Long emergencyId;

    @Column(name = "ambulance_id", nullable = false, length = 32)
    private String ambulanceId;

    @Column(name = "hospital_id", nullable = false, length = 32)
    private String hospitalId;

    @Column(name = "route_emergency_json", columnDefinition = "TEXT")
    private String routeEmergencyJson; // Ordered JSON array string: ["N1","N2","N7"]

    @Column(name = "route_hospital_json", columnDefinition = "TEXT")
    private String routeHospitalJson; // Ordered JSON array string: ["N7","N16"]

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @Column(name = "estimated_response_minutes", nullable = false)
    private Double estimatedResponseMinutes;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // AMBULANCE_ASSIGNED, EN_ROUTE_TO_EMERGENCY, ON_SCENE, EN_ROUTE_TO_HOSPITAL, COMPLETED, CANCELLED

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Dispatch() {}

    public Dispatch(Long emergencyId, String ambulanceId, String hospitalId,
                    String routeEmergencyJson, String routeHospitalJson,
                    Double totalDistanceKm, Double estimatedResponseMinutes,
                    String status, LocalDateTime assignedAt) {
        this.emergencyId = emergencyId;
        this.ambulanceId = ambulanceId;
        this.hospitalId = hospitalId;
        this.routeEmergencyJson = routeEmergencyJson;
        this.routeHospitalJson = routeHospitalJson;
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedResponseMinutes = estimatedResponseMinutes;
        this.status = status != null ? status : "AMBULANCE_ASSIGNED";
        this.assignedAt = assignedAt != null ? assignedAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmergencyId() { return emergencyId; }
    public void setEmergencyId(Long emergencyId) { this.emergencyId = emergencyId; }

    public String getAmbulanceId() { return ambulanceId; }
    public void setAmbulanceId(String ambulanceId) { this.ambulanceId = ambulanceId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getRouteEmergencyJson() { return routeEmergencyJson; }
    public void setRouteEmergencyJson(String routeEmergencyJson) { this.routeEmergencyJson = routeEmergencyJson; }

    public String getRouteHospitalJson() { return routeHospitalJson; }
    public void setRouteHospitalJson(String routeHospitalJson) { this.routeHospitalJson = routeHospitalJson; }

    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public Double getEstimatedResponseMinutes() { return estimatedResponseMinutes; }
    public void setEstimatedResponseMinutes(Double estimatedResponseMinutes) { this.estimatedResponseMinutes = estimatedResponseMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
