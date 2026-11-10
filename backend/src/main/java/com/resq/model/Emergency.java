package com.resq.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Emergency Incident Entity.
 * 
 * Priority Sorting:
 * Primary: Severity (CRITICAL > HIGH > MEDIUM > LOW)
 * Secondary: Creation Time (FIFO)
 */
@Entity
@Table(name = "emergencies", indexes = {
    @Index(name = "idx_emg_status", columnList = "status"),
    @Index(name = "idx_emg_severity", columnList = "severity")
})
public class Emergency implements Comparable<Emergency> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_code", unique = true, nullable = false, length = 32)
    private String referenceCode;

    @Column(name = "location_node_id", nullable = false, length = 32)
    private String locationNodeId;

    @Column(name = "emergency_type", nullable = false, length = 64)
    private String emergencyType; // Cardiac Emergency, Road Accident, Fire Injury, General Medical Emergency

    @Column(name = "severity", nullable = false, length = 16)
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, ASSIGNED, RESOLVED, CANCELLED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Emergency() {}

    public Emergency(String referenceCode, String locationNodeId, String emergencyType,
                     String severity, String description, String status, LocalDateTime createdAt) {
        this.referenceCode = referenceCode;
        this.locationNodeId = locationNodeId;
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.description = description;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getSeverityWeight() {
        if (severity == null) return 0;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    @Override
    public int compareTo(Emergency other) {
        int weightCompare = Integer.compare(other.getSeverityWeight(), this.getSeverityWeight());
        if (weightCompare != 0) {
            return weightCompare;
        }
        return this.createdAt.compareTo(other.createdAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }

    public String getLocationNodeId() { return locationNodeId; }
    public void setLocationNodeId(String locationNodeId) { this.locationNodeId = locationNodeId; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
