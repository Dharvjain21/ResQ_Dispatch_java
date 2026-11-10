/**
 * ==============================================================================
 * RESQ DISPATCH: EMERGENCY OPERATIONS & GPS SIMULATION ENGINE
 * Vanilla JavaScript + Leaflet.js + Fetch API
 * ==============================================================================
 */

// Configuration & State
const API_BASE = '/api'; // Relative URL works when served by Spring Boot, or absolute when standalone
let fallbackApiUrl = 'http://localhost:8080/api';

const state = {
    emergencies: [],
    ambulances: [],
    hospitals: [],
    dispatches: [],
    roadNodes: {},
    roadEdges: [],
    selectedEmergency: null,
    activeDispatch: null,
    activeTab: 'map-view',
    simulationInterval: null,
    currentStepIndex: 0,
    routeProgress: 0
};

// Map & Layer References
let map = null;
let networkLayerGroup = null;
let ambulanceLayerGroup = null;
let hospitalLayerGroup = null;
let emergencyLayerGroup = null;
let routePolylineLayer = null;
let simulatedAmbulanceMarker = null;

// Fallback Mock Graph (Used if backend is offline during standalone HTML viewing)
const FALLBACK_NODES = [
    { id: 'N1', name: 'North Gate Interstation', latitude: 28.6325, longitude: 77.2185 },
    { id: 'N2', name: 'Metro Circle Terminal', latitude: 28.6300, longitude: 77.2250 },
    { id: 'N3', name: 'Tech Park Junction', latitude: 28.6250, longitude: 77.2320 },
    { id: 'N4', name: 'Riverside Boulevard', latitude: 28.6180, longitude: 77.2350 },
    { id: 'N5', name: 'East Bypass Outer', latitude: 28.6120, longitude: 77.2300 },
    { id: 'N6', name: 'Market Square Crossing', latitude: 28.6140, longitude: 77.2220 },
    { id: 'N7', name: 'Central Plaza Hub', latitude: 28.6200, longitude: 77.2180 },
    { id: 'N8', name: 'Civic Center Junction', latitude: 28.6260, longitude: 77.2120 },
    { id: 'N9', name: 'University Crossing', latitude: 28.6210, longitude: 77.2050 },
    { id: 'N10', name: 'West Expressway Toll', latitude: 28.6150, longitude: 77.2000 },
    { id: 'N11', name: 'Industrial Area Hub', latitude: 28.6080, longitude: 77.2060 },
    { id: 'N12', name: 'South Gate Interchange', latitude: 28.6050, longitude: 77.2150 },
    { id: 'N13', name: 'Harbor Link Crossing', latitude: 28.6030, longitude: 77.2240 },
    { id: 'N14', name: 'Greenfield Crossing', latitude: 28.6280, longitude: 77.2020 },
    { id: 'N15', name: 'Sports Arena Circle', latitude: 28.6350, longitude: 77.2100 },
    { id: 'N16', name: 'Downtown Terminus', latitude: 28.6170, longitude: 77.2120 },
    { id: 'N17', name: 'Suburb Crossing', latitude: 28.6380, longitude: 77.2250 },
    { id: 'N18', name: 'Isolated Island Depot (Unreachable)', latitude: 28.6450, longitude: 77.1900 }
];

const FALLBACK_EDGES = [
    { sourceNodeId: 'N1', targetNodeId: 'N2', distanceKm: 1.8 },
    { sourceNodeId: 'N1', targetNodeId: 'N8', distanceKm: 2.2 },
    { sourceNodeId: 'N1', targetNodeId: 'N15', distanceKm: 1.5 },
    { sourceNodeId: 'N2', targetNodeId: 'N3', distanceKm: 2.5 },
    { sourceNodeId: 'N2', targetNodeId: 'N7', distanceKm: 2.1 },
    { sourceNodeId: 'N2', targetNodeId: 'N17', distanceKm: 2.0 },
    { sourceNodeId: 'N3', targetNodeId: 'N4', distanceKm: 2.4 },
    { sourceNodeId: 'N4', targetNodeId: 'N5', distanceKm: 2.0 },
    { sourceNodeId: 'N4', targetNodeId: 'N6', distanceKm: 2.6 },
    { sourceNodeId: 'N5', targetNodeId: 'N6', distanceKm: 2.1 },
    { sourceNodeId: 'N5', targetNodeId: 'N13', distanceKm: 2.8 },
    { sourceNodeId: 'N6', targetNodeId: 'N7', distanceKm: 1.6 },
    { sourceNodeId: 'N6', targetNodeId: 'N12', distanceKm: 2.3 },
    { sourceNodeId: 'N7', targetNodeId: 'N8', distanceKm: 1.9 },
    { sourceNodeId: 'N7', targetNodeId: 'N16', distanceKm: 1.2 },
    { sourceNodeId: 'N7', targetNodeId: 'N9', distanceKm: 2.5 },
    { sourceNodeId: 'N8', targetNodeId: 'N9', distanceKm: 2.0 },
    { sourceNodeId: 'N8', targetNodeId: 'N14', distanceKm: 1.7 },
    { sourceNodeId: 'N9', targetNodeId: 'N10', distanceKm: 1.8 },
    { sourceNodeId: 'N9', targetNodeId: 'N16', distanceKm: 1.5 },
    { sourceNodeId: 'N10', targetNodeId: 'N11', distanceKm: 2.2 },
    { sourceNodeId: 'N10', targetNodeId: 'N16', distanceKm: 2.4 },
    { sourceNodeId: 'N11', targetNodeId: 'N12', distanceKm: 2.1 },
    { sourceNodeId: 'N11', targetNodeId: 'N16', distanceKm: 2.7 },
    { sourceNodeId: 'N12', targetNodeId: 'N13', distanceKm: 2.5 },
    { sourceNodeId: 'N14', targetNodeId: 'N15', distanceKm: 2.3 },
    { sourceNodeId: 'N14', targetNodeId: 'N9', distanceKm: 2.4 },
    { sourceNodeId: 'N16', targetNodeId: 'N12', distanceKm: 2.2 }
];

