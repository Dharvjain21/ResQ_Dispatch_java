-- ==============================================================================
-- RESQ DISPATCH: SAMPLE SEED DATA
-- Fictional academic simulation data for ResQ Dispatch System
-- ==============================================================================

USE resq_dispatch_db;

-- 1. Insert 18 Road Intersections (Vertices)
INSERT INTO road_nodes (id, name, latitude, longitude) VALUES
('N1', 'North Gate Interstation', 28.6325, 77.2185),
('N2', 'Metro Circle Terminal', 28.6300, 77.2250),
('N3', 'Tech Park Junction', 28.6250, 77.2320),
('N4', 'Riverside Boulevard', 28.6180, 77.2350),
('N5', 'East Bypass Outer', 28.6120, 77.2300),
('N6', 'Market Square Crossing', 28.6140, 77.2220),
('N7', 'Central Plaza Hub', 28.6200, 77.2180),
('N8', 'Civic Center Junction', 28.6260, 77.2120),
('N9', 'University Crossing', 28.6210, 77.2050),
('N10', 'West Expressway Toll', 28.6150, 77.2000),
('N11', 'Industrial Area Hub', 28.6080, 77.2060),
('N12', 'South Gate Interchange', 28.6050, 77.2150),
('N13', 'Harbor Link Crossing', 28.6030, 77.2240),
('N14', 'Greenfield Crossing', 28.6280, 77.2020),
('N15', 'Sports Arena Circle', 28.6350, 77.2100),
('N16', 'Downtown Terminus', 28.6170, 77.2120),
('N17', 'Suburb Crossing', 28.6380, 77.2250),
('N18', 'Isolated Island Depot (Unreachable)', 28.6450, 77.1900);

-- 2. Insert 28 Weighted Road Edges (Distances in km)
INSERT INTO road_edges (source_node_id, target_node_id, distance_km, is_bidirectional) VALUES
('N1', 'N2', 1.8, TRUE),
('N1', 'N8', 2.2, TRUE),
('N1', 'N15', 1.5, TRUE),
('N2', 'N3', 2.5, TRUE),
('N2', 'N7', 2.1, TRUE),
('N2', 'N17', 2.0, TRUE),
('N3', 'N4', 2.4, TRUE),
('N4', 'N5', 2.0, TRUE),
('N4', 'N6', 2.6, TRUE),
('N5', 'N6', 2.1, TRUE),
('N5', 'N13', 2.8, TRUE),
('N6', 'N7', 1.6, TRUE),
('N6', 'N12', 2.3, TRUE),
('N7', 'N8', 1.9, TRUE),
('N7', 'N16', 1.2, TRUE),
('N7', 'N9', 2.5, TRUE),
('N8', 'N9', 2.0, TRUE),
('N8', 'N14', 1.7, TRUE),
('N9', 'N10', 1.8, TRUE),
('N9', 'N16', 1.5, TRUE),
('N10', 'N11', 2.2, TRUE),
('N10', 'N16', 2.4, TRUE),
('N11', 'N12', 2.1, TRUE),
('N11', 'N16', 2.7, TRUE),
('N12', 'N13', 2.5, TRUE),
('N14', 'N15', 2.3, TRUE),
('N14', 'N9', 2.4, TRUE),
('N16', 'N12', 2.2, TRUE);

-- 3. Insert Ambulances
INSERT INTO ambulances (id, vehicle_code, current_node_id, ambulance_type, available, equipment, status, current_assignment) VALUES
('AMB-101', 'DL-01-EQ-9001', 'N1', 'ADVANCED_LIFE_SUPPORT', TRUE, 'Ventilator, Defibrillator, Cardiac Monitor, Oxygen, Infusion Pump', 'AVAILABLE', NULL),
('AMB-102', 'DL-01-EQ-9002', 'N8', 'BASIC_LIFE_SUPPORT', TRUE, 'First Aid Kit, Stretcher, Oxygen, Splints', 'AVAILABLE', NULL),
('AMB-103', 'DL-01-EQ-9003', 'N11', 'ADVANCED_LIFE_SUPPORT', TRUE, 'Ventilator, Defibrillator, Cardiac Monitor, Oxygen, Trauma Kit', 'AVAILABLE', NULL),
('AMB-104', 'DL-01-EQ-9004', 'N4', 'PATIENT_TRANSPORT', TRUE, 'Wheelchair, Stretcher, First Aid', 'AVAILABLE', NULL),
('AMB-105', 'DL-01-EQ-9005', 'N6', 'ADVANCED_LIFE_SUPPORT', FALSE, 'Ventilator, Defibrillator, Cardiac Monitor, Oxygen', 'BUSY', 'DISPATCH-HIST-01'),
('AMB-106', 'DL-01-EQ-9006', 'N14', 'BASIC_LIFE_SUPPORT', FALSE, 'First Aid, Oxygen, Splints', 'MAINTENANCE', NULL);

-- 4. Insert Hospitals
INSERT INTO hospitals (id, name, location_node_id, available_beds, total_beds, facilities, capacity_status) VALUES
('HOSP-1', 'Apex Metro Trauma & Heart Center', 'N7', 8, 20, 'General Emergency, Cardiac Care, Trauma Care, ICU, Burn Care', 'NORMAL'),
('HOSP-2', 'St. Jude Memorial Hospital', 'N3', 2, 15, 'General Emergency, Cardiac Care, ICU', 'NEAR_CAPACITY'),
('HOSP-3', 'City Children & General Clinic', 'N10', 12, 25, 'General Emergency', 'NORMAL'),
('HOSP-4', 'Phoenix Specialized Burn Institute', 'N13', 0, 10, 'General Emergency, Burn Care, ICU', 'FULL');

-- 5. Insert Emergencies
INSERT INTO emergencies (id, reference_code, location_node_id, emergency_type, severity, description, status, created_at) VALUES
(1, 'EMG-801', 'N7', 'Cardiac Emergency', 'CRITICAL', '62-year-old patient collapsed in shopping mall with chest pain and shortness of breath', 'PENDING', NOW() - INTERVAL 12 MINUTE),
(2, 'EMG-802', 'N12', 'Road Accident', 'HIGH', 'Multi-car collision on flyover, two passengers trapped with trauma injuries', 'PENDING', NOW() - INTERVAL 8 MINUTE),
(3, 'EMG-803', 'N6', 'Fire Injury', 'CRITICAL', 'Kitchen cylinder flare up, victim sustained second-degree burns', 'PENDING', NOW() - INTERVAL 15 MINUTE),
(4, 'EMG-804', 'N4', 'General Medical Emergency', 'MEDIUM', 'High fever and sudden acute dehydration in elderly resident', 'PENDING', NOW() - INTERVAL 22 MINUTE),
(5, 'EMG-805', 'N9', 'General Medical Emergency', 'LOW', 'Sprained ankle from sports activity requiring transfer for X-ray', 'PENDING', NOW() - INTERVAL 35 MINUTE);
