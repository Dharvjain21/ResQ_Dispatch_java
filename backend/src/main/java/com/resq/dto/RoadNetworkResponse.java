package com.resq.dto;

import com.resq.model.RoadEdge;
import com.resq.model.RoadNode;
import java.util.List;

public class RoadNetworkResponse {
    private List<RoadNode> nodes;
    private List<RoadEdge> edges;

    public RoadNetworkResponse() {}

    public RoadNetworkResponse(List<RoadNode> nodes, List<RoadEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<RoadNode> getNodes() { return nodes; }
    public void setNodes(List<RoadNode> nodes) { this.nodes = nodes; }

    public List<RoadEdge> getEdges() { return edges; }
    public void setEdges(List<RoadEdge> edges) { this.edges = edges; }
}
