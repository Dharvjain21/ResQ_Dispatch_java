package com.resq.repository;

import com.resq.model.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Optional<Dispatch> findByEmergencyId(Long emergencyId);

    List<Dispatch> findByStatus(String status);

    List<Dispatch> findByStatusNot(String status);

    long countByStatus(String status);

    @Query("SELECT AVG(d.estimatedResponseMinutes) FROM Dispatch d WHERE d.status = 'COMPLETED'")
    Double calculateAverageResponseTime();
}
