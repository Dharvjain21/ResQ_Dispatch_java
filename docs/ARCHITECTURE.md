# System Architecture & Technical Specifications
## Smart Ambulance Dispatch & Emergency Management System (ResQ Dispatch)

---

### 1. High-Level System Architecture

ResQ Dispatch is engineered following a clean, enterprise-grade layered architecture:

```mermaid
graph TD
    subgraph ClientLayer["Frontend Client (Web Browser)"]
        UI["Command Center Dashboard (HTML5 / CSS3 / Vanilla JS)"]
        LeafletMap["Interactive Leaflet.js GPS Simulation"]
    end

    subgraph ControllerLayer["Spring Boot REST API Layer"]
        EC["EmergencyController (/api/emergencies)"]
        AC["AmbulanceController (/api/ambulances)"]
        HC["HospitalController (/api/hospitals)"]
        DC["DispatchController (/api/dispatches)"]
        RC["RouteController (/api/routes)"]
        RepC["ReportController (/api/reports)"]
        GEH["GlobalExceptionHandler (@RestControllerAdvice)"]
    end

    subgraph ServiceLayer["Business Logic & Algorithm Layer"]
        ES["EmergencyService (Priority Queue Triage)"]
        DS["DispatchService (Transactional Allocation Engine)"]
        AmbS["AmbulanceService (Fleet & Equipment Matching)"]
        HospS["HospitalService (Facility & Bed Capacity Check)"]
        RS["RouteService (Graph Cache & Query)"]
        RepS["ReportService (Telemetry & KPI Aggregator)"]
        DijkstraEngine["Dijkstra Shortest Path Engine (O((V+E)log V))"]
    end

    subgraph DataAccessLayer["Persistence Layer (Spring Data JPA)"]
        ERepo["EmergencyRepository"]
        ARepo["AmbulanceRepository"]
        HRepo["HospitalRepository"]
        DRepo["DispatchRepository"]
        SHRepo["StatusHistoryRepository"]
        RNRepo["RoadNodeRepository"]
        RERepo["RoadEdgeRepository"]
    end

    subgraph DatabaseLayer["Relational Database"]
        DB[("MySQL 8.x / H2 In-Memory DB")]
    end

    UI -->|REST / JSON (Fetch)| ControllerLayer
    ControllerLayer --> ServiceLayer
    DS --> DijkstraEngine
    RS --> DijkstraEngine
    ServiceLayer --> DataAccessLayer
    DataAccessLayer --> DatabaseLayer
```

---

### 2. End-to-End Workflow Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Dispatcher as Emergency Dispatcher
    participant FE as Frontend Dashboard (Leaflet / JS)
    participant Ctrl as DispatchController / EmergencyController
    participant DS as DispatchService
    participant Dijk as Dijkstra Algorithm Engine
    participant Repo as JPA Repositories
    participant DB as MySQL / H2 Database

    Dispatcher->>FE: Reports Emergency (Type, Severity, Node N7)
    FE->>Ctrl: POST /api/emergencies (JSON Payload)
    Ctrl->>Repo: save(Emergency [PENDING, CRITICAL])
    Repo->>DB: INSERT into emergencies
    DB-->>Repo: Saved entity with ID 101
    Repo-->>Ctrl: Emergency Entity
    Ctrl-->>FE: 201 Created (Emergency Queued)

    Dispatcher->>FE: Clicks "Allocate Ambulance"
    FE->>Ctrl: POST /api/dispatches/101/allocate
    Ctrl->>DS: allocateEmergency(101) [Transactional]
    DS->>Repo: findByAvailableTrue()
    Repo-->>DS: List of available Ambulances (AMB-101, AMB-102, AMB-103)
    
    loop For each candidate ambulance
        DS->>DS: Check clinical equipment suitability
        DS->>Dijk: findShortestPath(ambNode, emergencyNode)
        Dijk-->>DS: PathResult (Nodes, Distance in km)
    end
    DS->>DS: Select nearest reachable ambulance (AMB-101 at N1, 3.9 km)

    loop For each hospital with beds > 0
        DS->>DS: Check required clinical facility (e.g. Cardiac Care)
        DS->>Dijk: findShortestPath(emergencyNode, hospitalNode)
        Dijk-->>DS: PathResult (Nodes, Distance in km)
    end
    DS->>DS: Select nearest capable hospital (Apex Heart Center at N7)

    DS->>Repo: Save Dispatch & mark Ambulance BUSY
    Repo->>DB: INSERT dispatches, UPDATE ambulances SET available=false
    DS-->>Ctrl: DispatchResponse (Shortest path, ETA, distance)
    Ctrl-->>FE: 201 Created + Route Coordinates

    FE->>FE: Draw cyan route polyline on Leaflet Map
    FE->>FE: Animate ambulance marker along route nodes
    FE->>Ctrl: PATCH /api/dispatches/1/status (ON_SCENE -> EN_ROUTE -> COMPLETED)
    Ctrl->>DS: updateDispatchStatus(1, COMPLETED)
    DS->>Repo: Set Ambulance AVAILABLE, Emergency RESOLVED, Bed - 1
    Repo->>DB: Commit transaction
    DS-->>FE: Updated telemetry
    FE->>FE: Update KPI counts & mission completed badge
