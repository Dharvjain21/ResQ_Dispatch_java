package com.resq.repository;

import com.resq.model.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    Optional<Emergency> findByReferenceCode(String referenceCode);

    List<Emergency> findByStatus(String status);

    @Query("SELECT e FROM Emergency e WHERE e.status = 'PENDING' ORDER BY " +
           "CASE e.severity " +
           "  WHEN 'CRITICAL' THEN 1 " +
           "  WHEN 'HIGH' THEN 2 " +
           "  WHEN 'MEDIUM' THEN 3 " +
           "  WHEN 'LOW' THEN 4 " +
           "  ELSE 5 END ASC, e.createdAt ASC")
    List<Emergency> findPendingEmergenciesSortedByPriority();

    long countByStatus(String status);

    long countBySeverity(String severity);
}
