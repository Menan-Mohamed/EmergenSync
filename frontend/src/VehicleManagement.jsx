import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import './VehicleManagement.css';

// Fix for default marker icon in leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

// API base URLs
const VEHICLE_API_BASE_URL = 'http://localhost:8080/api/responder/vehicle';
const INCIDENT_API_BASE_URL = 'http://localhost:8080/api/dispatcher/incidents';

// Custom icons for different vehicle types using SVG data URLs
const vehicleIcons = {
  MEDICAL: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23E74C3C"/><path d="M10 15H40C41.1046 15 42 15.8954 42 17V35C42 36.1046 41.1046 37 40 37H10C8.89543 37 8 36.1046 8 35V17C8 15.8954 8.89543 15 10 15Z" fill="%23FFFFFF"/><path d="M12 28H20M16 24V32" stroke="%23E74C3C" stroke-width="2" stroke-linecap="round"/><circle cx="14" cy="38" r="3" fill="%23555"/><circle cx="36" cy="38" r="3" fill="%23555"/><rect x="35" y="20" width="5" height="8" fill="%23444"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  FIRE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FF8C00"/><path d="M10 18H40C41.1046 18 42 18.8954 42 20V33C42 34.1046 41.1046 35 40 35H10C8.89543 35 8 34.1046 8 33V20C8 18.8954 8.89543 18 10 18Z" fill="%23FFFFFF"/><circle cx="15" cy="10" r="2" fill="%23FF0000"/><circle cx="20" cy="8" r="2" fill="%23FF0000"/><circle cx="25" cy="9" r="2" fill="%23FF0000"/><circle cx="30" cy="8" r="2" fill="%23FF0000"/><circle cx="35" cy="10" r="2" fill="%23FF0000"/><circle cx="14" cy="37" r="3" fill="%23555"/><circle cx="36" cy="37" r="3" fill="%23555"/><rect x="18" y="22" width="14" height="8" fill="%23FFD700" opacity="0.7"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  POLICE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%230047AB"/><path d="M10 18H40C41.1046 18 42 18.8954 42 20V33C42 34.1046 41.1046 35 40 35H10C8.89543 35 8 34.1046 8 33V20C8 18.8954 8.89543 18 10 18Z" fill="%23FFFFFF"/><rect x="16" y="8" width="8" height="8" fill="%23FF0000" rx="1"/><rect x="26" y="8" width="8" height="8" fill="%23FF0000" rx="1"/><rect x="18" y="10" width="4" height="4" fill="%230047AB"/><rect x="28" y="10" width="4" height="4" fill="%230047AB"/><circle cx="14" cy="37" r="3" fill="%23555"/><circle cx="36" cy="37" r="3" fill="%23555"/><path d="M22 22H28M25 19V25" stroke="%230047AB" stroke-width="1.5" stroke-linecap="round"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
};

// Custom icons for different incident types with emojis
const incidentIcons = {
  MEDICAL: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FF1744"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28" font-family="Arial">🩸</text></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  FIRE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FF6F00"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28" font-family="Arial">🔥</text></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  POLICE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FFD600"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28" font-family="Arial">🚨</text></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
};

// Vehicle markers component that displays all vehicles on the map
function VehicleMarkers({ vehicles }) {
  const vehicleEmojis = {
    MEDICAL: '🚑',
    FIRE: '🚒',
    POLICE: '🚓',
  };

  if (!vehicles || vehicles.length === 0) {
    console.log('No vehicles to display');
    return null;
  }

  console.log('Rendering VehicleMarkers with', vehicles.length, 'vehicles');

  return vehicles.map((vehicle) => {
    console.log('Processing vehicle:', vehicle);

    if (!vehicle) {
      console.warn('Null vehicle');
      return null;
    }

    const lat = parseFloat(vehicle.latitude);
    const lng = parseFloat(vehicle.longitude);

    console.log(`Vehicle ${vehicle.id}: lat=${lat}, lng=${lng}, type=${vehicle.type}`);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn(`Invalid coordinates for vehicle ${vehicle.id}:`, { latitude: vehicle.latitude, longitude: vehicle.longitude });
      return null;
    }

    return (
      <Marker
        key={vehicle.id}
        position={[lat, lng]}
        icon={vehicleIcons[vehicle.type] || vehicleIcons.POLICE}
      >
        <Popup>
          <div className="marker-popup">
            <strong>{vehicleEmojis[vehicle.type] || '🚗'} {vehicle.type}</strong>
            <p>ID: {vehicle.id}</p>
            <p>Status: {vehicle.status}</p>
            <p>Responder: {vehicle.responderId}</p>
          </div>
        </Popup>
      </Marker>
    );
  });
}

