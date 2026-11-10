# Project Report & Synopsis
# SMART AMBULANCE DISPATCH & EMERGENCY MANAGEMENT SYSTEM (ResQ Dispatch)

---

## 1. Abstract
Emergency medical response systems depend heavily on rapid triage, optimal fleet allocation, and low-latency route calculation. In dense metropolitan environments, manual dispatching often results in suboptimal ambulance allocation, delays in locating specialized hospital facilities (such as cardiac care units or burn wards), and increased patient mortality.

**ResQ Dispatch** is an integrated full-stack web simulation engineered to address these challenges. The system dynamically triages emergencies using a priority queue (ranking cases by severity: `CRITICAL > HIGH > MEDIUM > LOW`), allocates the closest qualified ambulance using **Dijkstra's shortest path algorithm** on an adjacency list road network ($\mathcal{O}((V+E)\log V)$), identifies hospitals based on real-time bed capacity and clinical specialization, and provides an interactive command center dashboard with simulated GPS tracking using Leaflet.js and OpenStreetMap.

---

## 2. Problem Statement
Traditional ambulance dispatching suffers from three primary bottlenecks:
1. **Inefficient Routing:** Dispatchers often rely on rough estimates or fixed zone boundaries, failing to dynamically calculate the shortest road distance in complex networks.
2. **Suboptimal Hospital Selection:** Patients are often rushed to the physically nearest clinic regardless of whether that facility has available ICU/burn beds or specialized doctors.
3. **Lack of Centralized Telemetry:** Operations centers lack unified visual oversight of vehicle availability, real-time dispatch progress, and historical response times.

---

## 3. Technology Stack & Justification

| Layer | Technology | Justification |
| :--- | :--- | :--- |
| **Backend Framework** | Java 17 / 21, Spring Boot 3.x | Type safety, high performance, enterprise IoC, dependency injection, and JPA repository abstractions. |
| **Routing Algorithm** | Dijkstra's Algorithm | Guarantees mathematically optimal shortest paths in weighted road networks with non-negative edge weights. |
| **Data Structures** | Adjacency List, PriorityQueue (Min-Heap) | Efficient $\mathcal{O}((V+E)\log V)$ execution time and $\mathcal{O}(V+E)$ space complexity. |
| **Relational Database** | MySQL 8.x / H2 Database | ACID compliance, referential integrity with foreign keys, and dual profile support (embedded testing or persistent production). |
| **Frontend Architecture** | HTML5, CSS3, Vanilla JavaScript, Fetch API | Zero-dependency, lightweight, rapid DOM updates, custom military/ops center theme. |
| **Mapping Engine** | Leaflet.js & OpenStreetMap | Open-source interactive mapping with custom tile filters, animated waypoint movement, and layer controls. |

---

## 4. Module Specifications

### Module 1: Emergency Management
- Generates unique reference identifiers (e.g., `EMG-801`).
- Captures location intersection node, clinical emergency type, description, and timestamp.
- Orders emergency incidents in a Priority Queue using a compound comparator:
  $$\text{Primary: Severity Weight} \quad (\text{CRITICAL} = 4 > \text{HIGH} = 3 > \text{MEDIUM} = 2 > \text{LOW} = 1)$$
  $$\text{Secondary: Creation Time} \quad (\text{FIFO tie-breaker})$$

### Module 2: Ambulance Fleet Allocation
- Classifies ambulances into ALS (Advanced Life Support), BLS (Basic Life Support), and PT (Patient Transport).
- Verifies onboard equipment requirements (e.g., Ventilator, Defibrillator, Oxygen).
- Runs Dijkstra from all available ambulance depots to the emergency intersection to find the minimum distance.
- Uses vehicle code as a deterministic tie-breaker if two ambulances are equidistant.
- Employs `@Transactional` database locks to prevent concurrent double-booking.