// ------------------------------------------------------------------------------
// INITIALIZATION
// ------------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    initLeafletMap();
    setupEventListeners();
    fetchAllData();

    // Auto-refresh summary telemetry every 10 seconds
    setInterval(() => {
        fetchReportsSummary();
    }, 10000);
});

// ------------------------------------------------------------------------------
// LEAFLET MAP SETUP
// ------------------------------------------------------------------------------
function initLeafletMap() {
    const centerPoint = [28.6200, 77.2180]; // Central Plaza Hub
    map = L.map('map', {
        center: centerPoint,
        zoom: 14,
        zoomControl: true
    });

    // OpenStreetMap tiles require attribution and do not need an API key.
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap contributors</a>'
    }).addTo(map);

    // Create Layer Groups
    networkLayerGroup = L.layerGroup().addTo(map);
    hospitalLayerGroup = L.layerGroup().addTo(map);
    ambulanceLayerGroup = L.layerGroup().addTo(map);
    emergencyLayerGroup = L.layerGroup().addTo(map);
}

// ------------------------------------------------------------------------------
// API CLIENT HELPERS
// ------------------------------------------------------------------------------
async function apiRequest(endpoint, method = 'GET', body = null) {
    let url = `${API_BASE}${endpoint}`;
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        let response = await fetch(url, options);
        if (!response.ok) {
            // Check fallback URL if running standalone from different port
            if (window.location.protocol === 'file:' || window.location.port !== '8080') {
                url = `${fallbackApiUrl}${endpoint}`;
                response = await fetch(url, options);
            }
        }

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.error || `HTTP error ${response.status}`);
        }
        return data.data !== undefined ? data.data : data;
    } catch (err) {
        // If server is unavailable, provide mock behavior for standalone UI demonstration
        console.warn(`API call ${endpoint} failed. Using client fallback.`, err.message);
        return handleFallbackApi(endpoint, method, body, err.message);
    }
}

function handleFallbackApi(endpoint, method, body, errorMsg) {
    if (endpoint === '/routes/network') {
        return { nodes: FALLBACK_NODES, edges: FALLBACK_EDGES };
    }
    if (endpoint === '/reports/summary') {
        return {
            totalEmergencies: state.emergencies.length,
            activeEmergencies: state.emergencies.filter(e => e.status !== 'RESOLVED').length,
            pendingEmergencies: state.emergencies.filter(e => e.status === 'PENDING').length,
            criticalEmergencies: state.emergencies.filter(e => e.severity === 'CRITICAL').length,
            totalAmbulances: 6,
            availableAmbulances: state.ambulances.filter(a => a.available).length || 4,
            totalHospitals: 4,
            availableBeds: 22,
            completedDispatches: state.dispatches.filter(d => d.status === 'COMPLETED').length,
            activeDispatches: state.dispatches.filter(d => d.status !== 'COMPLETED').length,
            averageResponseTimeMinutes: 5.4,
            emergenciesByType: { 'Cardiac Emergency': 2, 'Road Accident': 2, 'Fire Injury': 1 },
            emergenciesBySeverity: { 'CRITICAL': 2, 'HIGH': 1, 'MEDIUM': 1, 'LOW': 1 }
        };
    }
    throw new Error(errorMsg);
}