// Incident markers component that displays all incidents on the map
function IncidentMarkers({ incidents }) {
  const incidentEmojis = {
    MEDICAL: '🩸',
    FIRE: '🔥',
    POLICE: '🚨',
  };

  if (!incidents || incidents.length === 0) {
    console.log('No incidents to display');
    return null;
  }

  console.log('Rendering IncidentMarkers with', incidents.length, 'incidents');

  return incidents.map((incident, index) => {
    console.log('Processing incident:', incident);

    if (!incident) {
      console.warn('Null incident');
      return null;
    }

    const lat = parseFloat(incident.latitude);
    const lng = parseFloat(incident.longitude);

    console.log(`Incident ${index}: lat=${lat}, lng=${lng}, type=${incident.type}`);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn(`Invalid coordinates for incident ${index}:`, { latitude: incident.latitude, longitude: incident.longitude });
      return null;
    }

    return (
      <Marker
        key={`incident-${index}`}
        position={[lat, lng]}
        icon={incidentIcons[incident.type] || incidentIcons.POLICE}
      >
        <Popup>
          <div className="marker-popup">
            <strong>{incidentEmojis[incident.type] || '📍'} {incident.type} Incident</strong>
            <p>Description: {incident.description}</p>
            <p>Severity: {incident.severity || 'N/A'}</p>
            <p>Status: {incident.status || 'Reported'}</p>
          </div>
        </Popup>
      </Marker>
    );
  });
}

// Location selection marker component
function LocationSelectMarker({ onLocationSelect, selectedLocation }) {
  useMapEvents({
    click(e) {
      onLocationSelect({
        latitude: e.latlng.lat,
        longitude: e.latlng.lng,
      });
    },
  });

  return selectedLocation ? (
    <Marker
      position={[selectedLocation.latitude, selectedLocation.longitude]}
      icon={
        new L.Icon({
          iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="25" cy="25" r="20" fill="%23ED3030" opacity="0.3"/><circle cx="25" cy="25" r="8" fill="%23ED3030"/></svg>',
          iconSize: [50, 50],
          iconAnchor: [25, 25],
        })
      }
    >
      <Popup>Selected Location for New Unit</Popup>
    </Marker>
  ) : null;
}

