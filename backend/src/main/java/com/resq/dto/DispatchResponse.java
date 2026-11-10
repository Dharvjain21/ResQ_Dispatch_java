package com.resq.dto;

import com.resq.model.Dispatch;
import java.time.LocalDateTime;
import java.util.List;

public class DispatchResponse {
    private Long id;
    private Long emergencyId;
    private String emergencyRefCode;
    private String emergencyType;
    private String emergencySeverity;
    private String emergencyLocation;
    private String ambulanceId;
    private String ambulanceVehicleCode;
    private String hospitalId;
    private String hospitalName;
    private String hospitalLocation;
    private List<String> routeToEmergency;
    private List<String> routeToHospital;
    private Double totalDistanceKm;
    private Double estimatedResponseMinutes;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    public DispatchResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmergencyId() { return emergencyId; }
    public void setEmergencyId(Long emergencyId) { this.emergencyId = emergencyId; }

    public String getEmergencyRefCode() { return emergencyRefCode; }
    public void setEmergencyRefCode(String emergencyRefCode) { this.emergencyRefCode = emergencyRefCode; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getEmergencySeverity() { return emergencySeverity; }
    public void setEmergencySeverity(String emergencySeverity) { this.emergencySeverity = emergencySeverity; }

    public String getEmergencyLocation() { return emergencyLocation; }
    public void setEmergencyLocation(String emergencyLocation) { this.emergencyLocation = emergencyLocation; }

    public String getAmbulanceId() { return ambulanceId; }
    public void setAmbulanceId(String ambulanceId) { this.ambulanceId = ambulanceId; }

    public String getAmbulanceVehicleCode() { return ambulanceVehicleCode; }
    public void setAmbulanceVehicleCode(String ambulanceVehicleCode) { this.ambulanceVehicleCode = ambulanceVehicleCode; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalLocation() { return hospitalLocation; }
    public void setHospitalLocation(String hospitalLocation) { this.hospitalLocation = hospitalLocation; }

    public List<String> getRouteToEmergency() { return routeToEmergency; }
    public void setRouteToEmergency(List<String> routeToEmergency) { this.routeToEmergency = routeToEmergency; }

    public List<String> getRouteToHospital() { return routeToHospital; }
    public void setRouteToHospital(List<String> routeToHospital) { this.routeToHospital = routeToHospital; }

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
