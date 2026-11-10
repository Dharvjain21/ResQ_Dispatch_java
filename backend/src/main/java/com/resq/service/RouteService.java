package com.resq.service;

import com.resq.algorithm.Dijkstra;
import com.resq.algorithm.Graph;
import com.resq.algorithm.PathResult;
import com.resq.dto.RoadNetworkResponse;
import com.resq.exception.ResourceNotFoundException;
import com.resq.model.RoadEdge;
import com.resq.model.RoadNode;
import com.resq.repository.RoadEdgeRepository;
import com.resq.repository.RoadNodeRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RoadNodeRepository nodeRepository;
    private final RoadEdgeRepository edgeRepository;
    private Graph graph = new Graph();

    public RouteService(RoadNodeRepository nodeRepository, RoadEdgeRepository edgeRepository) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    @PostConstruct
    public synchronized void reloadGraph() {
        Graph newGraph = new Graph();
        List<RoadNode> nodes = nodeRepository.findAll();
        for (RoadNode node : nodes) {
            newGraph.addNode(node.getId(), node.getName());
        }

        List<RoadEdge> edges = edgeRepository.findAll();
        for (RoadEdge edge : edges) {
            if (Boolean.TRUE.equals(edge.getBidirectional())) {
                newGraph.addUndirectedEdge(edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getDistanceKm());
            } else {
                newGraph.addDirectedEdge(edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getDistanceKm());
            }
        }

        this.graph = newGraph;
        log.info("Road network graph reloaded: {} nodes, {} edges registered.",
                newGraph.getNodeCount(), edges.size());
    }

    public PathResult calculateShortestRoute(String sourceNodeId, String destinationNodeId) {
        if (!graph.containsNode(sourceNodeId)) {
            throw new ResourceNotFoundException("Source node '" + sourceNodeId + "' does not exist in road network");
        }
        if (!graph.containsNode(destinationNodeId)) {
            throw new ResourceNotFoundException("Destination node '" + destinationNodeId + "' does not exist in road network");
        }
        return Dijkstra.findShortestPath(graph, sourceNodeId, destinationNodeId);
    }

    public RoadNetworkResponse getFullRoadNetwork() {
        return new RoadNetworkResponse(nodeRepository.findAll(), edgeRepository.findAll());
    }

    public Graph getGraph() {
        return graph;
    }
}
