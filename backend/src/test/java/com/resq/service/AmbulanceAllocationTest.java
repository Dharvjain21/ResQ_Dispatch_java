package com.resq.service;

import com.resq.model.Ambulance;
import com.resq.model.Emergency;
import com.resq.repository.AmbulanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AmbulanceAllocationTest {

    private AmbulanceService ambulanceService;

    @BeforeEach
    void setUp() {
        AmbulanceRepository repo = Mockito.mock(AmbulanceRepository.class);
        ambulanceService = new AmbulanceService(repo);
    }

    @Test
    @DisplayName("ALS ambulance with Defibrillator is suitable for Cardiac Emergency")
    void testCardiacAmbulanceSuitability() {
        Emergency cardiac = new Emergency("EMG-C1", "N7", "Cardiac Emergency", "CRITICAL",
                "Cardiac arrest", "PENDING", LocalDateTime.now());

        Ambulance als = new Ambulance("AMB-101", "DL-01-EQ-9001", "N1", "ADVANCED_LIFE_SUPPORT",
                true, "Ventilator, Defibrillator, Cardiac Monitor, Oxygen", "AVAILABLE");

        Ambulance bls = new Ambulance("AMB-102", "DL-01-EQ-9002", "N8", "BASIC_LIFE_SUPPORT",
                true, "First Aid Kit, Stretcher, Oxygen", "AVAILABLE");

        assertTrue(ambulanceService.isAmbulanceSuitable(als, cardiac));
        assertFalse(ambulanceService.isAmbulanceSuitable(bls, cardiac));
    }

    @Test
    @DisplayName("Busy ambulance is strictly ignored even if equipment matches")
    void testBusyAmbulanceIgnored() {
        Emergency cardiac = new Emergency("EMG-C2", "N7", "Cardiac Emergency", "CRITICAL",
                "Cardiac arrest", "PENDING", LocalDateTime.now());

        Ambulance busyAls = new Ambulance("AMB-105", "DL-01-EQ-9005", "N7", "ADVANCED_LIFE_SUPPORT",
                false, "Ventilator, Defibrillator, Cardiac Monitor, Oxygen", "BUSY");

        assertFalse(ambulanceService.isAmbulanceSuitable(busyAls, cardiac));
    }
}