```

---

### 3. UML Class Diagram

```mermaid
classDiagram
    class Graph {
        -Map~String, List~Edge~~ adjacencyList
        -Map~String, String~ nodeLabels
        +addNode(String id, String label)
        +addUndirectedEdge(String u, String v, double dist)
        +getNeighbors(String nodeId) List~Edge~
        +containsNode(String nodeId) boolean
    }

    class Edge {
        -String targetNodeId
        -double distanceKm
        +getTargetNodeId() String
        +getDistanceKm() double
    }

    class Dijkstra {
        +findShortestPath(Graph graph, String source, String dest) PathResult
    }

    class PathResult {
        -boolean reachable
        -double totalDistanceKm
        -List~String~ pathNodes
        -String errorMessage
        +isReachable() boolean
        +getTotalDistanceKm() double
        +getPathNodes() List~String~
    }

    class Emergency {
        -Long id
        -String referenceCode
        -String locationNodeId
        -String emergencyType
        -String severity
        -String description
        -String status
        -LocalDateTime createdAt
        +getSeverityWeight() int
        +compareTo(Emergency other) int
    }

    class Ambulance {
        -String id
        -String vehicleCode
        -String currentNodeId
        -String ambulanceType
        -Boolean available
        -String equipment
        -String status
        -String currentAssignment
        +hasEquipmentItem(String item) boolean
    }

    class Hospital {
        -String id
        -String name
        -String locationNodeId
        -Integer availableBeds
        -Integer totalBeds
        -String facilities
        -String capacityStatus
        +hasFacilityItem(String facility) boolean
        +isEligibleFor(String requiredFacility) boolean
    }

    class Dispatch {
        -Long id
        -Long emergencyId
        -String ambulanceId
        -String hospitalId
        -String routeEmergencyJson
        -String routeHospitalJson
        -Double totalDistanceKm
        -Double estimatedResponseMinutes
        -String status
        -LocalDateTime assignedAt
        -LocalDateTime completedAt
    }

    Graph "1" *-- "*" Edge : contains
    Dijkstra ..> Graph : traverses
    Dijkstra ..> PathResult : produces
    Dispatch --> Emergency : handles
    Dispatch --> Ambulance : assigns
    Dispatch --> Hospital : routes to
```

---

### 4. Database Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    ROAD_NODES ||--o{ ROAD_EDGES : "source / target"
    ROAD_NODES ||--o{ AMBULANCES : "stationed at"
    ROAD_NODES ||--o{ HOSPITALS : "located at"
    ROAD_NODES ||--o{ EMERGENCIES : "incident site"

    EMERGENCIES ||--o| DISPATCHES : "triggers"
    AMBULANCES ||--o{ DISPATCHES : "assigned to"
    HOSPITALS ||--o{ DISPATCHES : "receives patient"

    DISPATCHES ||--o{ STATUS_HISTORY : "tracks lifecycle"

    ROAD_NODES {
        string id PK
        string name
        double latitude
        double longitude
    }

    ROAD_EDGES {
        bigint id PK
        string source_node_id FK
        string target_node_id FK
        double distance_km
        boolean is_bidirectional
    }

    AMBULANCES {
        string id PK
        string vehicle_code UK
        string current_node_id FK
        string ambulance_type
        boolean available
        string equipment
        string status
        string current_assignment
    }

    HOSPITALS {
        string id PK
        string name
        string location_node_id FK
        int available_beds
        int total_beds
        string facilities
        string capacity_status
    }

    EMERGENCIES {
        bigint id PK
        string reference_code UK
        string location_node_id FK
        string emergency_type
        string severity
        string description
        string status
        datetime created_at
    }

    DISPATCHES {
        bigint id PK
        bigint emergency_id FK
        string ambulance_id FK
        string hospital_id FK
        text route_emergency_json
        text route_hospital_json
        double total_distance_km
        double estimated_response_minutes
        string status
        datetime assigned_at
        datetime completed_at
    }

    STATUS_HISTORY {
        bigint id PK
        bigint dispatch_id FK
        string status
        datetime timestamp
        string note
    }
```

---

### 5. Algorithmic Complexity Analysis

#### Dijkstra's Algorithm:
- **Vertices ($V$):** 18 road intersections (`N1` to `N18`).
- **Edges ($E$):** 28 bidirectional road segments (56 directed edges).
- **Time Complexity:** $\mathcal{O}((V + E) \log V)$
  - Extract-min operation: Each vertex extracted from PriorityQueue at most once $\rightarrow V \log V$.
  - Edge relaxation: Each edge visited once per directed representation and relaxed into heap $\rightarrow E \log V$.
- **Space Complexity:** $\mathcal{O}(V + E)$
  - Adjacency list: $\mathcal{O}(V + E)$
  - Distance map, visited set, predecessor map: $\mathcal{O}(V)$
  - Priority queue: $\mathcal{O}(V)$

#### Priority Queue Emergency Sorting:
- **Time Complexity:**
  - Insertion (`offer`): $\mathcal{O}(\log N)$
  - Extraction (`poll`): $\mathcal{O}(\log N)$
  - Peek highest priority: $\mathcal{O}(1)$
- **Comparator Order:**
  1. Primary: Severity weight (`CRITICAL` = 4, `HIGH` = 3, `MEDIUM` = 2, `LOW` = 1).
  2. Secondary: Creation timestamp (`createdAt` ascending, guaranteeing FIFO fairness for identical severity).
