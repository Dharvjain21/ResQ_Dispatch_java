package com.resq.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateRequest {

    @NotBlank(message = "Status cannot be blank")
    private String status; // AMBULANCE_ASSIGNED, EN_ROUTE_TO_EMERGENCY, ON_SCENE, EN_ROUTE_TO_HOSPITAL, COMPLETED, CANCELLED

    private String note;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(String status, String note) {
        this.status = status;
        this.note = note;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