// ------------------------------------------------------------------------------
// DATA FETCHING & SYNCHRONIZATION
// ------------------------------------------------------------------------------
async function fetchAllData() {
    try {
        await Promise.all([
            fetchRoadNetwork(),
            fetchEmergencies(),
            fetchAmbulances(),
            fetchHospitals(),
            fetchDispatches(),
            fetchReportsSummary()
        ]);
        populateNodeDropdowns();
        renderMapLayers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function fetchRoadNetwork() {
    try {
        const net = await apiRequest('/routes/network');
        state.roadEdges = net.edges || FALLBACK_EDGES;
        state.roadNodes = {};
        (net.nodes || FALLBACK_NODES).forEach(node => {
            state.roadNodes[node.id] = node;
        });
    } catch (e) {
        state.roadEdges = FALLBACK_EDGES;
        state.roadNodes = {};
        FALLBACK_NODES.forEach(node => {
            state.roadNodes[node.id] = node;
        });
    }
}

async function fetchEmergencies() {
    try {
        const list = await apiRequest('/emergencies');
        state.emergencies = list || [];
        renderEmergencyQueue();
    } catch (e) {
        console.error(e);
    }
}

async function fetchAmbulances() {
    try {
        const list = await apiRequest('/ambulances');
        state.ambulances = list || [];
        renderAmbulancesTable();
    } catch (e) {
        console.error(e);
    }
}

async function fetchHospitals() {
    try {
        const list = await apiRequest('/hospitals');
        state.hospitals = list || [];
        renderHospitalsTable();
    } catch (e) {
        console.error(e);
    }
}

async function fetchDispatches() {
    try {
        const list = await apiRequest('/dispatches');
        state.dispatches = list || [];
        renderDispatchesTable();
    } catch (e) {
        console.error(e);
    }
}

async function fetchReportsSummary() {
    try {
        const summary = await apiRequest('/reports/summary');
        if (summary) {
            updateKpiHeader(summary);
            renderReportsView(summary);
        }
    } catch (e) {
        console.error(e);
    }
}

// ------------------------------------------------------------------------------
// UI RENDERING: EMERGENCY QUEUE
// ------------------------------------------------------------------------------
function renderEmergencyQueue() {
    const listEl = document.getElementById('incident-list');
    const filterSeverity = document.getElementById('filter-severity')?.value || 'ALL';
    const filterStatus = document.getElementById('filter-status')?.value || 'ALL';

    let filtered = [...state.emergencies];

    if (filterSeverity !== 'ALL') {
        filtered = filtered.filter(e => e.severity === filterSeverity);
    }
    if (filterStatus !== 'ALL') {
        filtered = filtered.filter(e => e.status === filterStatus);
    }

    // Sort by Priority: CRITICAL > HIGH > MEDIUM > LOW, then oldest first
    const severityWeight = { 'CRITICAL': 4, 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
    filtered.sort((a, b) => {
        const diff = (severityWeight[b.severity] || 0) - (severityWeight[a.severity] || 0);
        if (diff !== 0) return diff;
        return new Date(a.createdAt) - new Date(b.createdAt);
    });

    document.getElementById('incident-counter').innerText = `${filtered.length} shown`;

    if (filtered.length === 0) {
        listEl.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📡</div>
                <div>No matching emergency incidents.</div>
            </div>`;
        return;
    }

    listEl.innerHTML = filtered.map(emg => {
        const isSelected = state.selectedEmergency && state.selectedEmergency.id === emg.id;
        const stripeClass = `${emg.severity.toLowerCase()}-stripe`;
        const timeAgo = formatTimeAgo(emg.createdAt);
        const nodeName = state.roadNodes[emg.locationNodeId]?.name || emg.locationNodeId;

        return `
            <div class="incident-card ${stripeClass} ${isSelected ? 'active-selected' : ''}" 
                 onclick="selectEmergency(${emg.id})">
                <div class="card-top">
                    <span class="incident-ref">${emg.referenceCode}</span>
                    <span class="severity-pill ${emg.severity.toLowerCase()}">${emg.severity}</span>
                </div>
                <div class="card-title">${emg.emergencyType}</div>
                <div class="card-desc">${escapeHtml(emg.description || 'No description provided.')}</div>
                <div class="card-meta">
                    <div class="card-location">📍 ${emg.locationNodeId} - ${nodeName}</div>
                    <span>${timeAgo}</span>
                </div>
                <div class="card-action-bar">
                    <span class="status-badge ${emg.status.toLowerCase()}">${emg.status}</span>
                    ${emg.status === 'PENDING' ? `
                        <button class="btn-dispatch-inline" onclick="event.stopPropagation(); allocateAmbulance(${emg.id})">
                            ⚡ Allocate Ambulance
                        </button>` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function selectEmergency(id) {
    const emg = state.emergencies.find(e => e.id === id);
    if (!emg) return;

    state.selectedEmergency = emg;
    renderEmergencyQueue();
    updateTriagePanel(emg);

    // Check if there is an active dispatch associated with this emergency
    const relatedDispatch = state.dispatches.find(d => d.emergencyId === emg.id);
    if (relatedDispatch) {
        loadDispatchDetails(relatedDispatch);
    } else {
        clearDispatchVisuals();
    }

    // Pan map to emergency node
    const node = state.roadNodes[emg.locationNodeId];
    if (node && map) {
        map.flyTo([node.latitude, node.longitude], 15, { duration: 1.2 });
    }
}

// ------------------------------------------------------------------------------
// TRIAGE & DISPATCH DETAILS PANEL
// ------------------------------------------------------------------------------
function updateTriagePanel(emg) {
    const titleEl = document.getElementById('triage-title');
    const refEl = document.getElementById('triage-ref');
    const sevEl = document.getElementById('triage-severity');
    const locEl = document.getElementById('triage-location');
    const timeEl = document.getElementById('triage-time');
    const reqFacEl = document.getElementById('triage-required-facility');

    if (!emg) {
        titleEl.innerText = 'Select an Incident';
        refEl.innerText = '--';
        sevEl.innerHTML = '--';
        locEl.innerText = '--';
        timeEl.innerText = '--';
        reqFacEl.innerText = '--';
        return;
    }

    titleEl.innerText = emg.emergencyType;
    refEl.innerText = emg.referenceCode;
    sevEl.innerHTML = `<span class="severity-pill ${emg.severity.toLowerCase()}">${emg.severity}</span>`;
    const node = state.roadNodes[emg.locationNodeId];
    locEl.innerText = `${emg.locationNodeId} (${node ? node.name : ''})`;
    timeEl.innerText = new Date(emg.createdAt).toLocaleTimeString();

    // Determine required facility
    let required = 'General Emergency';
    if (emg.emergencyType === 'Cardiac Emergency') required = 'Cardiac Care / ICU';
    else if (emg.emergencyType === 'Fire Injury') required = 'Burn Care';
    else if (emg.emergencyType === 'Road Accident') required = 'Trauma Care';
    reqFacEl.innerText = required;
}

function loadDispatchDetails(dispatch) {
    state.activeDispatch = dispatch;
    document.getElementById('dispatch-details-section').style.display = 'block';

    document.getElementById('assigned-amb-code').innerText = dispatch.ambulanceId + (dispatch.ambulanceVehicleCode ? ` (${dispatch.ambulanceVehicleCode})` : '');
    document.getElementById('assigned-amb-distance').innerText = `${dispatch.totalDistanceKm ? dispatch.totalDistanceKm.toFixed(2) : '--'} km`;

    document.getElementById('assigned-hosp-name').innerText = dispatch.hospitalName || dispatch.hospitalId;
    document.getElementById('assigned-hosp-loc').innerText = `Location: ${dispatch.hospitalLocation || '--'}`;

    const fullRoute = [...(dispatch.routeToEmergency || []), ...(dispatch.routeToHospital || []).slice(1)];
    document.getElementById('route-path-display').innerText = fullRoute.length ? fullRoute.join(' ➔ ') : 'Calculating...';

    document.getElementById('metric-total-dist').innerText = dispatch.totalDistanceKm ? dispatch.totalDistanceKm.toFixed(1) : '--';
    document.getElementById('metric-eta').innerText = dispatch.estimatedResponseMinutes ? dispatch.estimatedResponseMinutes.toFixed(1) : '--';

    updateLifecycleStepper(dispatch.status);
    drawDispatchRoute(dispatch);
}

function clearDispatchVisuals() {
    state.activeDispatch = null;
    document.getElementById('dispatch-details-section').style.display = 'none';
    if (routePolylineLayer) {
        map.removeLayer(routePolylineLayer);
        routePolylineLayer = null;
    }
    if (simulatedAmbulanceMarker) {
        map.removeLayer(simulatedAmbulanceMarker);
        simulatedAmbulanceMarker = null;
    }
    if (state.simulationInterval) {
        clearInterval(state.simulationInterval);
        state.simulationInterval = null;
    }
}

// ------------------------------------------------------------------------------
// ALLOCATION ENGINE TRIGGER
// ------------------------------------------------------------------------------
async function allocateAmbulance(emergencyId) {
    try {
        showToast('Running Dijkstra shortest path and allocating nearest ambulance...', 'success');
        const dispatch = await apiRequest(`/dispatches/${emergencyId}/allocate`, 'POST');
        showToast('Ambulance successfully allocated!', 'success');

        await Promise.all([
            fetchEmergencies(),
            fetchAmbulances(),
            fetchHospitals(),
            fetchDispatches(),
            fetchReportsSummary()
        ]);

        const updatedEmg = state.emergencies.find(e => e.id === emergencyId);
        if (updatedEmg) {
            selectEmergency(updatedEmg.id);
        }
        loadDispatchDetails(dispatch);
        renderMapLayers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// MAP RENDERING: ROAD NETWORK, NODES, VEHICLES, HOSPITALS
// ------------------------------------------------------------------------------
function renderMapLayers() {
    if (!map) return;

    // 1. Clear existing layers
    networkLayerGroup.clearLayers();
    hospitalLayerGroup.clearLayers();
    ambulanceLayerGroup.clearLayers();
    emergencyLayerGroup.clearLayers();

    // 2. Draw Road Edges (Polylines)
    state.roadEdges.forEach(edge => {
        const u = state.roadNodes[edge.sourceNodeId];
        const v = state.roadNodes[edge.targetNodeId];
        if (u && v) {
            const line = L.polyline([
                [u.latitude, u.longitude],
                [v.latitude, v.longitude]
            ], {
                color: '#243552',
                weight: 3,
                opacity: 0.7,
                smoothFactor: 1
            });
            line.bindTooltip(`${edge.sourceNodeId} ↔ ${edge.targetNodeId} (${edge.distanceKm} km)`, {
                sticky: true,
                className: 'map-tooltip'
            });
            networkLayerGroup.addLayer(line);
        }
    });

    // 3. Draw Intersections (Road Nodes)
    Object.values(state.roadNodes).forEach(node => {
        const icon = L.divIcon({
            className: 'custom-node-icon',
            html: `<span>${node.id}</span>`,
            iconSize: [22, 22],
            iconAnchor: [11, 11]
        });

        const marker = L.marker([node.latitude, node.longitude], { icon });
        marker.bindPopup(`
            <div style="font-family: var(--font-sans); color: #000;">
                <strong>${node.id}: ${node.name}</strong><br>
                Lat: ${node.latitude.toFixed(4)}, Lng: ${node.longitude.toFixed(4)}<br>
                <button onclick="openCreateEmergencyModal('${node.id}')" style="margin-top: 6px; padding: 4px 8px; font-size: 11px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer;">
                    + Report Emergency Here
                </button>
            </div>
        `);
        networkLayerGroup.addLayer(marker);
    });

    // 4. Draw Hospitals
    state.hospitals.forEach(hosp => {
        const node = state.roadNodes[hosp.locationNodeId];
        if (node) {
            const icon = L.divIcon({
                className: 'custom-hosp-icon',
                html: `<span>H</span>`,
                iconSize: [26, 26],
                iconAnchor: [13, 13]
            });
            const marker = L.marker([node.latitude, node.longitude], { icon });
            marker.bindPopup(`
                <div style="color: #000; font-family: var(--font-sans); font-size: 12px;">
                    <strong>${hosp.name}</strong><br>
                    <span>Node: ${hosp.locationNodeId}</span><br>
                    <span>Available Beds: <strong>${hosp.availableBeds} / ${hosp.totalBeds}</strong></span><br>
                    <span>Status: <strong>${hosp.capacityStatus}</strong></span><br>
                    <span style="font-size: 10px; color: #555;">Facilities: ${hosp.facilities}</span>
                </div>
            `);
            hospitalLayerGroup.addLayer(marker);
        }
    });

    // 5. Draw Ambulances
    state.ambulances.forEach(amb => {
        const node = state.roadNodes[amb.currentNodeId];
        if (node) {
            const isAvail = amb.available;
            const icon = L.divIcon({
                className: `custom-amb-icon ${isAvail ? '' : 'busy'}`,
                html: `<span>🚑</span>`,
                iconSize: [26, 26],
                iconAnchor: [13, 13]
            });
            const marker = L.marker([node.latitude, node.longitude], { icon });
            marker.bindPopup(`
                <div style="color: #000; font-family: var(--font-sans); font-size: 12px;">
                    <strong>Ambulance: ${amb.id}</strong> (${amb.vehicleCode})<br>
                    <span>Type: ${amb.ambulanceType}</span><br>
                    <span>Station: ${amb.currentNodeId}</span><br>
                    <span>Status: <strong>${amb.status}</strong> (${isAvail ? 'Available' : 'Busy'})</span><br>
                    <span style="font-size: 10px; color: #555;">Equipment: ${amb.equipment}</span>
                </div>
            `);
            ambulanceLayerGroup.addLayer(marker);
        }
    });

    // 6. Draw Pending Emergencies
    state.emergencies.filter(e => e.status !== 'RESOLVED').forEach(emg => {
        const node = state.roadNodes[emg.locationNodeId];
        if (node) {
            const icon = L.divIcon({
                className: 'custom-emg-icon',
                html: `<span>⚠️</span>`,
                iconSize: [26, 26],
                iconAnchor: [13, 13]
            });
            const marker = L.marker([node.latitude, node.longitude], { icon });
            marker.bindPopup(`
                <div style="color: #000; font-family: var(--font-sans); font-size: 12px;">
                    <strong style="color: #dc2626;">[${emg.severity}] ${emg.emergencyType}</strong><br>
                    <span>Code: ${emg.referenceCode}</span><br>
                    <span>Location: ${emg.locationNodeId}</span><br>
                    <span>Status: ${emg.status}</span><br>
                    <p style="font-size: 11px; margin-top: 4px;">${escapeHtml(emg.description)}</p>
                </div>
            `);
            emergencyLayerGroup.addLayer(marker);
        }
    });
}

// ------------------------------------------------------------------------------
// ROUTE DRAWING & GPS SIMULATION ANIMATION
// ------------------------------------------------------------------------------
function drawDispatchRoute(dispatch) {
    if (routePolylineLayer) {
        map.removeLayer(routePolylineLayer);
    }
    if (simulatedAmbulanceMarker) {
        map.removeLayer(simulatedAmbulanceMarker);
    }

    const pathNodes = [...(dispatch.routeToEmergency || []), ...(dispatch.routeToHospital || []).slice(1)];
    if (!pathNodes || pathNodes.length < 2) return;

    const latLngs = [];
    pathNodes.forEach(nodeId => {
        const node = state.roadNodes[nodeId];
        if (node) latLngs.push([node.latitude, node.longitude]);
    });

    if (latLngs.length < 2) return;

    // Draw neon cyan glowing polyline
    routePolylineLayer = L.polyline(latLngs, {
        color: '#00d2ff',
        weight: 5,
        opacity: 0.9,
        dashArray: '8, 8',
        lineCap: 'round'
    }).addTo(map);

    map.fitBounds(routePolylineLayer.getBounds(), { padding: [50, 50] });

    // Create Simulated Ambulance Marker
    const startPoint = latLngs[0];
    const ambIcon = L.divIcon({
        className: 'custom-amb-icon busy',
        html: `<span>🚨</span>`,
        iconSize: [32, 32],
        iconAnchor: [16, 16]
    });

    simulatedAmbulanceMarker = L.marker(startPoint, { icon: ambIcon, zIndexOffset: 2000 }).addTo(map);
    simulatedAmbulanceMarker.bindTooltip(`Ambulance ${dispatch.ambulanceId} En Route (Simulated)`, {
        permanent: true,
        direction: 'top'
    });
}

function startRouteSimulation() {
    if (!state.activeDispatch) {
        showToast('Please select or create an active dispatch first.', 'error');
        return;
    }

    const dispatch = state.activeDispatch;
    const pathNodes = [...(dispatch.routeToEmergency || []), ...(dispatch.routeToHospital || []).slice(1)];

    if (!pathNodes || pathNodes.length < 2) {
        showToast('Route has insufficient waypoints for simulation.', 'error');
        return;
    }

    if (state.simulationInterval) {
        clearInterval(state.simulationInterval);
    }

    showToast(`Simulation started: Ambulance ${dispatch.ambulanceId} is navigating the route...`, 'success');
    state.currentStepIndex = 0;
    state.routeProgress = 0;

    const latLngPoints = pathNodes.map(id => {
        const node = state.roadNodes[id];
        return [node.latitude, node.longitude];
    });

    // Split simulation into key stages
    const emergencyIndex = (dispatch.routeToEmergency || []).length - 1;

    state.simulationInterval = setInterval(async () => {
        state.routeProgress += 0.08;
        const segmentIndex = Math.floor(state.routeProgress);
        const segmentProgress = state.routeProgress - segmentIndex;

        if (state.routeProgress < latLngPoints.length - 1) {
            const start = latLngPoints[segmentIndex];
            const end = latLngPoints[segmentIndex + 1];
            const nextCoord = [
                start[0] + (end[0] - start[0]) * segmentProgress,
                start[1] + (end[1] - start[1]) * segmentProgress
            ];
            if (simulatedAmbulanceMarker) {
                simulatedAmbulanceMarker.setLatLng(nextCoord);
            }

            if (segmentIndex > state.currentStepIndex) {
                state.currentStepIndex = segmentIndex;
                if (segmentIndex === 1) {
                    await updateDispatchStatus(dispatch.id, 'EN_ROUTE_TO_EMERGENCY', 'Ambulance en route to emergency');
                } else if (segmentIndex === emergencyIndex) {
                    await updateDispatchStatus(dispatch.id, 'ON_SCENE', 'Ambulance arrived on scene');
                    showToast(`🚨 Ambulance ${dispatch.ambulanceId} arrived ON SCENE at ${pathNodes[emergencyIndex]}!`, 'success');
                } else if (segmentIndex === emergencyIndex + 1) {
                    await updateDispatchStatus(dispatch.id, 'EN_ROUTE_TO_HOSPITAL', 'Transporting patient to hospital');
                }
            }
        } else {
            // Reached Destination Hospital
            if (simulatedAmbulanceMarker) {
                simulatedAmbulanceMarker.setLatLng(latLngPoints[latLngPoints.length - 1]);
            }
            clearInterval(state.simulationInterval);
            state.simulationInterval = null;
            await completeActiveDispatch();
            showToast(`🏁 Mission Complete! Patient safely delivered to ${dispatch.hospitalName}.`, 'success');
        }
    }, 100); // Smoothly animate between route waypoints
}

async function stepNextStatus() {
    if (!state.activeDispatch) return;
    const d = state.activeDispatch;
    const progression = [
        'AMBULANCE_ASSIGNED',
        'EN_ROUTE_TO_EMERGENCY',
        'ON_SCENE',
        'EN_ROUTE_TO_HOSPITAL',
        'COMPLETED'
    ];
    const currentIndex = progression.indexOf(d.status);
    if (currentIndex >= 0 && currentIndex < progression.length - 1) {
        const next = progression[currentIndex + 1];
        await updateDispatchStatus(d.id, next, `Manual status advancement to ${next}`);
    }
}

async function completeActiveDispatch() {
    if (!state.activeDispatch) return;
    try {
        await apiRequest(`/dispatches/${state.activeDispatch.id}/complete`, 'POST');
        await fetchAllData();
        const updatedDispatch = state.dispatches.find(d => d.id === state.activeDispatch.id);
        if (updatedDispatch) {
            loadDispatchDetails(updatedDispatch);
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function updateDispatchStatus(dispatchId, status, note) {
    try {
        const updated = await apiRequest(`/dispatches/${dispatchId}/status`, 'PATCH', { status, note });
        state.activeDispatch = updated;
        updateLifecycleStepper(updated.status);

        // Update local ambulance and emergency state in memory as well
        const amb = state.ambulances.find(a => a.id === updated.ambulanceId);
        if (amb && status === 'COMPLETED') amb.available = true;

        const emg = state.emergencies.find(e => e.id === updated.emergencyId);
        if (emg && status === 'COMPLETED') emg.status = 'RESOLVED';

        renderEmergencyQueue();
        renderMapLayers();
    } catch (err) {
        console.error(err);
    }
}

function updateLifecycleStepper(currentStatus) {
    const steps = [
        { id: 'step-created', statuses: ['CREATED', 'AMBULANCE_ASSIGNED', 'EN_ROUTE_TO_EMERGENCY', 'ON_SCENE', 'EN_ROUTE_TO_HOSPITAL', 'COMPLETED'] },
        { id: 'step-assigned', statuses: ['AMBULANCE_ASSIGNED', 'EN_ROUTE_TO_EMERGENCY', 'ON_SCENE', 'EN_ROUTE_TO_HOSPITAL', 'COMPLETED'] },
        { id: 'step-enroute', statuses: ['EN_ROUTE_TO_EMERGENCY', 'ON_SCENE', 'EN_ROUTE_TO_HOSPITAL', 'COMPLETED'] },
        { id: 'step-onscene', statuses: ['ON_SCENE', 'EN_ROUTE_TO_HOSPITAL', 'COMPLETED'] },
        { id: 'step-transport', statuses: ['EN_ROUTE_TO_HOSPITAL', 'COMPLETED'] },
        { id: 'step-completed', statuses: ['COMPLETED'] }
    ];

    steps.forEach((step, idx) => {
        const el = document.getElementById(step.id);
        if (!el) return;
        el.classList.remove('active', 'completed');

        if (step.statuses.includes(currentStatus)) {
            if (currentStatus === step.statuses[0]) {
                el.classList.add('active');
            } else {
                el.classList.add('completed');
            }
        }
    });
}

// ------------------------------------------------------------------------------
// TOP KPI TELEMETRY BAR
// ------------------------------------------------------------------------------
function updateKpiHeader(summary) {
    if (!summary) return;
    document.getElementById('kpi-active-calls').innerText = summary.activeEmergencies || 0;
    document.getElementById('kpi-critical').innerText = summary.criticalEmergencies || 0;

    const totalAmbs = summary.totalAmbulances || 6;
    const busyAmbs = summary.busyAmbulances || 0;
    const loadPercent = Math.round((busyAmbs / totalAmbs) * 100);
    document.getElementById('kpi-operator-load').innerText = `${loadPercent}%`;

    document.getElementById('kpi-available-fleet').innerText = `${summary.availableAmbulances || 0} / ${totalAmbs}`;
}

// ------------------------------------------------------------------------------
// OVERLAY VIEWS: FLEET, HOSPITALS, DISPATCHES, REPORTS
// ------------------------------------------------------------------------------
function renderAmbulancesTable() {
    const tbody = document.getElementById('ambulance-table-body');
    if (!tbody) return;

    tbody.innerHTML = state.ambulances.map(a => `
        <tr>
            <td><strong>${a.id}</strong></td>
            <td><code>${a.vehicleCode}</code></td>
            <td>${a.ambulanceType}</td>
            <td>📍 ${a.currentNodeId}</td>
            <td><span class="status-badge ${a.available ? 'resolved' : 'pending'}">${a.available ? 'AVAILABLE' : 'BUSY'}</span></td>
            <td>${escapeHtml(a.equipment || '--')}</td>
            <td>${a.currentAssignment || '--'}</td>
        </tr>
    `).join('');
}

function renderHospitalsTable() {
    const tbody = document.getElementById('hospital-table-body');
    if (!tbody) return;

    tbody.innerHTML = state.hospitals.map(h => `
        <tr>
            <td><strong>${h.id}</strong></td>
            <td>${h.name}</td>
            <td>📍 ${h.locationNodeId}</td>
            <td><strong style="color: ${h.availableBeds > 0 ? '#34d399' : '#f87171'}">${h.availableBeds} / ${h.totalBeds}</strong></td>
            <td><span class="status-badge ${h.capacityStatus === 'NORMAL' ? 'resolved' : 'pending'}">${h.capacityStatus}</span></td>
            <td>${escapeHtml(h.facilities || '--')}</td>
        </tr>
    `).join('');
}

function renderDispatchesTable() {
    const tbody = document.getElementById('dispatch-table-body');
    if (!tbody) return;

    tbody.innerHTML = state.dispatches.map(d => `
        <tr>
            <td><strong>DISP-${d.id}</strong></td>
            <td><code>${d.emergencyRefCode || d.emergencyId}</code></td>
            <td>🚑 ${d.ambulanceId}</td>
            <td>🏥 ${d.hospitalName || d.hospitalId}</td>
            <td>${d.totalDistanceKm ? d.totalDistanceKm.toFixed(2) + ' km' : '--'}</td>
            <td>${d.estimatedResponseMinutes ? d.estimatedResponseMinutes.toFixed(1) + ' min' : '--'}</td>
            <td><span class="status-badge ${d.status.toLowerCase()}">${d.status}</span></td>
            <td>${new Date(d.assignedAt).toLocaleTimeString()}</td>
        </tr>
    `).join('');
}

function renderReportsView(summary) {
    if (!summary) return;

    document.getElementById('rep-total-emg').innerText = summary.totalEmergencies || 0;
    document.getElementById('rep-active-emg').innerText = summary.activeEmergencies || 0;
    document.getElementById('rep-completed-disp').innerText = summary.completedDispatches || 0;
    document.getElementById('rep-avg-response').innerText = `${summary.averageResponseTimeMinutes || 0} min`;

    // Severity Breakdown Bars
    const sevContainer = document.getElementById('severity-distribution-bars');
    if (sevContainer && summary.emergenciesBySeverity) {
        const total = summary.totalEmergencies || 1;
        const colors = { 'CRITICAL': '#ef4444', 'HIGH': '#f97316', 'MEDIUM': '#eab308', 'LOW': '#38bdf8' };
        sevContainer.innerHTML = Object.entries(summary.emergenciesBySeverity).map(([sev, count]) => {
            const pct = Math.round((count / total) * 100);
            return `
                <div class="distribution-bar">
                    <div class="bar-meta">
                        <span>${sev}</span>
                        <span><strong>${count}</strong> (${pct}%)</span>
                    </div>
                    <div class="bar-track">
                        <div class="bar-fill" style="width: ${pct}%; background: ${colors[sev] || '#fff'};"></div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // Type Breakdown Bars
    const typeContainer = document.getElementById('type-distribution-bars');
    if (typeContainer && summary.emergenciesByType) {
        const total = summary.totalEmergencies || 1;
        typeContainer.innerHTML = Object.entries(summary.emergenciesByType).map(([type, count]) => {
            const pct = Math.round((count / total) * 100);
            return `
                <div class="distribution-bar">
                    <div class="bar-meta">
                        <span>${type}</span>
                        <span><strong>${count}</strong> (${pct}%)</span>
                    </div>
                    <div class="bar-track">
                        <div class="bar-fill" style="width: ${pct}%; background: #00d2ff;"></div>
                    </div>
                </div>
            `;
        }).join('');
    }
}

// ------------------------------------------------------------------------------
// MODAL & FORM HANDLING
// ------------------------------------------------------------------------------
function openCreateEmergencyModal(presetNodeId = null) {
    const modal = document.getElementById('emergency-modal');
    modal.classList.add('open');
    if (presetNodeId) {
        const select = document.getElementById('emg-location-select');
        if (select) select.value = presetNodeId;
    }
}

function closeCreateEmergencyModal() {
    const modal = document.getElementById('emergency-modal');
    modal.classList.remove('open');
}

function selectSeverityForm(sev) {
    document.querySelectorAll('.severity-btn').forEach(b => b.classList.remove('active'));
    const btn = document.querySelector(`.severity-btn[data-sev="${sev}"]`);
    if (btn) btn.classList.add('active');
    document.getElementById('emg-severity-input').value = sev;
}

async function handleEmergencyFormSubmit(e) {
    e.preventDefault();
    const locationNodeId = document.getElementById('emg-location-select').value;
    const emergencyType = document.getElementById('emg-type-select').value;
    const severity = document.getElementById('emg-severity-input').value;
    const description = document.getElementById('emg-desc-input').value;

    if (!locationNodeId) {
        showToast('Please select a valid road intersection', 'error');
        return;
    }

    try {
        const created = await apiRequest('/emergencies', 'POST', {
            locationNodeId,
            emergencyType,
            severity,
            description
        });

        showToast(`Incident ${created.referenceCode} registered into Priority Queue!`, 'success');
        closeCreateEmergencyModal();
        await fetchAllData();
        selectEmergency(created.id);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function populateNodeDropdowns() {
    const select = document.getElementById('emg-location-select');
    if (!select) return;

    select.innerHTML = Object.values(state.roadNodes).map(node => `
        <option value="${node.id}">${node.id} - ${node.name}</option>
    `).join('');
}

// ------------------------------------------------------------------------------
// DEMO PRESETS & BATCH SIMULATION
// ------------------------------------------------------------------------------
async function triggerDemoCardiacEmergency() {
    const sampleNodes = ['N7', 'N6', 'N2', 'N9', 'N12'];
    const randomNode = sampleNodes[Math.floor(Math.random() * sampleNodes.length)];
    try {
        const created = await apiRequest('/emergencies', 'POST', {
            locationNodeId: randomNode,
            emergencyType: 'Cardiac Emergency',
            severity: 'CRITICAL',
            description: 'SIMULATION PRESET: Patient collapsed with acute coronary distress.'
        });
        showToast(`CRITICAL Cardiac Incident triggered at ${randomNode}!`, 'success');
        await fetchAllData();
        selectEmergency(created.id);
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function triggerDemoRoadAccident() {
    const sampleNodes = ['N4', 'N10', 'N12', 'N16', 'N8'];
    const randomNode = sampleNodes[Math.floor(Math.random() * sampleNodes.length)];
    try {
        const created = await apiRequest('/emergencies', 'POST', {
            locationNodeId: randomNode,
            emergencyType: 'Road Accident',
            severity: 'HIGH',
            description: 'SIMULATION PRESET: High speed rear-end crash reported on express link.'
        });
        showToast(`HIGH Road Accident incident triggered at ${randomNode}!`, 'success');
        await fetchAllData();
        selectEmergency(created.id);
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function runBatchAutoDispatch() {
    const pending = state.emergencies.filter(e => e.status === 'PENDING');
    if (pending.length === 0) {
        showToast('No pending emergencies to dispatch.', 'error');
        return;
    }

    showToast(`Dispatching ${pending.length} pending emergencies in priority order...`, 'success');
    for (const emg of pending) {
        try {
            await apiRequest(`/dispatches/${emg.id}/allocate`, 'POST');
        } catch (e) {
            console.warn(`Could not dispatch ${emg.id}: ${e.message}`);
        }
    }
    await fetchAllData();
    showToast('Batch auto-dispatch cycle completed!', 'success');
}

// ------------------------------------------------------------------------------
// EVENT LISTENERS & NAVIGATION
// ------------------------------------------------------------------------------
function setupEventListeners() {
    // Nav Tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    // Filters
    document.getElementById('filter-severity')?.addEventListener('change', renderEmergencyQueue);
    document.getElementById('filter-status')?.addEventListener('change', renderEmergencyQueue);

    // Form
    document.getElementById('emergency-form')?.addEventListener('submit', handleEmergencyFormSubmit);

    // Layer Toggles
    setupLayerToggle('toggle-network', () => networkLayerGroup, networkLayerGroup);
    setupLayerToggle('toggle-ambulances', () => ambulanceLayerGroup, ambulanceLayerGroup);
    setupLayerToggle('toggle-hospitals', () => hospitalLayerGroup, hospitalLayerGroup);
    setupLayerToggle('toggle-incidents', () => emergencyLayerGroup, emergencyLayerGroup);
}

function setupLayerToggle(elementId, getLayer) {
    const toggle = document.getElementById(elementId);
    if (!toggle) return;
    toggle.addEventListener('click', () => {
        const layer = getLayer();
        if (toggle.classList.contains('on')) {
            toggle.classList.remove('on');
            map.removeLayer(layer);
        } else {
            toggle.classList.add('on');
            map.addLayer(layer);
        }
    });
}

function switchTab(tabId) {
    state.activeTab = tabId;
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`.tab-btn[data-tab="${tabId}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    document.querySelectorAll('.overlay-view').forEach(view => view.classList.remove('active'));

    if (tabId === 'map-view') {
        setTimeout(() => { if (map) map.invalidateSize(); }, 150);
    } else {
        const viewEl = document.getElementById(`view-${tabId}`);
        if (viewEl) viewEl.classList.add('active');
    }
}

// ------------------------------------------------------------------------------
// TOAST NOTIFICATIONS & UTILITIES
// ------------------------------------------------------------------------------
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'error' ? '⚠️' : type === 'success' ? '✅' : 'ℹ️'}</span>
        <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function formatTimeAgo(dateString) {
    if (!dateString) return '';
    const diffSeconds = Math.floor((new Date() - new Date(dateString)) / 1000);
    if (diffSeconds < 60) return `${diffSeconds}s ago`;
    const diffMinutes = Math.floor(diffSeconds / 60);
    if (diffMinutes < 60) return `${diffMinutes}m ago`;
    const diffHours = Math.floor(diffMinutes / 60);
    return `${diffHours}h ago`;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[m]);
}
