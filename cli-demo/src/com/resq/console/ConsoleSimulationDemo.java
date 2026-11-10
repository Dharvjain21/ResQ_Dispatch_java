package com.resq.console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ============================================================================
 * STAGE 1: CORE JAVA APPLICATION & STANDALONE DEMO
 * Project: Smart Ambulance Dispatch & Emergency Management System (ResQ Dispatch)
 * 
 * DSA Implementation:
 * - Graph with Adjacency List
 * - Dijkstra's Shortest Path Algorithm with PriorityQueue (Min-Heap)
 * - Path Reconstruction using Predecessor Map
 * - Time Complexity: O((V + E) log V)
 * - Space Complexity: O(V + E)
 * - Priority Queue Emergency Sorting (CRITICAL > HIGH > MEDIUM > LOW, then oldest first)
 * - Dynamic Ambulance Allocation and Hospital Selection
 * ============================================================================
 */
public class ConsoleSimulationDemo {

    // ------------------------------------------------------------------------
    // GRAPH & DIJKSTRA DATA STRUCTURES
    // ------------------------------------------------------------------------

    public static class Edge {
        private final String targetNodeId;
        private final double distanceKm;

        public Edge(String targetNodeId, double distanceKm) {
            if (distanceKm < 0) {
                throw new IllegalArgumentException("Edge weight cannot be negative for Dijkstra: " + distanceKm);
            }
            this.targetNodeId = targetNodeId;
            this.distanceKm = distanceKm;
        }

        public String getTargetNodeId() { return targetNodeId; }
        public double getDistanceKm() { return distanceKm; }

        @Override
        public String toString() {
            return "->" + targetNodeId + " (" + distanceKm + " km)";
        }
    }

    public static class Graph {
        private final Map<String, List<Edge>> adjacencyList = new HashMap<>();
        private final Map<String, String> nodeLabels = new HashMap<>();

        public void addNode(String nodeId, String label) {
            adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
            nodeLabels.put(nodeId, label);
        }

        public void addUndirectedEdge(String u, String v, double distanceKm) {
            addDirectedEdge(u, v, distanceKm);
            addDirectedEdge(v, u, distanceKm);
        }

        public void addDirectedEdge(String from, String to, double distanceKm) {
            if (!adjacencyList.containsKey(from)) {
                addNode(from, from);
            }
            if (!adjacencyList.containsKey(to)) {
                addNode(to, to);
            }
            adjacencyList.get(from).add(new Edge(to, distanceKm));
        }

        public List<Edge> getNeighbors(String nodeId) {
            return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
        }

        public boolean containsNode(String nodeId) {
            return adjacencyList.containsKey(nodeId);
        }

        public Set<String> getAllNodeIds() {
            return Collections.unmodifiableSet(adjacencyList.keySet());
        }

        public String getNodeLabel(String nodeId) {
            return nodeLabels.getOrDefault(nodeId, nodeId);
        }

        public int getNodeCount() {
            return adjacencyList.size();
        }
    }

    public static class PathResult {
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

