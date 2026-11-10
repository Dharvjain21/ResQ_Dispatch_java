package com.resq.dto;

import com.resq.model.Hospital;

public class HospitalResponse {
    private String id;
    private String name;
    private String locationNodeId;
    private Integer availableBeds;
    private Integer totalBeds;
    private String facilities;
    private String capacityStatus;

    public HospitalResponse() {}

    public static HospitalResponse fromEntity(Hospital h) {
        if (h == null) return null;
        HospitalResponse dto = new HospitalResponse();
        dto.id = h.getId();
        dto.name = h.getName();
        dto.locationNodeId = h.getLocationNodeId();
        dto.availableBeds = h.getAvailableBeds();
        dto.totalBeds = h.getTotalBeds();
        dto.facilities = h.getFacilities();
        dto.capacityStatus = h.getCapacityStatus();
        return dto;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocationNodeId() { return locationNodeId; }
    public Integer getAvailableBeds() { return availableBeds; }
    public Integer getTotalBeds() { return totalBeds; }
    public String getFacilities() { return facilities; }
    public String getCapacityStatus() { return capacityStatus; }
}
