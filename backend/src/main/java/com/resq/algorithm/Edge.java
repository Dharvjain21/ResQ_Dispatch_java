package com.resq.algorithm;

/**
 * Represents a directed weighted road edge in the adjacency list.
 */
public class Edge {
    private final String targetNodeId;
    private final double distanceKm;

    public Edge(String targetNodeId, double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative for Dijkstra: " + distanceKm);
        }
        this.targetNodeId = targetNodeId;
        this.distanceKm = distanceKm;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "target='" + targetNodeId + '\'' +
                ", distanceKm=" + distanceKm +
                '}';
    }
}
