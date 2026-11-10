package com.resq.dto;

import com.resq.model.Ambulance;

public class AmbulanceResponse {
    private String id;
    private String vehicleCode;
    private String currentNodeId;
    private String ambulanceType;
    private Boolean available;
    private String equipment;
    private String status;
    private String currentAssignment;

    public AmbulanceResponse() {}

    public static AmbulanceResponse fromEntity(Ambulance a) {
        if (a == null) return null;
        AmbulanceResponse dto = new AmbulanceResponse();
        dto.id = a.getId();
        dto.vehicleCode = a.getVehicleCode();
        dto.currentNodeId = a.getCurrentNodeId();
        dto.ambulanceType = a.getAmbulanceType();
        dto.available = a.getAvailable();
        dto.equipment = a.getEquipment();
        dto.status = a.getStatus();
        dto.currentAssignment = a.getCurrentAssignment();
        return dto;
    }

    public String getId() { return id; }
    public String getVehicleCode() { return vehicleCode; }
    public String getCurrentNodeId() { return currentNodeId; }
    public String getAmbulanceType() { return ambulanceType; }
    public Boolean getAvailable() { return available; }
    public String getEquipment() { return equipment; }
    public String getStatus() { return status; }
    public String getCurrentAssignment() { return currentAssignment; }
}
