package com.resq.algorithm;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the output of a Dijkstra shortest path calculation.
 */
public class PathResult {
    private final boolean reachable;
    private final double totalDistanceKm;
    private final List<String> pathNodes;
    private final String errorMessage;

    public PathResult(double totalDistanceKm, List<String> pathNodes) {
        this.reachable = true;
        this.totalDistanceKm = totalDistanceKm;
        this.pathNodes = Collections.unmodifiableList(pathNodes);
        this.errorMessage = null;
    }

    public PathResult(String errorMessage) {
        this.reachable = false;
        this.totalDistanceKm = Double.POSITIVE_INFINITY;
        this.pathNodes = Collections.emptyList();
        this.errorMessage = errorMessage;
    }

    public boolean isReachable() {
        return reachable;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public List<String> getPathNodes() {
        return pathNodes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (!reachable) {
            return "PathResult{unreachable, error='" + errorMessage + "'}";
        }
        return "PathResult{" +
                "path=" + String.join(" -> ", pathNodes) +
                ", totalDistanceKm=" + String.format("%.2f", totalDistanceKm) +
                '}';
    }
}
