-- ==============================================================================
-- RESQ DISPATCH: DATABASE SCHEMA DEFINITION (MySQL 8.x)
-- Database: resq_dispatch_db
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS resq_dispatch_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE resq_dispatch_db;

-- ------------------------------------------------------------------------------
-- Table 1: road_nodes (Intersections / Geographical Vertices)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS status_history;
DROP TABLE IF EXISTS dispatches;
DROP TABLE IF EXISTS emergencies;
DROP TABLE IF EXISTS ambulances;
DROP TABLE IF EXISTS hospitals;
DROP TABLE IF EXISTS road_edges;
DROP TABLE IF EXISTS road_nodes;

CREATE TABLE road_nodes (
    id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 2: road_edges (Weighted Roads / Graph Edges)
-- ------------------------------------------------------------------------------
CREATE TABLE road_edges (
    id BIGINT AUTO_INCREMENT NOT NULL,
    source_node_id VARCHAR(32) NOT NULL,
    target_node_id VARCHAR(32) NOT NULL,
    distance_km DOUBLE NOT NULL,
    is_bidirectional BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    INDEX idx_edge_source (source_node_id),
    INDEX idx_edge_target (target_node_id),
    CONSTRAINT fk_edge_source FOREIGN KEY (source_node_id) REFERENCES road_nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_target FOREIGN KEY (target_node_id) REFERENCES road_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 3: ambulances (Fleet Vehicles & Equipment)
-- ------------------------------------------------------------------------------
CREATE TABLE ambulances (
    id VARCHAR(32) NOT NULL,
    vehicle_code VARCHAR(32) NOT NULL UNIQUE,
    current_node_id VARCHAR(32) NOT NULL,
    ambulance_type VARCHAR(64) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    equipment VARCHAR(512),
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    current_assignment VARCHAR(64),
    PRIMARY KEY (id),
    INDEX idx_amb_avail (available),
    INDEX idx_amb_status (status),
    CONSTRAINT fk_amb_node FOREIGN KEY (current_node_id) REFERENCES road_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 4: hospitals (Medical Centers & Bed Capacities)
-- ------------------------------------------------------------------------------
CREATE TABLE hospitals (
    id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    location_node_id VARCHAR(32) NOT NULL,
    available_beds INT NOT NULL DEFAULT 0,
    total_beds INT NOT NULL DEFAULT 0,
    facilities VARCHAR(512),
    capacity_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    PRIMARY KEY (id),
    INDEX idx_hosp_node (location_node_id),
    CONSTRAINT fk_hosp_node FOREIGN KEY (location_node_id) REFERENCES road_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 5: emergencies (Incidents Reported & Priority Triaged)
-- ------------------------------------------------------------------------------
CREATE TABLE emergencies (
    id BIGINT AUTO_INCREMENT NOT NULL,
    reference_code VARCHAR(32) NOT NULL UNIQUE,
    location_node_id VARCHAR(32) NOT NULL,
    emergency_type VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL, -- CRITICAL, HIGH, MEDIUM, LOW
    description VARCHAR(512),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, ASSIGNED, RESOLVED, CANCELLED
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_emg_status (status),
    INDEX idx_emg_severity (severity),
    INDEX idx_emg_created (created_at),
    CONSTRAINT fk_emg_node FOREIGN KEY (location_node_id) REFERENCES road_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 6: dispatches (Allocated Missions with Dijkstra Routes)
-- ------------------------------------------------------------------------------
CREATE TABLE dispatches (
    id BIGINT AUTO_INCREMENT NOT NULL,
    emergency_id BIGINT NOT NULL,
    ambulance_id VARCHAR(32) NOT NULL,
    hospital_id VARCHAR(32) NOT NULL,
    route_emergency_json TEXT, -- JSON Array of Node IDs (e.g. ["N1","N2","N7"])
    route_hospital_json TEXT,  -- JSON Array of Node IDs
    total_distance_km DOUBLE NOT NULL,
    estimated_response_minutes DOUBLE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'AMBULANCE_ASSIGNED',
    assigned_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    PRIMARY KEY (id),
    INDEX idx_disp_status (status),
    INDEX idx_disp_emg (emergency_id),
    INDEX idx_disp_amb (ambulance_id),
    CONSTRAINT fk_disp_emg FOREIGN KEY (emergency_id) REFERENCES emergencies(id) ON DELETE CASCADE,
    CONSTRAINT fk_disp_amb FOREIGN KEY (ambulance_id) REFERENCES ambulances(id),
    CONSTRAINT fk_disp_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- Table 7: status_history (Audit Trail for GPS & Status Simulation)
-- ------------------------------------------------------------------------------
CREATE TABLE status_history (
    id BIGINT AUTO_INCREMENT NOT NULL,
    dispatch_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    timestamp DATETIME NOT NULL,
    note VARCHAR(256),
    PRIMARY KEY (id),
    INDEX idx_history_disp (dispatch_id),
    CONSTRAINT fk_history_disp FOREIGN KEY (dispatch_id) REFERENCES dispatches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
