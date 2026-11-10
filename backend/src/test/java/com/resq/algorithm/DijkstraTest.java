package com.resq.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DijkstraTest {

    private Graph testGraph;

    @BeforeEach
    void setUp() {
        testGraph = new Graph();
        // A -> B = 4 km, A -> C = 2 km, C -> B = 1 km, B -> D = 3 km, C -> D = 5 km
        // Expected shortest path A -> D: A -> C -> B -> D = 2 + 1 + 3 = 6 km
        testGraph.addNode("A", "Node A");
        testGraph.addNode("B", "Node B");
        testGraph.addNode("C", "Node C");
        testGraph.addNode("D", "Node D");
        testGraph.addNode("ISOLATED", "Isolated Node");

        testGraph.addUndirectedEdge("A", "B", 4.0);
        testGraph.addUndirectedEdge("A", "C", 2.0);
        testGraph.addUndirectedEdge("C", "B", 1.0);
        testGraph.addUndirectedEdge("B", "D", 3.0);
        testGraph.addUndirectedEdge("C", "D", 5.0);
    }

    @Test
    @DisplayName("Should find correct shortest path dynamically (A -> C -> B -> D = 6.0 km)")
    void testStandardShortestPath() {
        PathResult result = Dijkstra.findShortestPath(testGraph, "A", "D");
        assertTrue(result.isReachable());
        assertEquals(6.0, result.getTotalDistanceKm(), 0.001);
        assertEquals(List.of("A", "C", "B", "D"), result.getPathNodes());
    }

    @Test
    @DisplayName("Should return 0 distance and single node path when source equals destination")
    void testSourceEqualsDestination() {
        PathResult result = Dijkstra.findShortestPath(testGraph, "A", "A");
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalDistanceKm(), 0.001);
        assertEquals(List.of("A"), result.getPathNodes());
    }

    @Test
    @DisplayName("Should return unreachable when no path exists to destination")
    void testUnreachableNode() {
        PathResult result = Dijkstra.findShortestPath(testGraph, "A", "ISOLATED");
        assertFalse(result.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, result.getTotalDistanceKm());
        assertTrue(result.getPathNodes().isEmpty());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should gracefully handle non-existent node IDs")
    void testNonExistentNode() {
        PathResult result = Dijkstra.findShortestPath(testGraph, "A", "DOES_NOT_EXIST");
        assertFalse(result.isReachable());
        assertTrue(result.getErrorMessage().contains("not found in road network"));
    }
}
