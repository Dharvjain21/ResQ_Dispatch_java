package com.resq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmergencyCreateRequest {

    @NotBlank(message = "Location intersection ID is required")
    private String locationNodeId;

    @NotBlank(message = "Emergency type is required")
    private String emergencyType; // Cardiac Emergency, Road Accident, Fire Injury, General Medical Emergency

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "^(?i)(CRITICAL|HIGH|MEDIUM|LOW)$", message = "Severity must be CRITICAL, HIGH, MEDIUM, or LOW")
    private String severity;

    private String description;

    public EmergencyCreateRequest() {}

    public EmergencyCreateRequest(String locationNodeId, String emergencyType, String severity, String description) {
        this.locationNodeId = locationNodeId;
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.description = description;
    }

    public String getLocationNodeId() { return locationNodeId; }
    public void setLocationNodeId(String locationNodeId) { this.locationNodeId = locationNodeId; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
