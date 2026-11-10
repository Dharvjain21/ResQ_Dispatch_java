package com.resq.config;

import com.resq.model.*;
import com.resq.repository.*;
import com.resq.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoadNodeRepository roadNodeRepository;
    private final RoadEdgeRepository roadEdgeRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final EmergencyRepository emergencyRepository;
    private final RouteService routeService;

    public DataInitializer(RoadNodeRepository roadNodeRepository,
                           RoadEdgeRepository roadEdgeRepository,
                           AmbulanceRepository ambulanceRepository,
                           HospitalRepository hospitalRepository,
                           EmergencyRepository emergencyRepository,
                           RouteService routeService) {
        this.roadNodeRepository = roadNodeRepository;
        this.roadEdgeRepository = roadEdgeRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.emergencyRepository = emergencyRepository;
        this.routeService = routeService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roadNodeRepository.count() == 0) {
            log.info("Populating initial Road Network nodes...");
            seedRoadNodes();
            seedRoadEdges();
        }

        routeService.reloadGraph();

        if (ambulanceRepository.count() == 0) {
            log.info("Populating Ambulance Fleet...");
            seedAmbulances();
        }

        if (hospitalRepository.count() == 0) {
            log.info("Populating Hospitals...");
            seedHospitals();
        }

        if (emergencyRepository.count() == 0) {
            log.info("Populating initial Emergency Queue...");
            seedEmergencies();
        }

        log.info("ResQ Dispatch Database Initialized Successfully!");
    }

    private void seedRoadNodes() {
        roadNodeRepository.saveAll(List.of(
                new RoadNode("N1", "North Gate Interstation", 28.6325, 77.2185),
                new RoadNode("N2", "Metro Circle Terminal", 28.6300, 77.2250),
                new RoadNode("N3", "Tech Park Junction", 28.6250, 77.2320),
                new RoadNode("N4", "Riverside Boulevard", 28.6180, 77.2350),
                new RoadNode("N5", "East Bypass Outer", 28.6120, 77.2300),
                new RoadNode("N6", "Market Square Crossing", 28.6140, 77.2220),
                new RoadNode("N7", "Central Plaza Hub", 28.6200, 77.2180),
                new RoadNode("N8", "Civic Center Junction", 28.6260, 77.2120),
                new RoadNode("N9", "University Crossing", 28.6210, 77.2050),
                new RoadNode("N10", "West Expressway Toll", 28.6150, 77.2000),
                new RoadNode("N11", "Industrial Area Hub", 28.6080, 77.2060),
                new RoadNode("N12", "South Gate Interchange", 28.6050, 77.2150),
                new RoadNode("N13", "Harbor Link Crossing", 28.6030, 77.2240),
                new RoadNode("N14", "Greenfield Crossing", 28.6280, 77.2020),
                new RoadNode("N15", "Sports Arena Circle", 28.6350, 77.2100),
                new RoadNode("N16", "Downtown Terminus", 28.6170, 77.2120),
                new RoadNode("N17", "Suburb Crossing", 28.6380, 77.2250),
                new RoadNode("N18", "Isolated Island Depot (Unreachable)", 28.6450, 77.1900)
        ));
    }

    private void seedRoadEdges() {
        roadEdgeRepository.saveAll(List.of(
                new RoadEdge("N1", "N2", 1.8, true),
                new RoadEdge("N1", "N8", 2.2, true),
                new RoadEdge("N1", "N15", 1.5, true),
                new RoadEdge("N2", "N3", 2.5, true),
                new RoadEdge("N2", "N7", 2.1, true),
                new RoadEdge("N2", "N17", 2.0, true),
                new RoadEdge("N3", "N4", 2.4, true),
                new RoadEdge("N4", "N5", 2.0, true),
                new RoadEdge("N4", "N6", 2.6, true),
                new RoadEdge("N5", "N6", 2.1, true),
                new RoadEdge("N5", "N13", 2.8, true),
                new RoadEdge("N6", "N7", 1.6, true),
                new RoadEdge("N6", "N12", 2.3, true),
                new RoadEdge("N7", "N8", 1.9, true),
                new RoadEdge("N7", "N16", 1.2, true),
                new RoadEdge("N7", "N9", 2.5, true),
                new RoadEdge("N8", "N9", 2.0, true),
                new RoadEdge("N8", "N14", 1.7, true),
                new RoadEdge("N9", "N10", 1.8, true),
                new RoadEdge("N9", "N16", 1.5, true),
                new RoadEdge("N10", "N11", 2.2, true),
                new RoadEdge("N10", "N16", 2.4, true),
                new RoadEdge("N11", "N12", 2.1, true),
                new RoadEdge("N11", "N16", 2.7, true),
                new RoadEdge("N12", "N13", 2.5, true),
                new RoadEdge("N14", "N15", 2.3, true),
                new RoadEdge("N14", "N9", 2.4, true),
                new RoadEdge("N16", "N12", 2.2, true)
        ));
    }

    private void seedAmbulances() {
        ambulanceRepository.saveAll(List.of(
                new Ambulance("AMB-101", "DL-01-EQ-9001", "N1", "ADVANCED_LIFE_SUPPORT",
                        true, "Ventilator, Defibrillator, Cardiac Monitor, Oxygen, Infusion Pump", "AVAILABLE"),

                new Ambulance("AMB-102", "DL-01-EQ-9002", "N8", "BASIC_LIFE_SUPPORT",
                        true, "First Aid Kit, Stretcher, Oxygen, Splints", "AVAILABLE"),

                new Ambulance("AMB-103", "DL-01-EQ-9003", "N11", "ADVANCED_LIFE_SUPPORT",
                        true, "Ventilator, Defibrillator, Cardiac Monitor, Oxygen, Trauma Kit", "AVAILABLE"),

                new Ambulance("AMB-104", "DL-01-EQ-9004", "N4", "PATIENT_TRANSPORT",
                        true, "Wheelchair, Stretcher, First Aid", "AVAILABLE"),

                new Ambulance("AMB-105", "DL-01-EQ-9005", "N6", "ADVANCED_LIFE_SUPPORT",
                        false, "Ventilator, Defibrillator, Cardiac Monitor, Oxygen", "BUSY"),

                new Ambulance("AMB-106", "DL-01-EQ-9006", "N14", "BASIC_LIFE_SUPPORT",
                        false, "First Aid, Oxygen, Splints", "MAINTENANCE")
        ));
    }

    private void seedHospitals() {
        hospitalRepository.saveAll(List.of(
                new Hospital("HOSP-1", "Apex Metro Trauma & Heart Center", "N7",
                        8, 20, "General Emergency, Cardiac Care, Trauma Care, ICU, Burn Care", "NORMAL"),

                new Hospital("HOSP-2", "St. Jude Memorial Hospital", "N3",
                        2, 15, "General Emergency, Cardiac Care, ICU", "NEAR_CAPACITY"),

                new Hospital("HOSP-3", "City Children & General Clinic", "N10",
                        12, 25, "General Emergency", "NORMAL"),

                new Hospital("HOSP-4", "Phoenix Specialized Burn Institute", "N13",
                        0, 10, "General Emergency, Burn Care, ICU", "FULL")
        ));
    }

    private void seedEmergencies() {
        LocalDateTime now = LocalDateTime.now();
        emergencyRepository.saveAll(List.of(
                new Emergency("EMG-801", "N7", "Cardiac Emergency", "CRITICAL",
                        "62-year-old patient collapsed in shopping mall with chest pain and shortness of breath", "PENDING", now.minusMinutes(12)),

                new Emergency("EMG-802", "N12", "Road Accident", "HIGH",
                        "Multi-car collision on flyover, two passengers trapped with trauma injuries", "PENDING", now.minusMinutes(8)),

                new Emergency("EMG-803", "N6", "Fire Injury", "CRITICAL",
                        "Kitchen cylinder flare up, victim sustained second-degree burns", "PENDING", now.minusMinutes(15)),

                new Emergency("EMG-804", "N4", "General Medical Emergency", "MEDIUM",
                        "High fever and sudden acute dehydration in elderly resident", "PENDING", now.minusMinutes(22)),

                new Emergency("EMG-805", "N9", "General Medical Emergency", "LOW",
                        "Sprained ankle from sports activity requiring transfer for X-ray", "PENDING", now.minusMinutes(35))
        ));
    }
}
