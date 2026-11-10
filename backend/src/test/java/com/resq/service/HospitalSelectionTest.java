package com.resq.service;

import com.resq.model.Hospital;
import com.resq.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class HospitalSelectionTest {

    private HospitalService hospitalService;

    @BeforeEach
    void setUp() {
        HospitalRepository repo = Mockito.mock(HospitalRepository.class);
        hospitalService = new HospitalService(repo);
    }

    @Test
    @DisplayName("Hospital with 0 available beds is rejected despite having the required facility")
    void testHospitalWithZeroBedsRejected() {
        Hospital fullBurnHospital = new Hospital("HOSP-4", "Phoenix Burn Institute", "N13",
                0, 10, "General Emergency, Burn Care, ICU", "FULL");

        assertFalse(fullBurnHospital.isEligibleFor("Burn Care"));
    }

    @Test
    @DisplayName("Hospital with available beds and matching facility is eligible")
    void testEligibleHospitalAccepted() {
        Hospital apexCenter = new Hospital("HOSP-1", "Apex Metro Trauma & Heart Center", "N7",
                8, 20, "General Emergency, Cardiac Care, Trauma Care, ICU, Burn Care", "NORMAL");

        assertTrue(apexCenter.isEligibleFor("Cardiac Care"));
        assertTrue(apexCenter.isEligibleFor("Burn Care"));
        assertTrue(apexCenter.isEligibleFor("Trauma Care"));
        assertFalse(apexCenter.isEligibleFor("Hyperbaric Oncology"));
    }
}
