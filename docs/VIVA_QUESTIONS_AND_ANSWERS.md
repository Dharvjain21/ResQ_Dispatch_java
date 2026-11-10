# College Viva Preparation: Questions & Answers
## Smart Ambulance Dispatch & Emergency Management System (ResQ Dispatch)

---

### Q1: What is the main objective of the ResQ Dispatch project?
**Answer:**  
ResQ Dispatch is an academic emergency response simulation system. When a medical emergency is reported, the system:
1. Prioritizes emergencies based on medical urgency (`CRITICAL > HIGH > MEDIUM > LOW`).
2. Finds and allocates the nearest suitable, available ambulance based on vehicle capabilities (ALS, BLS) and onboard equipment (Ventilator, Defibrillator, Oxygen).
3. Uses **Dijkstra's Shortest Path Algorithm** on an adjacency-list graph representing a city road network to find the optimal path.
4. Selects the nearest hospital that possesses the required specialized medical department (Cardiac Care, Burn Care, Trauma Care, ICU) and has **available bed capacity**.
5. Displays the route and simulates GPS tracking of the ambulance on an interactive Leaflet map, tracking its complete lifecycle from dispatch to patient delivery.

---

### Q2: Why did you choose Dijkstra's algorithm instead of BFS or Bellman-Ford?
**Answer:**  
- **Why not BFS (Breadth-First Search)?** BFS only finds the shortest path in an **unweighted** graph (where all edges have the same cost/distance of 1). In a real city road network, road segments have different physical lengths (e.g., 1.2 km, 2.5 km). BFS would minimize the number of intersections (hops), but might pick a route that is physically much longer.
- **Why not Bellman-Ford?** Bellman-Ford has a time complexity of $\mathcal{O}(V \times E)$ and is primarily used when graphs have negative edge weights. Because physical road distances are always non-negative ($d \ge 0$), Dijkstra is significantly faster ($\mathcal{O}((V + E) \log V)$) and mathematically optimal.
- **Why Dijkstra?** Dijkstra is the industry standard for single-source shortest path problems with non-negative weights, providing provably optimal routes in sub-second time using a Min-Heap / PriorityQueue.

---

### Q3: What is the time and space complexity of your Dijkstra implementation?
**Answer:**  
- **Time Complexity:** $\mathcal{O}((V + E) \log V)$
  - $V$ is the number of intersections (vertices) and $E$ is the number of road segments (edges).
  - Extracting the minimum distance node from the PriorityQueue (`minHeap.poll()`) takes $\mathcal{O}(\log V)$ time and happens at most $V$ times $\rightarrow \mathcal{O}(V \log V)$.
  - Relaxing adjacent edges and inserting new tentative distances into the PriorityQueue takes $\mathcal{O}(\log V)$ and happens at most $E$ times $\rightarrow \mathcal{O}(E \log V)$.
- **Space Complexity:** $\mathcal{O}(V + E)$
  - Adjacency list requires $\mathcal{O}(V + E)$ memory.
  - Distances table, visited set, predecessor map, and PriorityQueue each require $\mathcal{O}(V)$ memory.

---

### Q4: How do you reconstruct the actual path of intersections from Dijkstra?
**Answer:**  
During the relaxation step in Dijkstra's algorithm, whenever a shorter path to a neighbor vertex $v$ is discovered through current vertex $u$ (i.e., `dist[u] + weight < dist[v]`), we store $u$ in a `predecessors` map:
```java
prev.put(v, u);
```
Once the destination is reached, we backtrack from the destination node to the source node using a `LinkedList`:
```java
LinkedList<String> path = new LinkedList<>();
String step = destination;
while (step != null) {
    path.addFirst(step);
    step = prev.get(step);
}
```
This reconstructs the exact sequence of intersection IDs (e.g., `N1 -> N2 -> N7 -> N16 -> N12`) in linear time $\mathcal{O}(V)$.

---

### Q5: How does your emergency priority queue work?
**Answer:**  
Emergencies implement Java's `Comparable<Emergency>` interface. The comparator evaluates two criteria:
1. **Primary Key — Severity Weight:**
   - `CRITICAL` = 4
   - `HIGH` = 3
   - `MEDIUM` = 2
   - `LOW` = 1
   A higher weight always gets processed before a lower weight.
2. **Secondary Key (Tie-breaker) — Timestamp (FIFO):**
   If two emergencies share the same severity (e.g., two `CRITICAL` cardiac emergencies), the older emergency (`createdAt` ascending) is popped first to prevent incident starvation.

---

