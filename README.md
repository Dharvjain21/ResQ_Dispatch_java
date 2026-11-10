# ResQ Dispatch: Smart Ambulance Dispatch & Emergency Management System

> **A Complete College-Level Full-Stack Engineering Project**  
> **Technologies:** Java 17/21 • Spring Boot 3.x • Dijkstra's Algorithm • Leaflet.js • OpenStreetMap • MySQL / H2 • HTML5/CSS3/JavaScript  
> **Architecture:** Layered Enterprise Architecture (Controller -> Service -> Repository -> Entity)  
> **UI Aesthetic:** Military / Emergency Operations Command Center Dark Navy Theme

---

## 📑 Table of Contents
1. [Project Overview](#project-overview)
2. [Key Features & Highlights](#key-features--highlights)
3. [Dijkstra's Algorithm & DSA Architecture](#dijkstras-algorithm--dsa-architecture)
4. [Project Structure](#project-structure)
5. [Prerequisites & Environment](#prerequisites--environment)
6. [Quick Start (Stage 1 Console Demonstration)](#quick-start-stage-1-console-demonstration)
7. [Running the Spring Boot Web Application](#running-the-spring-boot-web-application)
8. [Deploying on Render](#deploying-on-render)
9. [Database Setup (H2 vs MySQL)](#database-setup-h2-vs-mysql)
10. [REST API Documentation & Postman Examples](#rest-api-documentation--postman-examples)
11. [Testing & Verification Checklist](#testing--verification-checklist)
12. [Academic Defense & Viva Prep](#academic-defense--viva-prep)

---

## 1. Project Overview

**ResQ Dispatch** simulates an intelligent metropolitan emergency response command center. When medical emergencies are reported by citizens or operators, the system:
- **Prioritizes incidents** in real-time (`CRITICAL > HIGH > MEDIUM > LOW`), applying FIFO tie-breaking for equal severity.
- **Finds the nearest qualified ambulance** matching clinical requirements (Advanced Life Support vs Basic Life Support, Defibrillator, Ventilator, Oxygen).
- **Calculates the optimal road route** dynamically using Dijkstra's shortest path algorithm on an adjacency-list graph representing 18 city intersections.
- **Selects an eligible destination hospital** that has available beds and the required specialized facilities (Cardiac Care, Trauma Care, Burn Care, ICU).
- **Simulates GPS vehicle tracking** on an interactive Leaflet.js map, updating mission states from dispatch through on-scene triage to hospital admission.
- **Aggregates operational telemetry** into dashboard metrics, response time analytics, and historical audit logs.

---

## 2. Key Features & Highlights

- **Anti-Slop Implementation:** No fake buttons, no dummy data, no hardcoded routing results. Every button triggers real API endpoints and algorithms.
- **Dual Database Support:**
  - **H2 In-Memory Database (Default):** Zero-install, starts automatically with Spring Boot for immediate local evaluation.
  - **MySQL 8.x Database:** Production-grade normalized schema with foreign keys, indexes, and sample seed data.
- **Data Initialization:** Seeds 18 intersections, 28 road edges, 6 ambulances, 4 hospitals, and initial emergencies on application launch.
- **Emergency Priority Queue:** Heap-based triage ensuring critical patients receive attention first.
- **Clinical Suitability Engine:** Prevents assigning basic patient transport vehicles to acute cardiac or multi-trauma incidents.
- **Dynamic Capacity Control:** Automatically decrements hospital bed counts upon admission and frees ambulances upon mission completion.
- **Simulated GPS Telemetry:** Visual waypoint interpolation and route progress tracking on a dark-themed Leaflet map.

---

## 3. Dijkstra's Algorithm & DSA Architecture

### Road Network Topology
The system models a 18-intersection metro road grid (`N1` to `N18`):
- **Vertices ($V$):** 18 intersections with real-world geographical coordinates.
- **Edges ($E$):** 28 bidirectional road segments with physical distances in kilometers.
- **Isolated Node ($N18$):** An island depot with zero connected roads, specifically designed to test unreachable destination handling.

### Algorithmic Complexity
- **Time Complexity:** $\mathcal{O}((V + E) \log V)$
  - Uses an Adjacency List `Map<String, List<Edge>>` for constant-time neighbor lookups.
  - Utilizes a `PriorityQueue<NodeDistance>` (Min-Heap) for $\mathcal{O}(\log V)$ extract-min operations.
  - Edge relaxation updates vertex distances and inserts into the heap in $\mathcal{O}(E \log V)$ time.
- **Space Complexity:** $\mathcal{O}(V + E)$ for graph representation and predecessor back-pointers.

### Path Reconstruction
During graph traversal, whenever a shorter path to neighbor vertex $v$ through $u$ is discovered:
$$\text{dist}[v] = \text{dist}[u] + w(u, v), \quad \text{prev}[v] = u$$
Upon reaching the target, the complete path is reconstructed backwards in linear time $\mathcal{O}(V)$ using a `LinkedList`.

---

## 4. Project Structure

```text
resq-dispatch/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/resq/
│   │   │   │   ├── ResqApplication.java
│   │   │   │   ├── algorithm/
│   │   │   │   │   ├── Edge.java
│   │   │   │   │   ├── Graph.java
│   │   │   │   │   ├── PathResult.java
│   │   │   │   │   └── Dijkstra.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── RoadNode.java
│   │   │   │   │   ├── RoadEdge.java
│   │   │   │   │   ├── Emergency.java
│   │   │   │   │   ├── Ambulance.java
│   │   │   │   │   ├── Hospital.java
│   │   │   │   │   ├── Dispatch.java
│   │   │   │   │   └── StatusHistory.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── RoadNodeRepository.java
│   │   │   │   │   ├── RoadEdgeRepository.java
│   │   │   │   │   ├── EmergencyRepository.java
│   │   │   │   │   ├── AmbulanceRepository.java
│   │   │   │   │   ├── HospitalRepository.java
│   │   │   │   │   ├── DispatchRepository.java
│   │   │   │   │   └── StatusHistoryRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── RouteService.java
│   │   │   │   │   ├── EmergencyService.java
│   │   │   │   │   ├── AmbulanceService.java
│   │   │   │   │   ├── HospitalService.java
│   │   │   │   │   ├── DispatchService.java
│   │   │   │   │   └── ReportService.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── EmergencyController.java
│   │   │   │   │   ├── AmbulanceController.java
│   │   │   │   │   ├── HospitalController.java
│   │   │   │   │   ├── DispatchController.java
│   │   │   │   │   ├── RouteController.java
│   │   │   │   │   └── ReportController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── EmergencyCreateRequest.java
│   │   │   │   │   ├── EmergencyResponse.java
│   │   │   │   │   ├── AmbulanceResponse.java
│   │   │   │   │   ├── HospitalResponse.java
│   │   │   │   │   ├── DispatchResponse.java
│   │   │   │   │   ├── StatusUpdateRequest.java
│   │   │   │   │   ├── ReportSummaryResponse.java
│   │   │   │   │   ├── RoadNetworkResponse.java
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── AllocationException.java
│   │   │   │   │   ├── RouteNotFoundException.java
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   └── config/
│   │   │   │       ├── CorsConfig.java
│   │   │   │       └── DataInitializer.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-mysql.properties
│   │   │       └── static/                  <-- Integrated Web Dashboard
│   │   │           ├── index.html
│   │   │           ├── style.css
│   │   │           └── app.js
│   │   └── test/java/com/resq/
│   │       ├── algorithm/DijkstraTest.java
│   │       └── service/
│   │           ├── AmbulanceAllocationTest.java
│   │           └── HospitalSelectionTest.java
├── cli-demo/                                <-- Standalone Stage 1 Prototype
│   └── src/com/resq/console/
│       └── ConsoleSimulationDemo.java
├── frontend/                                <-- Standalone Frontend Assets
│   ├── index.html
│   ├── style.css
│   └── app.js
├── database/
│   ├── schema.sql
│   └── sample-data.sql
├── docs/
│   ├── ARCHITECTURE.md                      <-- System, Sequence, UML & ER diagrams
│   ├── VIVA_QUESTIONS_AND_ANSWERS.md        <-- Oral exam & technical defense prep
│   └── PROJECT_REPORT.md                    <-- College project report & synopsis
└── README.md
```

---

## 5. Prerequisites & Environment

1. **Java Development Kit (JDK):** Java 17 or Java 21. Verify via:
   ```powershell
   java -version
   javac -version
   ```
2. **Build Tool:** Apache Maven 3.8+. Verify via:
   ```powershell
   mvn -version
   ```
3. **Web Browser:** Google Chrome, Microsoft Edge, or Mozilla Firefox.
4. **Database (Optional):** MySQL 8.x (if using MySQL profile; H2 runs out of the box with zero installation).

---

## 6. Quick Start (Stage 1 Console Demonstration)

The project includes a standalone Java console prototype (`ConsoleSimulationDemo.java`) that requires **no external dependencies or build tools**. You can compile and run it directly with standard `javac`:

### Windows PowerShell:
```powershell
cd "resq-dispatch\cli-demo"
javac -d bin src\com\resq\console\ConsoleSimulationDemo.java
java -cp bin com.resq.console.ConsoleSimulationDemo
```

### macOS / Linux:
```bash
cd resq-dispatch/cli-demo
javac -d bin src/com/resq/console/ConsoleSimulationDemo.java
java -cp bin com.resq.console.ConsoleSimulationDemo
```

This console application runs 4 complete test scenarios:
1. **Dijkstra Route Calculation:** Finds the shortest path from `N1` to `N12` ($7.30\text{ km}$).
2. **Priority Queue Sorting:** Verifies `CRITICAL` cases pop first, with FIFO tie-breaking for equal severity.
3. **Specialized Ambulance Allocation:** Verifies only ALS units with Defibrillators are assigned to Cardiac emergencies.
4. **Hospital Capacity Rejection:** Demonstrates bypassing a closer hospital with 0 beds to choose one with available capacity.
5. **Unreachable Node Test:** Verifies graceful error handling when querying isolated node `N18`.

---

## 7. Running the Spring Boot Web Application

### Step 1: Start the Backend Server
Navigate to the `backend` folder and run using Maven:

```powershell
cd "resq-dispatch\backend"
mvn spring-boot:run
```

Once started, the Spring Boot application initializes:
- Embedded Tomcat Web Server on port `8080`.
- H2 in-memory database with pre-populated sample road graph, ambulances, hospitals, and emergencies.
- Static dashboard served directly at `http://localhost:8080/`.

### Step 2: Open the Web Dashboard
Open your browser and navigate to:
```text
http://localhost:8080/
```

*(Alternatively, you can open `frontend/index.html` directly in any web browser; it automatically communicates with the Spring Boot backend at `http://localhost:8080/api`!)*

### Step 3: Access H2 Database Web Console
To inspect database tables and execute SQL queries directly in your browser:
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:resq_db`
- **User Name:** `sa`
- **Password:** *(Leave blank)*

---

## 8. Deploying on Render

The repository includes [`render.yaml`](render.yaml), which configures a Render web service for the Spring Boot backend.

1. Push the repository to GitHub.
2. In Render, select **New +** → **Blueprint**.
3. Connect `Dharvjain21/ResQ_Dispatch_java`.
4. Select the `main` branch and approve the Blueprint.
5. Render builds from `backend` with Maven and starts the packaged JAR.
6. Open the generated `https://<service-name>.onrender.com/` URL.

The application reads Render's `PORT` environment variable and falls back to `8080` locally. The default Render deployment uses the embedded H2 database, so seeded data is recreated after a service restart or redeploy. Use the MySQL profile with an external managed MySQL database if persistent production data is required.

## 9. Database Setup (H2 vs MySQL)

### Using Default H2 (Zero Installation)
No configuration required! The default `application.properties` uses embedded H2. All tables and seed records are automatically provisioned by `DataInitializer.java` on startup.

### Using MySQL 8.x
1. Start your local MySQL service.
2. Create the database and seed tables using the provided SQL scripts:
   ```powershell
   mysql -u root -p < "resq-dispatch\database\schema.sql"
   mysql -u root -p < "resq-dispatch\database\sample-data.sql"
   ```
3. Update database credentials in `backend/src/main/resources/application-mysql.properties` if your MySQL password is not `root`.
4. Run Spring Boot with the MySQL profile:
   ```powershell
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 9. REST API Documentation & Postman Examples

### 1. Register New Emergency
- **Endpoint:** `POST /api/emergencies`
- **Request Body:**
```json
{
  "locationNodeId": "N7",
  "emergencyType": "Cardiac Emergency",
  "severity": "CRITICAL",
  "description": "62-year-old male collapsed in shopping mall with acute chest pain."
}
```
- **Response (`201 Created`):**
```json
{
  "success": true,
  "message": "Emergency incident logged and queued into priority triage.",
  "data": {
    "id": 1,
    "referenceCode": "EMG-8F32AC10",
    "locationNodeId": "N7",
    "emergencyType": "Cardiac Emergency",
    "severity": "CRITICAL",
    "description": "62-year-old male collapsed in shopping mall with acute chest pain.",
    "status": "PENDING",
    "createdAt": "2026-11-10T11:45:00"
  }
}
```

### 2. Allocate Ambulance & Hospital (Dijkstra Calculation)
- **Endpoint:** `POST /api/dispatches/{emergencyId}/allocate`
- **Response (`201 Created`):**
```json
{
  "success": true,
  "message": "Optimal ambulance and hospital allocated via Dijkstra shortest-path algorithm.",
  "data": {
    "id": 1,
    "emergencyId": 1,
    "emergencyRefCode": "EMG-8F32AC10",
    "emergencyType": "Cardiac Emergency",
    "emergencySeverity": "CRITICAL",
    "emergencyLocation": "N7",
    "ambulanceId": "AMB-101",
    "ambulanceVehicleCode": "DL-01-EQ-9001",
    "hospitalId": "HOSP-1",
    "hospitalName": "Apex Metro Trauma & Heart Center",
    "hospitalLocation": "N7",
    "routeToEmergency": ["N1", "N2", "N7"],
    "routeToHospital": ["N7"],
    "totalDistanceKm": 3.9,
    "estimatedResponseMinutes": 5.85,
    "status": "AMBULANCE_ASSIGNED",
    "assignedAt": "2026-11-10T11:45:05"
  }
}
```

### 3. Update Dispatch Status / Simulate Progress
- **Endpoint:** `PATCH /api/dispatches/{id}/status`
- **Request Body:**
```json
{
  "status": "ON_SCENE",
  "note": "Ambulance arrived at patient location N7."
}
```
- **Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Dispatch status updated to: ON_SCENE",
  "data": {
    "id": 1,
    "status": "ON_SCENE"
  }
}
```

### 4. Fetch Operational Analytics Summary
- **Endpoint:** `GET /api/reports/summary`
- **Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Operational metrics summary retrieved.",
  "data": {
    "totalEmergencies": 5,
    "activeEmergencies": 4,
    "pendingEmergencies": 3,
    "resolvedEmergencies": 1,
    "criticalEmergencies": 2,
    "totalAmbulances": 6,
    "availableAmbulances": 4,
    "busyAmbulances": 2,
    "totalHospitals": 4,
    "availableBeds": 22,
    "completedDispatches": 1,
    "activeDispatches": 1,
    "averageResponseTimeMinutes": 5.8
  }
}
```

---

## 10. Testing & Verification Checklist

To run the automated JUnit 5 test suite:
```powershell
cd "resq-dispatch\backend"
mvn test
```

### Verification Scenarios Tested:
- [x] **Dijkstra Dynamic Shortest Route:** Confirmed against known path distances.
- [x] **Unreachable Destination Handling:** Verified isolated node `N18` returns an error instead of false routing.
- [x] **Source Equals Destination:** Verifies 0 km path when ambulance is already at scene.
- [x] **Priority Queue Ordering:** Verified `CRITICAL` cases pop first; FIFO tie-break for equal severity.
- [x] **Specialized Ambulance Matching:** Verified ALS vehicles with defibrillators are selected for cardiac emergencies.
- [x] **Busy Fleet Exclusion:** Dispatched ambulances are skipped from allocation candidates.
- [x] **Hospital Capacity Check:** Hospitals with 0 beds are rejected even if geographically closer.
- [x] **Transactional Integrity:** State changes to ambulances, dispatches, and beds are atomic.

---

## 11. Academic Defense & Viva Prep

Detailed documentation prepared specifically for college viva defense:
- **System Architecture & Diagrams:** [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- **Top 11 Viva Questions & Answers:** [`docs/VIVA_QUESTIONS_AND_ANSWERS.md`](docs/VIVA_QUESTIONS_AND_ANSWERS.md)
- **Complete Project Report & Synopsis:** [`docs/PROJECT_REPORT.md`](docs/PROJECT_REPORT.md)