### Module 3: Dijkstra Shortest Route Calculation
- Represents 18 intersections (`N1` to `N18`) and 28 road segments as an undirected weighted graph.
- Calculates exact cumulative distances in kilometers.
- Stores predecessors to reconstruct the full sequence of waypoints.
- Flags unreachable destinations (such as isolated node `N18`) gracefully.

### Module 4: Hospital Selection Engine
- Filters hospitals by specialized medical facilities:
  - Cardiac Emergency $\rightarrow$ `Cardiac Care` or `ICU`
  - Road Accident $\rightarrow$ `Trauma Care` or `General Emergency`
  - Fire Injury $\rightarrow$ `Burn Care`
  - General Medical $\rightarrow$ `General Emergency`
- Enforces `availableBeds > 0`; rejects full hospitals even if they are closest.
- Automatically decrements available bed count upon mission completion.

### Module 5: GPS Tracking & Leaflet Visualization
- Renders the city grid with custom dark-themed tiles.
- Overlays nodes, ambulances, hospitals, and emergency markers with status badges.
- Draws neon cyan polyline routes with dashed pulse aesthetics.
- Animates ambulance marker traversal through sequential waypoints on a configurable timer.
- Triggers automated backend status transitions:
  $$\text{CREATED} \rightarrow \text{AMBULANCE\_ASSIGNED} \rightarrow \text{EN\_ROUTE} \rightarrow \text{ON\_SCENE} \rightarrow \text{TRANSPORTING} \rightarrow \text{COMPLETED}$$

### Module 6: Reports & Telemetry Dashboard
- Live KPI chips for Active Calls, Critical Emergencies, Operator Load, and Fleet Availability.
- Tabular views for Fleet Inventory, Hospital Directory, and Mission Dispatch Logs.
- Graphical distribution bars for emergencies classified by clinical type and severity.
- Mean response time calculation across completed missions.

---

## 5. Test Scenarios & Results

| Test ID | Scenario | Expected Behavior | Actual Result |
| :---: | :--- | :--- | :---: |
| **TC-01** | Dijkstra Shortest Path (`N1` to `N12`) | Dynamic calculation yields $7.30\text{ km}$ via `N1 -> N2 -> N7 -> N16 -> N12`. | **PASS** |
| **TC-02** | Priority Queue Ordering | `EMG-04` (CRITICAL, 20m ago) pops before `EMG-02` (CRITICAL, 8m ago) and `EMG-05` (HIGH). | **PASS** |
| **TC-03** | Specialized Ambulance Match | Critical Cardiac case assigns ALS vehicle with Defibrillator; ignores BLS units. | **PASS** |
| **TC-04** | Busy Vehicle Exclusion | Currently dispatched ambulance (`available=false`) is skipped. | **PASS** |
| **TC-05** | Hospital Bed Capacity Rejection | `HOSP-4` has Burn Care but 0 beds; routed to `HOSP-1` with 8 beds. | **PASS** |
| **TC-06** | Unreachable Node Handling | Destination `N18` (isolated island) returns clear error; blocks false dispatch. | **PASS** |
| **TC-07** | Mission Completion State Return | Completing mission restores ambulance to `AVAILABLE` at destination hospital. | **PASS** |
| **TC-08** | Input Validation Check | Empty location or invalid severity returns HTTP `400 Bad Request`. | **PASS** |

---

## 6. Future Scope
1. **Real-time Traffic Ingestion:** Integrating live traffic congestion APIs to dynamically adjust edge weights ($t = \frac{d}{v(t)}$).
2. **WebSocket Push Telemetry:** Upgrading from polling to Spring WebSockets / STOMP for sub-10ms dashboard push updates.
3. **Predictive Fleet Positioning:** Machine learning model to pre-position ambulances in high-risk zones based on historical weather, time of day, and accident hotspots.
4. **Multi-Patient Incident Routing:** Vehicle Routing Problem with Time Windows (VRPTW) for mass-casualty disaster events.
