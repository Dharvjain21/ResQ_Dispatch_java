package com.resq.repository;

import com.resq.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, String> {

    List<Ambulance> findByAvailableTrue();

    List<Ambulance> findByAvailableFalse();

    long countByAvailableTrue();

    long countByAvailableFalse();
}