### Q6: How does the system ensure an ambulance is not double-booked?
**Answer:**  
In `DispatchService.java`, the `allocateEmergency()` method is marked with Spring's `@Transactional` annotation.
1. When checking candidate ambulances, it queries only vehicles where `available == true`.
2. As soon as the optimal ambulance is chosen, its status is immediately set to `available = false` and `status = "ASSIGNED"`, and saved within the same database transaction.
3. If two dispatch requests arrive concurrently, the database transactional lock prevents both threads from acquiring the same ambulance record, avoiding double allocation.

---

### Q7: What happens if a hospital is nearest to the emergency but has 0 beds or lacks the required medical facility?
**Answer:**  
The algorithm enforces strict clinical eligibility criteria:
1. **Facility Check:** The emergency type dictates the required facility (e.g., Fire Injury $\rightarrow$ "Burn Care", Cardiac Emergency $\rightarrow$ "Cardiac Care", Road Accident $\rightarrow$ "Trauma Care").
2. **Bed Capacity Check:** The hospital must have `availableBeds > 0`.
3. In our test scenario, **Phoenix Burn Institute (`HOSP-4`)** is located at node `N13`, which is only 2.5 km away from emergency node `N12`. However, it has 0 available beds (`FULL`). The system detects this, skips `HOSP-4`, and selects **Apex Metro Trauma & Heart Center (`HOSP-1`)** at `N7`, which has both the Burn Care facility and 8 available beds.
4. If no specialized hospital is available, the system attempts a graceful fallback to a hospital with `General Emergency` capacity before returning a clear error if none exist.

---

### Q8: What happens if a destination is unreachable due to road blocks or floods?
**Answer:**  
Node `N18` (Isolated Island Depot) has zero edges connecting it to the road graph. When Dijkstra runs for `N18`, the distance remains at `Double.POSITIVE_INFINITY`.
The system detects `isReachable() == false` and throws a custom `AllocationException` with the message:
`"CRITICAL ALERT: No suitable or available ambulance can reach location N18"`.
The system prevents creating a invalid dispatch or false routing.

---

### Q9: Explain the layered architecture of your Spring Boot backend.
**Answer:**  
The project uses the standard Controller-Service-Repository-Model pattern:
1. **Controller Layer (`@RestController`):** Handles incoming HTTP requests, validates input using `@Valid`, maps DTOs, and returns HTTP response entities with appropriate status codes (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`).
2. **Service Layer (`@Service`):** Contains all domain business logic, executes Dijkstra's algorithm, checks ambulance equipment and hospital facilities, manages transactions (`@Transactional`), and computes metrics.
3. **Repository Layer (`@Repository`):** Interfaces extending `JpaRepository<T, ID>` that provide CRUD operations and custom JPQL queries without writing boilerplate JDBC code.
4. **Model/Entity Layer (`@Entity`):** Plain Old Java Objects (POJOs) mapped to relational database tables using JPA/Hibernate annotations (`@Id`, `@Column`, `@Table`, `@Index`).
5. **Exception Handler (`@RestControllerAdvice`):** Centralizes error handling so client applications receive consistent JSON error responses (`ErrorResponse`).

---

### Q10: How does the simulated GPS tracking on the Leaflet map work?
**Answer:**  
1. The backend Dijkstra service calculates the ordered list of node IDs (e.g., `["N1", "N2", "N7", "N16", "N12"]`) and returns them as a JSON array.
2. The frontend JavaScript maps these node IDs to their geographical GPS coordinates (`[latitude, longitude]`).
3. Leaflet draws the complete path using an animated neon cyan polyline (`L.polyline`).
4. A JavaScript timer (`setInterval`) interpolates the ambulance marker's position step-by-step from waypoint to waypoint.
5. As the marker reaches critical milestone waypoints (the incident location and the hospital), it automatically invokes the backend status update endpoint (`PATCH /api/dispatches/{id}/status`), transitioning through:
   `AMBULANCE_ASSIGNED` $\rightarrow$ `EN_ROUTE_TO_EMERGENCY` $\rightarrow$ `ON_SCENE` $\rightarrow$ `EN_ROUTE_TO_HOSPITAL` $\rightarrow$ `COMPLETED`.
6. When completed, the ambulance is freed back to `AVAILABLE` status and the hospital bed count is decremented.

---

### Q11: What is the difference between H2 and MySQL in this project?
**Answer:**  
- **H2 Database:** An in-memory, embedded Java database. It requires zero external installation, starts instantly with the Spring Boot application, and is perfect for rapid unit testing, viva demonstrations, and evaluators who do not have MySQL installed.
- **MySQL Database:** An enterprise relational database management system. We provide `schema.sql` and `sample-data.sql` with full foreign keys, indexes, and normalized tables. By activating the `mysql` Spring profile (`--spring.profiles.active=mysql`), the application persists all data permanently across system reboots.
