package com.resq.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical status audit trail for dispatch lifecycle tracking.
 */
@Entity
@Table(name = "status_history", indexes = {
    @Index(name = "idx_sh_disp_id", columnList = "dispatch_id")
})
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "note", length = 256)
    private String note;

    public StatusHistory() {}

    public StatusHistory(Long dispatchId, String status, LocalDateTime timestamp, String note) {
        this.dispatchId = dispatchId;
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
