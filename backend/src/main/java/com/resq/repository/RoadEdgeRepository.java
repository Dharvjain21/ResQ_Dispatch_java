package com.resq.repository;

import com.resq.model.RoadEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadEdgeRepository extends JpaRepository<RoadEdge, Long> {
    List<RoadEdge> findBySourceNodeId(String sourceNodeId);
}