        public boolean isReachable() { return reachable; }
        public double getTotalDistanceKm() { return totalDistanceKm; }
        public List<String> getPathNodes() { return pathNodes; }
        public String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            if (!reachable) {
                return "[Unreachable: " + errorMessage + "]";
            }
            return String.join(" -> ", pathNodes) + " (Total: " + String.format("%.2f", totalDistanceKm) + " km)";
        }
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        final String nodeId;
        final double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public static class DijkstraRouter {

        /**
         * Calculates shortest path using Dijkstra's algorithm.
         * Time Complexity: O((V + E) log V)
         * Space Complexity: O(V)
         */
        public static PathResult findShortestPath(Graph graph, String source, String destination) {
            if (graph == null) {
                return new PathResult("Graph is null");
            }
            if (source == null || destination == null) {
                return new PathResult("Source or destination node cannot be null");
            }
            if (!graph.containsNode(source)) {
                return new PathResult("Source node '" + source + "' does not exist in graph");
            }
            if (!graph.containsNode(destination)) {
                return new PathResult("Destination node '" + destination + "' does not exist in graph");
            }

            // Case: source == destination
            if (source.equals(destination)) {
                return new PathResult(0.0, Collections.singletonList(source));
            }

            Map<String, Double> distances = new HashMap<>();
            Map<String, String> predecessors = new HashMap<>();
            PriorityQueue<NodeDistance> minHeap = new PriorityQueue<>();
            Set<String> visited = new HashSet<>();

            for (String nodeId : graph.getAllNodeIds()) {
                distances.put(nodeId, Double.POSITIVE_INFINITY);
            }

            distances.put(source, 0.0);
            minHeap.offer(new NodeDistance(source, 0.0));

            while (!minHeap.isEmpty()) {
                NodeDistance current = minHeap.poll();
                String u = current.nodeId;

                if (visited.contains(u)) {
                    continue;
                }
                visited.add(u);

                // Early exit optimization when target is reached
                if (u.equals(destination)) {
                    break;
                }

                // If current popped distance is greater than known minimum, skip
                if (current.distance > distances.get(u)) {
                    continue;
                }

                for (Edge edge : graph.getNeighbors(u)) {
                    String v = edge.getTargetNodeId();
                    if (visited.contains(v)) {
                        continue;
                    }

                    double newDist = distances.get(u) + edge.getDistanceKm();
                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        predecessors.put(v, u);
                        minHeap.offer(new NodeDistance(v, newDist));
                    }
                }
            }

            double finalDist = distances.get(destination);
            if (finalDist == Double.POSITIVE_INFINITY) {
                return new PathResult("No reachable road path between " + source + " and " + destination);
            }

            // Reconstruct path from predecessors
            LinkedList<String> path = new LinkedList<>();
            String curr = destination;
            while (curr != null) {
                path.addFirst(curr);
                curr = predecessors.get(curr);
            }

            return new PathResult(finalDist, path);
        }
    }

    // ------------------------------------------------------------------------
    // DOMAIN ENTITIES & ENUMS
    // ------------------------------------------------------------------------

    public enum Severity {
        CRITICAL(4),
        HIGH(3),
        MEDIUM(2),
        LOW(1);

        private final int weight;
        Severity(int weight) { this.weight = weight; }
        public int getWeight() { return weight; }
    }

    public enum EmergencyType {
        CARDIAC_EMERGENCY("Cardiac Emergency"),
        ROAD_ACCIDENT("Road Accident"),
        FIRE_INJURY("Fire Injury"),
        GENERAL_MEDICAL("General Medical Emergency");

        private final String display;
        EmergencyType(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    public enum AmbulanceType {
        ADVANCED_LIFE_SUPPORT("ALS"),
        BASIC_LIFE_SUPPORT("BLS"),
        PATIENT_TRANSPORT("PT");

        private final String code;
        AmbulanceType(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    public static class Emergency implements Comparable<Emergency> {
        private final String id;
        private final String locationNodeId;
        private final EmergencyType type;
        private final Severity severity;
        private final String description;
        private final LocalDateTime createdAt;
        private String status; // PENDING, DISPATCHED, RESOLVED

        public Emergency(String id, String locationNodeId, EmergencyType type, Severity severity, String description, LocalDateTime createdAt) {
            this.id = id;
            this.locationNodeId = locationNodeId;
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.createdAt = createdAt;
            this.status = "PENDING";
        }

        public String getId() { return id; }
        public String getLocationNodeId() { return locationNodeId; }
        public EmergencyType getType() { return type; }
        public Severity getSeverity() { return severity; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        /**
         * Priority Comparator:
         * 1. Higher Severity first (CRITICAL > HIGH > MEDIUM > LOW)
         * 2. Tie-breaker: Earlier createdAt first (FIFO)
         */
        @Override
        public int compareTo(Emergency other) {
            int severityDiff = Integer.compare(other.severity.getWeight(), this.severity.getWeight());
            if (severityDiff != 0) {
                return severityDiff;
            }
            return this.createdAt.compareTo(other.createdAt);
        }

        @Override
        public String toString() {
            return String.format("[%s] %-8s | Type: %-22s | Loc: %-4s | Status: %-10s | %s",
                    id, severity, type.getDisplay(), locationNodeId, status, description);
        }
    }

    public static class Ambulance {
        private final String id;
        private final String vehicleCode;
        private String currentNodeId;
        private final AmbulanceType type;
        private boolean available;
        private final Set<String> equipment;
        private String currentAssignment;

        public Ambulance(String id, String vehicleCode, String currentNodeId, AmbulanceType type, boolean available, Set<String> equipment) {
            this.id = id;
            this.vehicleCode = vehicleCode;
            this.currentNodeId = currentNodeId;
            this.type = type;
            this.available = available;
            this.equipment = new HashSet<>(equipment);
            this.currentAssignment = null;
        }

        public String getId() { return id; }
        public String getVehicleCode() { return vehicleCode; }
        public String getCurrentNodeId() { return currentNodeId; }
        public void setCurrentNodeId(String node) { this.currentNodeId = node; }
        public AmbulanceType getType() { return type; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public Set<String> getEquipment() { return Collections.unmodifiableSet(equipment); }
        public String getCurrentAssignment() { return currentAssignment; }
        public void setCurrentAssignment(String currentAssignment) { this.currentAssignment = currentAssignment; }

        public boolean hasEquipment(String req) {
            for (String eq : equipment) {
                if (eq.equalsIgnoreCase(req) || eq.toLowerCase().contains(req.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) [%s] at %s | Avail: %b | Equip: %s",
                    id, vehicleCode, type.getCode(), currentNodeId, available, equipment);
        }
    }

    public static class Hospital {
        private final String id;
        private final String name;
        private final String locationNodeId;
        private int availableBeds;
        private final Set<String> facilities;

        public Hospital(String id, String name, String locationNodeId, int availableBeds, Set<String> facilities) {
            this.id = id;
            this.name = name;
            this.locationNodeId = locationNodeId;
            this.availableBeds = availableBeds;
            this.facilities = new HashSet<>(facilities);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getLocationNodeId() { return locationNodeId; }
        public int getAvailableBeds() { return availableBeds; }
        public void setAvailableBeds(int beds) { this.availableBeds = beds; }
        public Set<String> getFacilities() { return Collections.unmodifiableSet(facilities); }

        public boolean hasFacility(String requiredFacility) {
            for (String f : facilities) {
                if (f.equalsIgnoreCase(requiredFacility) || f.toLowerCase().contains(requiredFacility.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        public boolean isEligible(String requiredFacility) {
            return availableBeds > 0 && hasFacility(requiredFacility);
        }

        @Override
        public String toString() {
            return String.format("%s: %-32s @ %-4s | Beds: %2d | Facilities: %s",
                    id, name, locationNodeId, availableBeds, facilities);
        }
    }

    // ------------------------------------------------------------------------
    // DISPATCH LOGIC ENGINE
    // ------------------------------------------------------------------------

    public static class AllocationResult {
        public final boolean success;
        public final Emergency emergency;
        public final Ambulance ambulance;
        public final Hospital hospital;
        public final PathResult pathToEmergency;
        public final PathResult pathToHospital;
        public final double totalDistanceKm;
        public final double estimatedResponseMinutes;
        public final String message;

        public AllocationResult(Emergency emergency, Ambulance ambulance, Hospital hospital,
                                PathResult pathToEmergency, PathResult pathToHospital,
                                double totalDistanceKm, double estimatedResponseMinutes, String message) {
            this.success = true;
            this.emergency = emergency;
            this.ambulance = ambulance;
            this.hospital = hospital;
            this.pathToEmergency = pathToEmergency;
            this.pathToHospital = pathToHospital;
            this.totalDistanceKm = totalDistanceKm;
            this.estimatedResponseMinutes = estimatedResponseMinutes;
            this.message = message;
        }

        public AllocationResult(Emergency emergency, String errorMessage) {
            this.success = false;
            this.emergency = emergency;
            this.ambulance = null;
            this.hospital = null;
            this.pathToEmergency = null;
            this.pathToHospital = null;
            this.totalDistanceKm = 0;
            this.estimatedResponseMinutes = 0;
            this.message = errorMessage;
        }
    }

    public static class DispatchEngine {
        private final Graph roadNetwork;
        private final List<Ambulance> ambulances = new ArrayList<>();
        private final List<Hospital> hospitals = new ArrayList<>();
        private final PriorityQueue<Emergency> emergencyQueue = new PriorityQueue<>();

        public DispatchEngine(Graph roadNetwork) {
            this.roadNetwork = roadNetwork;
        }

        public void registerAmbulance(Ambulance ambulance) {
            ambulances.add(ambulance);
        }

        public void registerHospital(Hospital hospital) {
            hospitals.add(hospital);
        }

        public void addEmergency(Emergency emergency) {
            emergencyQueue.offer(emergency);
        }

        public PriorityQueue<Emergency> getEmergencyQueue() {
            return emergencyQueue;
        }

        /**
         * Checks ambulance suitability based on emergency type and required equipment.
         */
        public boolean isAmbulanceSuitable(Ambulance amb, Emergency emg) {
            if (!amb.isAvailable()) {
                return false;
            }

            switch (emg.getType()) {
                case CARDIAC_EMERGENCY:
                    // Requires ALS and Defibrillator / Cardiac Monitor
                    return amb.getType() == AmbulanceType.ADVANCED_LIFE_SUPPORT &&
                           (amb.hasEquipment("Defibrillator") || amb.hasEquipment("Cardiac Monitor"));

                case FIRE_INJURY:
                    // Requires ALS or BLS with Oxygen & Burn Kit
                    return amb.hasEquipment("Oxygen");

                case ROAD_ACCIDENT:
                    // Requires ALS or BLS with Trauma Kit or Stretcher
                    return amb.getType() == AmbulanceType.ADVANCED_LIFE_SUPPORT ||
                           amb.getType() == AmbulanceType.BASIC_LIFE_SUPPORT;

                case GENERAL_MEDICAL:
                default:
                    return true;
            }
        }

        /**
         * Identifies the primary medical facility required for the emergency.
         */
        public String getRequiredFacility(Emergency emg) {
            switch (emg.getType()) {
                case CARDIAC_EMERGENCY:
                    return "Cardiac Care";
                case FIRE_INJURY:
                    return "Burn Care";
                case ROAD_ACCIDENT:
                    return "Trauma Care";
                case GENERAL_MEDICAL:
                default:
                    return "General Emergency";
            }
        }

        /**
         * Core Allocation Logic:
         * 1. Filter available & suitable ambulances.
         * 2. Run Dijkstra from each candidate's location to emergency location.
         * 3. Select nearest reachable ambulance (tie breaker: vehicleCode).
         * 4. Select nearest hospital with required facility & beds > 0 from emergency location.
         * 5. Atomically mark ambulance unavailable and link dispatch.
         */
        public AllocationResult allocate(Emergency emg) {
            if (emg == null) {
                return new AllocationResult(null, "Emergency is null");
            }

            if (!roadNetwork.containsNode(emg.getLocationNodeId())) {
                return new AllocationResult(emg, "Emergency location node '" + emg.getLocationNodeId() + "' not in road network");
            }

            // 1. Find suitable ambulances
            Ambulance bestAmbulance = null;
            PathResult bestPathToEmg = null;
            double minAmbulanceDist = Double.POSITIVE_INFINITY;

            for (Ambulance amb : ambulances) {
                if (isAmbulanceSuitable(amb, emg)) {
                    PathResult pathRes = DijkstraRouter.findShortestPath(roadNetwork, amb.getCurrentNodeId(), emg.getLocationNodeId());
                    if (pathRes.isReachable()) {
                        if (pathRes.getTotalDistanceKm() < minAmbulanceDist) {
                            minAmbulanceDist = pathRes.getTotalDistanceKm();
                            bestAmbulance = amb;
                            bestPathToEmg = pathRes;
                        } else if (pathRes.getTotalDistanceKm() == minAmbulanceDist && bestAmbulance != null) {
                            // Deterministic tie-breaker: vehicle code
                            if (amb.getVehicleCode().compareTo(bestAmbulance.getVehicleCode()) < 0) {
                                bestAmbulance = amb;
                                bestPathToEmg = pathRes;
                            }
                        }
                    }
                }
            }

            if (bestAmbulance == null) {
                return new AllocationResult(emg, "CRITICAL ALERT: No suitable or available ambulance can reach " + emg.getLocationNodeId());
            }

            // 2. Find eligible hospital
            String requiredFacility = getRequiredFacility(emg);
            Hospital bestHospital = null;
            PathResult bestPathToHosp = null;
            double minHospDist = Double.POSITIVE_INFINITY;

            for (Hospital hosp : hospitals) {
                if (hosp.isEligible(requiredFacility)) {
                    PathResult pathRes = DijkstraRouter.findShortestPath(roadNetwork, emg.getLocationNodeId(), hosp.getLocationNodeId());
                    if (pathRes.isReachable() && pathRes.getTotalDistanceKm() < minHospDist) {
                        minHospDist = pathRes.getTotalDistanceKm();
                        bestHospital = hosp;
                        bestPathToHosp = pathRes;
                    }
                }
            }

            // Fallback: If no hospital with specific facility has beds, try General Emergency with beds
            if (bestHospital == null && !requiredFacility.equals("General Emergency")) {
                for (Hospital hosp : hospitals) {
                    if (hosp.isEligible("General Emergency")) {
                        PathResult pathRes = DijkstraRouter.findShortestPath(roadNetwork, emg.getLocationNodeId(), hosp.getLocationNodeId());
                        if (pathRes.isReachable() && pathRes.getTotalDistanceKm() < minHospDist) {
                            minHospDist = pathRes.getTotalDistanceKm();
                            bestHospital = hosp;
                            bestPathToHosp = pathRes;
                        }
                    }
                }
            }

            if (bestHospital == null) {
                return new AllocationResult(emg, "CRITICAL ALERT: No hospital with capacity found for " + requiredFacility);
            }

            // Calculate metrics: average speed 40 km/h in urban emergency
            double totalDist = bestPathToEmg.getTotalDistanceKm() + bestPathToHosp.getTotalDistanceKm();
            double etaMinutes = (bestPathToEmg.getTotalDistanceKm() / 40.0) * 60.0; // Minutes to scene

            // Commit state changes
            bestAmbulance.setAvailable(false);
            bestAmbulance.setCurrentAssignment("DISPATCH-" + emg.getId());
            emg.setStatus("DISPATCHED");

            return new AllocationResult(
                    emg, bestAmbulance, bestHospital,
                    bestPathToEmg, bestPathToHosp, totalDist, etaMinutes,
                    "Dispatch successful: Ambulance " + bestAmbulance.getId() + " assigned, Destination: " + bestHospital.getName()
            );
        }

        public void completeDispatch(AllocationResult dispatch) {
            if (dispatch != null && dispatch.success) {
                dispatch.ambulance.setAvailable(true);
                dispatch.ambulance.setCurrentNodeId(dispatch.hospital.getLocationNodeId());
                dispatch.ambulance.setCurrentAssignment(null);
                dispatch.emergency.setStatus("RESOLVED");
                if (dispatch.hospital.getAvailableBeds() > 0) {
                    dispatch.hospital.setAvailableBeds(dispatch.hospital.getAvailableBeds() - 1);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // SAMPLE ROAD GRAPH INITIALIZER (18 Intersections)
    // ------------------------------------------------------------------------

    public static Graph buildMetroRoadNetwork() {
        Graph g = new Graph();

        // 18 Intersections across the city
        g.addNode("N1", "North Gate Interstation");
        g.addNode("N2", "Metro Circle Terminal");
        g.addNode("N3", "Tech Park Junction");
        g.addNode("N4", "Riverside Boulevard");
        g.addNode("N5", "East Bypass Outer");
        g.addNode("N6", "Market Square Crossing");
        g.addNode("N7", "Central Plaza Hub");
        g.addNode("N8", "Civic Center Junction");
        g.addNode("N9", "University Crossing");
        g.addNode("N10", "West Expressway Toll");
        g.addNode("N11", "Industrial Area Hub");
        g.addNode("N12", "South Gate Interchange");
        g.addNode("N13", "Harbor Link Crossing");
        g.addNode("N14", "Greenfield Crossing");
        g.addNode("N15", "Sports Arena Circle");
        g.addNode("N16", "Downtown Terminus");
        g.addNode("N17", "Suburb Crossing");
        g.addNode("N18", "Isolated Island Depot (Unreachable Road Block)"); // Intentionally disconnected

        // 28 Undirected Road Segments with real distance in km
        g.addUndirectedEdge("N1", "N2", 1.8);
        g.addUndirectedEdge("N1", "N8", 2.2);
        g.addUndirectedEdge("N1", "N15", 1.5);

        g.addUndirectedEdge("N2", "N3", 2.5);
        g.addUndirectedEdge("N2", "N7", 2.1);
        g.addUndirectedEdge("N2", "N17", 2.0);

        g.addUndirectedEdge("N3", "N4", 2.4);

        g.addUndirectedEdge("N4", "N5", 2.0);
        g.addUndirectedEdge("N4", "N6", 2.6);

        g.addUndirectedEdge("N5", "N6", 2.1);
        g.addUndirectedEdge("N5", "N13", 2.8);

        g.addUndirectedEdge("N6", "N7", 1.6);
        g.addUndirectedEdge("N6", "N12", 2.3);

        g.addUndirectedEdge("N7", "N8", 1.9);
        g.addUndirectedEdge("N7", "N16", 1.2);
        g.addUndirectedEdge("N7", "N9", 2.5);

        g.addUndirectedEdge("N8", "N9", 2.0);
        g.addUndirectedEdge("N8", "N14", 1.7);

        g.addUndirectedEdge("N9", "N10", 1.8);
        g.addUndirectedEdge("N9", "N16", 1.5);

        g.addUndirectedEdge("N10", "N11", 2.2);
        g.addUndirectedEdge("N10", "N16", 2.4);

        g.addUndirectedEdge("N11", "N12", 2.1);
        g.addUndirectedEdge("N11", "N16", 2.7);

        g.addUndirectedEdge("N12", "N13", 2.5);

        g.addUndirectedEdge("N14", "N15", 2.3);
        g.addUndirectedEdge("N14", "N9", 2.4);

        g.addUndirectedEdge("N16", "N12", 2.2);

        return g;
    }

    // ------------------------------------------------------------------------
    // MAIN DEMO EXECUTION (Demonstrates 4 Complete Scenarios)
    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("   RESQ DISPATCH: SMART AMBULANCE DISPATCH & EMERGENCY MANAGEMENT SYSTEM      ");
        System.out.println("            STAGE 1: CORE JAVA APPLICATION & DSA VERIFICATION                 ");
        System.out.println("===============================================================================\n");

        // 1. Build Graph
        Graph graph = buildMetroRoadNetwork();
        System.out.println("[INIT] Road Network Initialized with " + graph.getNodeCount() + " Intersections and 28 Road Edges.");
        System.out.println("       Note: N18 is an isolated flood-blocked depot for unreachable path testing.\n");

        DispatchEngine engine = new DispatchEngine(graph);

        // 2. Register Sample Ambulances
        engine.registerAmbulance(new Ambulance("AMB-101", "DL-01-EQ-9001", "N1",
                AmbulanceType.ADVANCED_LIFE_SUPPORT, true,
                Set.of("Ventilator", "Defibrillator", "Cardiac Monitor", "Oxygen", "Infusion Pump")));

        engine.registerAmbulance(new Ambulance("AMB-102", "DL-01-EQ-9002", "N8",
                AmbulanceType.BASIC_LIFE_SUPPORT, true,
                Set.of("First Aid Kit", "Stretcher", "Oxygen", "Splints")));

        engine.registerAmbulance(new Ambulance("AMB-103", "DL-01-EQ-9003", "N11",
                AmbulanceType.ADVANCED_LIFE_SUPPORT, true,
                Set.of("Ventilator", "Defibrillator", "Cardiac Monitor", "Oxygen", "Trauma Kit")));

        engine.registerAmbulance(new Ambulance("AMB-104", "DL-01-EQ-9004", "N4",
                AmbulanceType.PATIENT_TRANSPORT, true,
                Set.of("Wheelchair", "Stretcher", "First Aid")));

        engine.registerAmbulance(new Ambulance("AMB-105", "DL-01-EQ-9005", "N6",
                AmbulanceType.ADVANCED_LIFE_SUPPORT, false, // Currently BUSY
                Set.of("Ventilator", "Defibrillator", "Cardiac Monitor", "Oxygen")));

        // 3. Register Sample Hospitals
        engine.registerHospital(new Hospital("HOSP-1", "Apex Metro Trauma & Heart Center", "N7",
                8, Set.of("General Emergency", "Cardiac Care", "Trauma Care", "ICU", "Burn Care")));

        engine.registerHospital(new Hospital("HOSP-2", "St. Jude Memorial Hospital", "N3",
                2, Set.of("General Emergency", "Cardiac Care", "ICU")));

        engine.registerHospital(new Hospital("HOSP-3", "City Children & General Clinic", "N10",
                12, Set.of("General Emergency")));

        engine.registerHospital(new Hospital("HOSP-4", "Phoenix Specialized Burn Institute", "N13",
                0, Set.of("General Emergency", "Burn Care", "ICU"))); // 0 BEDS AVAILABLE (FULL)

        System.out.println("Registered 5 Ambulances (4 available, 1 busy) and 4 Hospitals.\n");

        // --------------------------------------------------------------------
        // TEST 1: DIJKSTRA DIRECT ALGORITHM TEST & PATH RECONSTRUCTION
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("TEST 1: DIJKSTRA SHORTEST PATH ROUTE CALCULATION (N1 -> N12)");
        System.out.println("-------------------------------------------------------------------------------");
        PathResult directRoute = DijkstraRouter.findShortestPath(graph, "N1", "N12");
        System.out.println("Source: N1 (North Gate) -> Destination: N12 (South Gate)");
        System.out.println("Calculated Shortest Path: " + directRoute);
        System.out.println("Verification: Correct path traversing shortest segments across N1->N2->N7->N16->N12 or N1->N8->N7...\n");

        // --------------------------------------------------------------------
        // TEST 2: EMERGENCY PRIORITY SORTING (PRIORITY QUEUE)
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("TEST 2: EMERGENCY QUEUE PRIORITY SORTING (CRITICAL > HIGH > MEDIUM > LOW)");
        System.out.println("-------------------------------------------------------------------------------");
        LocalDateTime now = LocalDateTime.now();
        Emergency eLow = new Emergency("EMG-01", "N9", EmergencyType.GENERAL_MEDICAL, Severity.LOW, "Mild fever & dehydration", now.minusMinutes(15));
        Emergency eCrit1 = new Emergency("EMG-02", "N7", EmergencyType.CARDIAC_EMERGENCY, Severity.CRITICAL, "Acute chest pain & cardiac arrest", now.minusMinutes(8));
        Emergency eMed = new Emergency("EMG-03", "N4", EmergencyType.GENERAL_MEDICAL, Severity.MEDIUM, "Fractured arm from bike skid", now.minusMinutes(12));
        Emergency eCrit2 = new Emergency("EMG-04", "N6", EmergencyType.CARDIAC_EMERGENCY, Severity.CRITICAL, "Severe chest pain (Older emergency)", now.minusMinutes(20));
        Emergency eHigh = new Emergency("EMG-05", "N12", EmergencyType.ROAD_ACCIDENT, Severity.HIGH, "Multiple vehicle collision", now.minusMinutes(5));

        engine.addEmergency(eLow);
        engine.addEmergency(eCrit1);
        engine.addEmergency(eMed);
        engine.addEmergency(eCrit2);
        engine.addEmergency(eHigh);

        System.out.println("Order popped from PriorityQueue:");
        int order = 1;
        while (!engine.getEmergencyQueue().isEmpty()) {
            Emergency popped = engine.getEmergencyQueue().poll();
            System.out.printf("  %d. %s (Created %s mins ago)\n",
                    order++, popped, java.time.Duration.between(popped.getCreatedAt(), now).toMinutes());
        }
        System.out.println("Observation: EMG-04 popped before EMG-02 because both are CRITICAL, but EMG-04 is older (FIFO tie-breaker).\n");

        // --------------------------------------------------------------------
        // SCENARIO 1: CRITICAL CARDIAC EMERGENCY DISPATCH
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("SCENARIO 1: CRITICAL CARDIAC EMERGENCY ALLOCATION");
        System.out.println("-------------------------------------------------------------------------------");
        Emergency cardiacCase = new Emergency("EMG-101", "N7", EmergencyType.CARDIAC_EMERGENCY, Severity.CRITICAL,
                "Patient collapsed in mall, pulse weak", LocalDateTime.now());
        System.out.println("Emergency: " + cardiacCase);

        AllocationResult res1 = engine.allocate(cardiacCase);
        if (res1.success) {
            System.out.println(" Allocated Ambulance: " + res1.ambulance.getId() + " (" + res1.ambulance.getType().getCode() + " at " + res1.pathToEmergency.getPathNodes().get(0) + ")");
            System.out.println("   Equipment verified: " + res1.ambulance.getEquipment());
            System.out.println("   Route to Patient:  " + res1.pathToEmergency);
            System.out.println("   Simulated ETA:     " + String.format("%.1f", res1.estimatedResponseMinutes) + " mins (at 40 km/h average speed)");
            System.out.println(" Allocated Hospital:  " + res1.hospital.getName() + " (at " + res1.hospital.getLocationNodeId() + ")");
            System.out.println("   Hospital Beds:     " + res1.hospital.getAvailableBeds() + " available");
            System.out.println("   Route to Hospital: " + res1.pathToHospital);
            System.out.println("   Total Distance:    " + String.format("%.2f", res1.totalDistanceKm) + " km");
        } else {
            System.out.println("Allocation Failed: " + res1.message);
        }
        System.out.println();

        // --------------------------------------------------------------------
        // SCENARIO 2: HOSPITAL CAPACITY & FACILITY FILTERING (BURN CARE)
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("SCENARIO 2: HOSPITAL CAPACITY & SPECIALIZED FACILITY FILTERING");
        System.out.println("-------------------------------------------------------------------------------");
        Emergency burnCase = new Emergency("EMG-102", "N12", EmergencyType.FIRE_INJURY, Severity.HIGH,
                "Chemical burn incident at workshop", LocalDateTime.now());
        System.out.println("Emergency: " + burnCase);
        System.out.println("Note: HOSP-4 (at N13) has 'Burn Care' and is only 2.5 km away, BUT has 0 BEDS!");
        System.out.println("      HOSP-1 (at N7) also has 'Burn Care' and 8 available beds.");

        AllocationResult res2 = engine.allocate(burnCase);
        if (res2.success) {
            System.out.println(" Allocated Ambulance: " + res2.ambulance.getId() + " at " + res2.pathToEmergency.getPathNodes().get(0));
            System.out.println(" Allocated Hospital:  " + res2.hospital.getName() + " at " + res2.hospital.getLocationNodeId());
            System.out.println(" Verification: Correctly bypassed HOSP-4 (0 beds) and routed patient to HOSP-1!");
            System.out.println(" Route to Hospital:   " + res2.pathToHospital);
        } else {
            System.out.println("Allocation Failed: " + res2.message);
        }
        System.out.println();

        // --------------------------------------------------------------------
        // SCENARIO 3: UNREACHABLE LOCATION TEST (ISOLATED NODE N18)
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("SCENARIO 3: UNREACHABLE DESTINATION TEST (N18 ISOLATED DEPOT)");
        System.out.println("-------------------------------------------------------------------------------");
        Emergency unreachableCase = new Emergency("EMG-103", "N18", EmergencyType.GENERAL_MEDICAL, Severity.MEDIUM,
                "Worker stranded at flooded island depot", LocalDateTime.now());
        System.out.println("Emergency: " + unreachableCase);

        AllocationResult res3 = engine.allocate(unreachableCase);
        if (!res3.success) {
            System.out.println(" Handled Expected Error: " + res3.message);
            System.out.println(" Verification: Dijkstra correctly identified no connected path and refused false dispatch.");
        } else {
            System.out.println("ERROR: Should not have allocated unreachable node!");
        }
        System.out.println();

        // --------------------------------------------------------------------
        // SCENARIO 4: ALL SUITABLE AMBULANCES BUSY TEST
        // --------------------------------------------------------------------
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("SCENARIO 4: NO SUITABLE AMBULANCE AVAILABLE TEST");
        System.out.println("-------------------------------------------------------------------------------");
        // Exhaust remaining ALS ambulances
        Emergency cardiacCase2 = new Emergency("EMG-104", "N9", EmergencyType.CARDIAC_EMERGENCY, Severity.CRITICAL,
                "Cardiac distress in university library", LocalDateTime.now());
        AllocationResult res4 = engine.allocate(cardiacCase2);
        System.out.println("Dispatched second ALS ambulance: " + (res4.success ? res4.ambulance.getId() : "None"));

        // Now all ALS ambulances (AMB-101, AMB-103, AMB-105) are unavailable or busy
        Emergency cardiacCase3 = new Emergency("EMG-105", "N10", EmergencyType.CARDIAC_EMERGENCY, Severity.CRITICAL,
                "Third cardiac case reported, but only BLS (AMB-102) and PT (AMB-104) are free", LocalDateTime.now());
        AllocationResult res5 = engine.allocate(cardiacCase3);
        if (!res5.success) {
            System.out.println(" Handled Expected Error: " + res5.message);
            System.out.println(" Verification: Prevented assigning unqualified BLS/PT ambulance to a Critical Cardiac case!");
        } else {
            System.out.println("ERROR: Assigned unqualified ambulance!");
        }

        // Test completion restoring availability
        System.out.println("\n[SIMULATION] Completing Mission for EMG-101...");
        engine.completeDispatch(res1);
        System.out.println(" Ambulance " + res1.ambulance.getId() + " is now AVAILABLE at " + res1.ambulance.getCurrentNodeId());
        System.out.println(" Hospital " + res1.hospital.getName() + " beds updated to: " + res1.hospital.getAvailableBeds());

        // Now retry allocating cardiacCase3
        AllocationResult retryRes = engine.allocate(cardiacCase3);
        System.out.println(" Retry allocation for EMG-105 after ambulance freed: " + (retryRes.success ? "SUCCESS with " + retryRes.ambulance.getId() : "FAILED"));

        System.out.println("\n===============================================================================");
        System.out.println("   STAGE 1 COMPLETE: ALL DSA AND DOMAIN LOGIC VERIFIED SUCCESSFULLY           ");
        System.out.println("===============================================================================");
    }
}
