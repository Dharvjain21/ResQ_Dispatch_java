package com.resq.model;

import jakarta.persistence.*;

/**
 * Ambulance Vehicle Entity.
 */
@Entity
@Table(name = "ambulances", indexes = {
    @Index(name = "idx_amb_avail", columnList = "available"),
    @Index(name = "idx_amb_type", columnList = "ambulance_type")
})
public class Ambulance {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id; // e.g. AMB-101

    @Column(name = "vehicle_code", unique = true, nullable = false, length = 32)
    private String vehicleCode; // e.g. DL-01-EQ-9001

    @Column(name = "current_node_id", nullable = false, length = 32)
    private String currentNodeId;

    @Column(name = "ambulance_type", nullable = false, length = 64)
    private String ambulanceType; // ADVANCED_LIFE_SUPPORT, BASIC_LIFE_SUPPORT, PATIENT_TRANSPORT

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "equipment", length = 512)
    private String equipment; // Comma separated: Ventilator, Defibrillator, Cardiac Monitor, Oxygen

    @Column(name = "status", nullable = false, length = 32)
    private String status; // AVAILABLE, ASSIGNED, EN_ROUTE, ON_SCENE, MAINTENANCE

    @Column(name = "current_assignment", length = 64)
    private String currentAssignment;

    public Ambulance() {}

    public Ambulance(String id, String vehicleCode, String currentNodeId, String ambulanceType,
                     Boolean available, String equipment, String status) {
        this.id = id;
        this.vehicleCode = vehicleCode;
        this.currentNodeId = currentNodeId;
        this.ambulanceType = ambulanceType;
        this.available = available != null ? available : true;
        this.equipment = equipment;
        this.status = status != null ? status : "AVAILABLE";
    }

    public boolean hasEquipmentItem(String item) {
        if (equipment == null || item == null) return false;
        String[] parts = equipment.split(",");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(item.trim()) || part.toLowerCase().contains(item.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleCode() { return vehicleCode; }
    public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }

    public String getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(String currentNodeId) { this.currentNodeId = currentNodeId; }

    public String getAmbulanceType() { return ambulanceType; }
    public void setAmbulanceType(String ambulanceType) { this.ambulanceType = ambulanceType; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentAssignment() { return currentAssignment; }
    public void setCurrentAssignment(String currentAssignment) { this.currentAssignment = currentAssignment; }
}
