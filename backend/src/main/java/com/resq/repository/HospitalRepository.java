package com.resq.repository;

import com.resq.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, String> {

    List<Hospital> findByAvailableBedsGreaterThan(Integer minBeds);
}
