package com.resq.algorithm;

import java.util.*;

/**
 * Dijkstra's Single-Source Shortest Path Algorithm Implementation.
 * 
 * Algorithm Characteristics:
 * - Graph: Weighted graph with non-negative edge weights (distances in km).
 * - Priority Queue (Min-Heap): Selects the unvisited vertex with the minimum tentative distance.
 * - Adjacency List: Efficient neighbor iteration.
 * - Predecessor Map: Allows linear-time backward path reconstruction from target to source.
 * 
 * Complexity Analysis:
 * - Time Complexity: O((V + E) log V) where V = number of intersections and E = number of roads.
 *   - Extract-min is called at most V times: O(V log V)
 *   - Edge relaxations occur at most E times, each inserting into heap: O(E log V)
 * - Space Complexity: O(V) for the distance table, predecessor map, and min-heap.
 */
public class Dijkstra {

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

    /**
     * Calculates the shortest path and total distance between source and destination.
     *
     * @param graph Road network graph
     * @param source Starting intersection ID
     * @param destination Target intersection ID
     * @return PathResult containing reachability status, ordered node list, and total km
     */
    public static PathResult findShortestPath(Graph graph, String source, String destination) {
        if (graph == null) {
            return new PathResult("Graph cannot be null");
        }
        if (source == null || destination == null) {
            return new PathResult("Source and destination must be non-null");
        }
        if (!graph.containsNode(source)) {
            return new PathResult("Source node '" + source + "' not found in road network");
        }
        if (!graph.containsNode(destination)) {
            return new PathResult("Destination node '" + destination + "' not found in road network");
        }

        // Base case: Start is already at destination
        if (source.equalsIgnoreCase(destination)) {
            return new PathResult(0.0, Collections.singletonList(source));
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();

        // Initialize distances to infinity
        for (String node : graph.getAllNodeIds()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }

        dist.put(source, 0.0);
        pq.offer(new NodeDistance(source, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.nodeId;

            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            // Target reached early termination
            if (u.equalsIgnoreCase(destination)) {
                break;
            }

            // Skip obsolete relaxed heap entries
            if (current.distance > dist.get(u)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(u)) {
                String v = edge.getTargetNodeId();
                if (visited.contains(v)) {
                    continue;
                }

                double weight = edge.getDistanceKm();
                double newDist = dist.get(u) + weight;

                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.offer(new NodeDistance(v, newDist));
                }
            }
        }

        double finalDistance = dist.get(destination);
        if (finalDistance == Double.POSITIVE_INFINITY) {
            return new PathResult("No reachable road route between " + source + " and " + destination);
        }

        // Path reconstruction: Backtrack from destination to source using predecessors
        LinkedList<String> path = new LinkedList<>();
        String step = destination;
        while (step != null) {
            path.addFirst(step);
            step = prev.get(step);
        }

        return new PathResult(finalDistance, path);
    }
}
