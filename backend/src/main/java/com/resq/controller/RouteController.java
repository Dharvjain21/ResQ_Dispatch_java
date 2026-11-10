package com.resq.controller;

import com.resq.algorithm.PathResult;
import com.resq.dto.ApiResponse;
import com.resq.dto.RoadNetworkResponse;
import com.resq.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/network")
    public ResponseEntity<ApiResponse<RoadNetworkResponse>> getRoadNetwork() {
        RoadNetworkResponse network = routeService.getFullRoadNetwork();
        return ResponseEntity.ok(ApiResponse.ok("Full road network graph topology retrieved.", network));
    }

    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<PathResult>> calculateRoute(
            @RequestParam String source,
            @RequestParam String destination) {
        PathResult result = routeService.calculateShortestRoute(source, destination);
        return ResponseEntity.ok(ApiResponse.ok("Dijkstra calculation complete.", result));
    }
}
