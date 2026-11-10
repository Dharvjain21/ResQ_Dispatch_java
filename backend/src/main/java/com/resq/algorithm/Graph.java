package com.resq.algorithm;

import java.util.*;

/**
 * Graph representation using Adjacency Lists.
 * Space Complexity: O(V + E)
 */
public class Graph {
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();
    private final Map<String, String> nodeLabels = new HashMap<>();

    public void addNode(String nodeId, String label) {
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
        nodeLabels.put(nodeId, label != null ? label : nodeId);
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

    public void clear() {
        adjacencyList.clear();
        nodeLabels.clear();
    }
}
