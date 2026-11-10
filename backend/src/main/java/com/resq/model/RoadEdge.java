package com.resq.model;

import jakarta.persistence.*;

/**
 * Represents a road segment between two intersections with distance in kilometers.
 */
@Entity
@Table(name = "road_edges")
public class RoadEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_node_id", nullable = false, length = 32)
    private String sourceNodeId;

    @Column(name = "target_node_id", nullable = false, length = 32)
    private String targetNodeId;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "is_bidirectional", nullable = false)
    private Boolean bidirectional = true;

    public RoadEdge() {}

    public RoadEdge(String sourceNodeId, String targetNodeId, Double distanceKm, Boolean bidirectional) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.distanceKm = distanceKm;
        this.bidirectional = bidirectional != null ? bidirectional : true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Boolean getBidirectional() { return bidirectional; }
    public void setBidirectional(Boolean bidirectional) { this.bidirectional = bidirectional; }
}
