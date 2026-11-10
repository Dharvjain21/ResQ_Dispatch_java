package com.resq.dto;

import com.resq.model.Emergency;
import java.time.LocalDateTime;

public class EmergencyResponse {
    private Long id;
    private String referenceCode;
    private String locationNodeId;
    private String emergencyType;
    private String severity;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public EmergencyResponse() {}

    public static EmergencyResponse fromEntity(Emergency e) {
        if (e == null) return null;
        EmergencyResponse dto = new EmergencyResponse();
        dto.id = e.getId();
        dto.referenceCode = e.getReferenceCode();
        dto.locationNodeId = e.getLocationNodeId();
        dto.emergencyType = e.getEmergencyType();
        dto.severity = e.getSeverity();
        dto.description = e.getDescription();
        dto.status = e.getStatus();
        dto.createdAt = e.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getReferenceCode() { return referenceCode; }
    public String getLocationNodeId() { return locationNodeId; }
    public String getEmergencyType() { return emergencyType; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