// Main Vehicle Management component
function VehicleManagement() {
  const [vehicles, setVehicles] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    type: 'MEDICAL',
    responderId: '',
  });

  const [editingVehicle, setEditingVehicle] = useState(null);
  const [updateLocation, setUpdateLocation] = useState(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);

  // Fetch all vehicles and incidents from database on component mount
  useEffect(() => {
    fetchVehicles();
    fetchIncidents();
    fetchAssignments();
  }, []);

  const fetchVehicles = async () => {
    try {
      setError('');
      const response = await fetch(`${VEHICLE_API_BASE_URL}/all`);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch vehicles: ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Fetched vehicles:', data);
      console.log('Vehicle count:', data ? data.length : 0);
      
      // Log first vehicle structure for debugging
      if (data && data.length > 0) {
        console.log('First vehicle structure:', JSON.stringify(data[0], null, 2));
      }
      
      setVehicles(data || []);
    } catch (err) {
      setError(`Error loading vehicles: ${err.message}`);
      console.error('Fetch vehicles error:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchIncidents = async () => {
    try {
      const response = await fetch(INCIDENT_API_BASE_URL);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch incidents: ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Fetched incidents:', data);
      console.log('Incident count:', data ? data.length : 0);
      
      // Log first incident structure for debugging
      if (data && data.length > 0) {
        console.log('First incident structure:', JSON.stringify(data[0], null, 2));
      }
      
      setIncidents(data || []);
    } catch (err) {
      console.error('Fetch incidents error:', err);
      // Don't set global error, incidents are optional
    }
  };

  const fetchAssignments = async () => {
    try {
      const response = await fetch('/api/assignments/all');
      if (!response.ok) {
        throw new Error(`Failed to fetch assignments: ${response.statusText}`);
      }
      const data = await response.json();
      console.log('Fetched assignments:', data);
      setAssignments(data || []);
    } catch (err) {
      console.error('Fetch assignments error:', err);
    }
  };

  // Helper to determine if an incident is solved based on assignments
  const isIncidentSolved = (incident) => {
    if (!incident) return false;
    const incidentId = incident.id ?? incident.incidentId ?? null;
    if (incidentId == null) return false;
    return assignments.some((a) => {
      if (a == null) return false;
      // assignment.incidentId may be number or string
      return String(a.incidentId) === String(incidentId) && a.solvedAt;
    });
  };

  const unsolvedIncidents = incidents.filter((inc) => !isIncidentSolved(inc));
  const solvedIncidents = incidents.filter((inc) => isIncidentSolved(inc));

  // Get incidents matching the vehicle type for modal display
  const getIncidentsForVehicle = (vehicle) => {
    if (!vehicle) return [];
    return unsolvedIncidents.filter((inc) => inc.type === vehicle.type);
  };

  const handleLocationSelect = (location) => {
    setSelectedLocation(location);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCreateVehicle = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.responderId.trim()) {
      setError('Please enter a Responder ID');
      return;
    }

    if (!selectedLocation) {
      setError('Please select a location on the map');
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        type: formData.type,
        responderId: parseInt(formData.responderId),
        longitude: selectedLocation.longitude,
        latitude: selectedLocation.latitude,
      };

      console.log('Sending payload:', JSON.stringify(payload, null, 2));

      const response = await fetch(VEHICLE_API_BASE_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);

      const contentType = response.headers.get('content-type');
      console.log('Content-Type:', contentType);

      if (!response.ok) {
        // Try to parse as JSON, fallback to text if not valid JSON
        let errorMessage = `HTTP ${response.status}`;
        
        try {
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.message || JSON.stringify(errorData);
          } else {
            const errorText = await response.text();
            errorMessage = errorText.substring(0, 200); // Limit length
          }
        } catch (parseErr) {
          console.error('Error parsing response:', parseErr);
        }

        throw new Error(`Failed to create vehicle: ${errorMessage}`);
      }

      const responseText = await response.text();
      console.log('Response text:', responseText);

      if (!responseText) {
        throw new Error('Empty response from server');
      }

      const newVehicle = JSON.parse(responseText);
      setVehicles((prev) => [...prev, newVehicle]);

      // Reset form
      setFormData({
        type: 'MEDICAL',
        responderId: '',
      });
      setSelectedLocation(null);
      setError('');
    } catch (err) {
      setError(`Error creating vehicle: ${err.message}`);
      console.error('Create vehicle error:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteVehicle = (vehicleId) => {
    setVehicles((prev) => prev.filter((v) => v.id !== vehicleId));
  };

  const handleEditVehicleLocation = (vehicle) => {
    setEditingVehicle(vehicle);
    setUpdateLocation(null);
  };

  const handleUpdateLocationSelect = (location) => {
    setUpdateLocation(location);
  };

  const handleCancelUpdate = () => {
    setEditingVehicle(null);
    setUpdateLocation(null);
    setUpdatingLocation(false);
  };

  const handleSubmitLocationUpdate = async () => {
    if (!editingVehicle || !updateLocation) {
      setError('Please select a location');
      return;
    }

    setUpdatingLocation(true);
    setError('');

    try {
      const url = `${VEHICLE_API_BASE_URL}/${editingVehicle.id}/location?latitude=${updateLocation.latitude}&longitude=${updateLocation.longitude}`;
      const response = await fetch(url, {
        method: 'PUT',
      });

      if (!response.ok) {
        throw new Error(`Failed to update location: ${response.statusText}`);
      }

      // Update vehicle in state
      setVehicles((prev) =>
        prev.map((v) =>
          v.id === editingVehicle.id
            ? { ...v, latitude: updateLocation.latitude, longitude: updateLocation.longitude }
            : v
        )
      );

      handleCancelUpdate();
    } catch (err) {
      setError(`Error updating location: ${err.message}`);
      console.error('Update location error:', err);
    } finally {
      setUpdatingLocation(false);
    }
  };

  return (
    <div className="vehicle-management">
      {/* Left Side - Map */}
      <div className="map-section">
        <div className="map-header">
          <h2>Vehicle Dispatch Map</h2>
          <p className="vehicle-count">Active Units: {vehicles.length}</p>
        </div>
        {loading ? (
          <div className="loading-spinner">Loading vehicles...</div>
        ) : (
          <MapContainer
            center={[26.8206, 30.8025]}
            zoom={6}
            scrollWheelZoom={true}
            className="management-map"
            key="map-container"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {vehicles && vehicles.length > 0 && <VehicleMarkers vehicles={vehicles} />}
            {unsolvedIncidents && unsolvedIncidents.length > 0 && (
              <IncidentMarkers incidents={unsolvedIncidents} />
            )}
            <LocationSelectMarker
              onLocationSelect={handleLocationSelect}
              selectedLocation={selectedLocation}
            />
          </MapContainer>
        )}
      </div>

      {/* Right Side - Form */}
      <div className="form-section">
        <div className="form-container">
          <h1>Create Vehicle Unit</h1>

          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleCreateVehicle}>
            <div className="form-group">
              <label htmlFor="responderId">Responder ID *</label>
              <input
                type="number"
                id="responderId"
                name="responderId"
                value={formData.responderId}
                onChange={handleInputChange}
                placeholder="e.g., 1, 2, 3"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="type">Vehicle Type *</label>
              <select
                id="type"
                name="type"
                value={formData.type}
                onChange={handleInputChange}
                required
              >
                <option value="MEDICAL">🚑 Medical</option>
                <option value="FIRE">🚒 Fire</option>
                <option value="POLICE">🚓 Police</option>
              </select>
            </div>

            {selectedLocation && (
              <div className="location-info">
                <p className="location-label">Selected Location:</p>
                <p className="location-coords">
                  {selectedLocation.latitude.toFixed(4)}°, {selectedLocation.longitude.toFixed(4)}°
                </p>
              </div>
            )}

            <button
              type="submit"
              className="create-button"
              disabled={!selectedLocation || submitting}
            >
              {submitting ? 'Creating...' : 'Create Unit'}
            </button>
          </form>

          {vehicles.length > 0 && (
            <div className="vehicles-list">
              <h3>Active Vehicles ({vehicles.length})</h3>
              <div className="list-content">
                {vehicles.map((vehicle) => (
                  <div
                    key={vehicle.id}
                    className="vehicle-item"
                    onClick={() => handleEditVehicleLocation(vehicle)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="vehicle-icon" data-type={vehicle.type.toLowerCase()}>
                      {vehicle.type === 'MEDICAL' && '🚑'}
                      {vehicle.type === 'FIRE' && '🚒'}
                      {vehicle.type === 'POLICE' && '🚓'}
                    </div>
                    <div className="vehicle-info">
                      <p className="vehicle-id">Unit #{vehicle.id}</p>
                      <p className="vehicle-status">{vehicle.status}</p>
                    </div>
                    <button
                      className="delete-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteVehicle(vehicle.id);
                      }}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
          {/* Assignments Section */}
          <div className="assignments-section">
            <h3>Assignments {assignments && assignments.length > 0 ? `(${assignments.length})` : '(0)'}</h3>
            <div className="assignments-list">
              {assignments && assignments.length > 0 ? (
                assignments.map((a, idx) => {
                  const assignedAt = a.assignedAt ? new Date(a.assignedAt).toLocaleString() : 'N/A';
                  const solvedAt = a.solvedAt ? new Date(a.solvedAt).toLocaleString() : null;
                  return (
                    <div key={`assign-${idx}`} className="assignment-item">
                      <div className="assignment-info">
                        <p><strong>Vehicle:</strong> #{a.vehicleId} &nbsp; <strong>Incident:</strong> #{a.incidentId}</p>
                        <p><strong>Assigned:</strong> {assignedAt}</p>
                        <p>
                          <strong>Status:</strong>{' '}
                          {solvedAt ? (
                            <span className="solved">Solved at {solvedAt}</span>
                          ) : (
                            <span className="in-progress">In Progress</span>
                          )}
                        </p>
                      </div>
                    </div>
                  );
                })
              ) : (
                <p className="no-assignments">No assignments found</p>
              )}
            </div>
          </div>
          {/* Solved Incidents Section */}
          <div className="vehicles-list solved-incidents">
            <h3>Solved Incidents {solvedIncidents && solvedIncidents.length > 0 ? `(${solvedIncidents.length})` : '(0)'}</h3>
            <div className="list-content">
              {solvedIncidents && solvedIncidents.length > 0 ? (
                solvedIncidents.map((inc) => {
                  const incId = inc.id ?? inc.incidentId ?? 'N/A';
                  const solvedAssignment = assignments.find((a) => String(a.incidentId) === String(incId) && a.solvedAt);
                  const solvedAt = solvedAssignment && solvedAssignment.solvedAt ? new Date(solvedAssignment.solvedAt).toLocaleString() : 'N/A';
                  return (
                    <div key={`solved-${incId}`} className="vehicle-item">
                      <div className="vehicle-icon" data-type={inc.type ? inc.type.toLowerCase() : 'police'}>
                        {inc.type === 'MEDICAL' && '🩸'}
                        {inc.type === 'FIRE' && '🔥'}
                        {inc.type === 'POLICE' && '🚨'}
                      </div>
                      <div className="vehicle-info">
                        <p className="vehicle-id">Incident #{incId}</p>
                        <p className="vehicle-status">{inc.description || 'No description'}</p>
                        <p className="vehicle-status">Solved: {solvedAt}</p>
                      </div>
                    </div>
                  );
                })
              ) : (
                <p className="no-assignments">No solved incidents</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Location Update Modal */}
      {editingVehicle && (
        <div className="modal-overlay" onClick={handleCancelUpdate}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Update Location - Unit #{editingVehicle.id}</h2>
              <button className="modal-close" onClick={handleCancelUpdate}>×</button>
            </div>

            <div className="modal-body">
              <p className="modal-instruction">Click on the map to select a new location</p>
              {editingVehicle && (
                <MapContainer
                  center={[updateLocation?.latitude || parseFloat(editingVehicle.latitude) || 26.8206, updateLocation?.longitude || parseFloat(editingVehicle.longitude) || 30.8025]}
                  zoom={6}
                  scrollWheelZoom={true}
                  className="modal-map"
                  key={`modal-map-${editingVehicle.id}`}
                >
                  <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  />
                  {/* Show filtered incidents matching vehicle type */}
                  {getIncidentsForVehicle(editingVehicle).length > 0 && (
                    <IncidentMarkers incidents={getIncidentsForVehicle(editingVehicle)} />
                  )}
                  <LocationSelectMarker
                    onLocationSelect={handleUpdateLocationSelect}
                    selectedLocation={updateLocation}
                  />
                </MapContainer>
              )}

              {updateLocation && (
                <div className="location-info" style={{ marginTop: '12px', flexShrink: 0 }}>
                  <p className="location-label">Selected Location:</p>
                  <p className="location-coords">
                    {updateLocation.latitude.toFixed(4)}°, {updateLocation.longitude.toFixed(4)}°
                  </p>
                </div>
              )}

              {error && <div className="error-message">{error}</div>}
            </div>

            <div className="modal-footer">
              <button
                className="modal-btn modal-btn-cancel"
                onClick={handleCancelUpdate}
                disabled={updatingLocation}
              >
                Cancel
              </button>
              <button
                className="modal-btn modal-btn-primary"
                onClick={handleSubmitLocationUpdate}
                disabled={!updateLocation || updatingLocation}
              >
                {updatingLocation ? 'Updating...' : 'Update Location'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default VehicleManagement;
