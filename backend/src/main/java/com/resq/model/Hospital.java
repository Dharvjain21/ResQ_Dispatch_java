package com.resq.model;

import jakarta.persistence.*;

/**
 * Hospital Entity with bed capacity and specialized facilities.
 */
@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id; // e.g. HOSP-1

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "location_node_id", nullable = false, length = 32)
    private String locationNodeId;

    @Column(name = "available_beds", nullable = false)
    private Integer availableBeds;

    @Column(name = "total_beds", nullable = false)
    private Integer totalBeds;

    @Column(name = "facilities", length = 512)
    private String facilities; // Comma-separated: General Emergency, Cardiac Care, Trauma Care, ICU, Burn Care

    @Column(name = "capacity_status", nullable = false, length = 32)
    private String capacityStatus; // NORMAL, NEAR_CAPACITY, FULL

    public Hospital() {}

    public Hospital(String id, String name, String locationNodeId, Integer availableBeds,
                    Integer totalBeds, String facilities, String capacityStatus) {
        this.id = id;
        this.name = name;
        this.locationNodeId = locationNodeId;
        this.availableBeds = availableBeds;
        this.totalBeds = totalBeds;
        this.facilities = facilities;
        this.capacityStatus = capacityStatus;
    }

    public boolean hasFacilityItem(String facility) {
        if (facilities == null || facility == null) return false;
        String[] parts = facilities.split(",");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(facility.trim()) || part.toLowerCase().contains(facility.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEligibleFor(String requiredFacility) {
        return (availableBeds != null && availableBeds > 0) && hasFacilityItem(requiredFacility);
    }

    public void updateCapacityStatus() {
        if (availableBeds == null || availableBeds <= 0) {
            this.capacityStatus = "FULL";
        } else if (totalBeds != null && availableBeds < (totalBeds * 0.25)) {
            this.capacityStatus = "NEAR_CAPACITY";
        } else {
            this.capacityStatus = "NORMAL";
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocationNodeId() { return locationNodeId; }
    public void setLocationNodeId(String locationNodeId) { this.locationNodeId = locationNodeId; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) {
        this.availableBeds = availableBeds;
        updateCapacityStatus();
    }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) {
        this.totalBeds = totalBeds;
        updateCapacityStatus();
    }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getCapacityStatus() { return capacityStatus; }
    public void setCapacityStatus(String capacityStatus) { this.capacityStatus = capacityStatus; }
}
