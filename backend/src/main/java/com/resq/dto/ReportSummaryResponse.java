package com.resq.dto;

import java.util.Map;

public class ReportSummaryResponse {
    private long totalEmergencies;
    private long activeEmergencies;
    private long pendingEmergencies;
    private long resolvedEmergencies;
    private long criticalEmergencies;

    private long totalAmbulances;
    private long availableAmbulances;
    private long busyAmbulances;

    private long totalHospitals;
    private long availableBeds;

    private long completedDispatches;
    private long activeDispatches;
    private double averageResponseTimeMinutes;

    private Map<String, Long> emergenciesByType;
    private Map<String, Long> emergenciesBySeverity;

    public ReportSummaryResponse() {}

    public long getTotalEmergencies() { return totalEmergencies; }
    public void setTotalEmergencies(long totalEmergencies) { this.totalEmergencies = totalEmergencies; }

    public long getActiveEmergencies() { return activeEmergencies; }
    public void setActiveEmergencies(long activeEmergencies) { this.activeEmergencies = activeEmergencies; }

    public long getPendingEmergencies() { return pendingEmergencies; }
    public void setPendingEmergencies(long pendingEmergencies) { this.pendingEmergencies = pendingEmergencies; }

    public long getResolvedEmergencies() { return resolvedEmergencies; }
    public void setResolvedEmergencies(long resolvedEmergencies) { this.resolvedEmergencies = resolvedEmergencies; }

    public long getCriticalEmergencies() { return criticalEmergencies; }
    public void setCriticalEmergencies(long criticalEmergencies) { this.criticalEmergencies = criticalEmergencies; }

    public long getTotalAmbulances() { return totalAmbulances; }
    public void setTotalAmbulances(long totalAmbulances) { this.totalAmbulances = totalAmbulances; }

    public long getAvailableAmbulances() { return availableAmbulances; }
    public void setAvailableAmbulances(long availableAmbulances) { this.availableAmbulances = availableAmbulances; }

    public long getBusyAmbulances() { return busyAmbulances; }
    public void setBusyAmbulances(long busyAmbulances) { this.busyAmbulances = busyAmbulances; }

    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }

    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }

    public long getCompletedDispatches() { return completedDispatches; }
    public void setCompletedDispatches(long completedDispatches) { this.completedDispatches = completedDispatches; }

    public long getActiveDispatches() { return activeDispatches; }
    public void setActiveDispatches(long activeDispatches) { this.activeDispatches = activeDispatches; }

    public double getAverageResponseTimeMinutes() { return averageResponseTimeMinutes; }
    public void setAverageResponseTimeMinutes(double averageResponseTimeMinutes) { this.averageResponseTimeMinutes = averageResponseTimeMinutes; }

    public Map<String, Long> getEmergenciesByType() { return emergenciesByType; }
    public void setEmergenciesByType(Map<String, Long> emergenciesByType) { this.emergenciesByType = emergenciesByType; }

    public Map<String, Long> getEmergenciesBySeverity() { return emergenciesBySeverity; }
    public void setEmergenciesBySeverity(Map<String, Long> emergenciesBySeverity) { this.emergenciesBySeverity = emergenciesBySeverity; }
}
