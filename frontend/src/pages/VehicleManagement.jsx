import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import LiveMap from './Livemap';
import {
  fetchAssignments,
  createVehicle,
  updateVehicleLocation
} from '../services/Service';
import './VehicleManagement.css';
import useVehicleSocket from '../services/VehicleSocket';
import useIncidentSocket from '../services/IncidentSocket';

function VehicleManagement() {

  const vehicles = useVehicleSocket();
  const incidents = useIncidentSocket();

  const [assignments, setAssignments] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState(null);


  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    type: 'MEDICAL',
    responderId: '',
  });

  const [editingVehicle, setEditingVehicle] = useState(null);
  const [updateLocation, setUpdateLocation] = useState(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);

  // Get auth user (may be null)
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user?.token) return;

    const loadData = async () => {
      try {
        setError('');

        const [assignmentsData] = await Promise.all([
          fetchAssignments(user.token),
        ]);

        setAssignments(assignmentsData);
      } catch (err) {
        console.error(err);
        setError('Failed to load admin data');
      }
    };

    loadData();
  }, [user?.token, incidents, vehicles]);


  // Helper to determine if an incident is solved based on assignments
  const isIncidentSolved = (incident) => {
    return incident.status === 'RESOLVED';
  };

  // const unsolvedIncidents = incidents.filter((inc) => !isIncidentSolved(inc));
  const solvedIncidents = incidents.filter((inc) => isIncidentSolved(inc));


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

      const token = user && user.token ? user.token : null;
      const newVehicle = await createVehicle(payload, token);

      console.log('Created new vehicle:', newVehicle);

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

  const handleDeleteVehicle = async (vehicleId) => {
    console.log('Delete vehicle not implemented yet:', vehicleId);
  };

  const handleEditVehicleLocation = (vehicle) => {
    setEditingVehicle(vehicle);
    setUpdateLocation(null);
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

    const token = user && user.token ? user.token : null;

    try {
      // Pass user.token to the updateVehicleLocation function
      await updateVehicleLocation(
        editingVehicle.id,
        updateLocation.latitude,
        updateLocation.longitude,
        token
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
      <LiveMap
        setLocation={true}
        selectedLocation={selectedLocation}
        onLocationSelect={handleLocationSelect}
      />

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
          <div className="vehicles-list">
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
              <div style={{ flexShrink: 0 }}>
                <p className="modal-instruction">Enter new coordinates for this unit</p>
              </div>

              <div className="modal-inputs-row">
                <div className="modal-input-group">
                  <label className="modal-label">Latitude *</label>
                  <input
                    type="number"
                    step="0.0001"
                    placeholder="e.g., 26.8206"
                    value={updateLocation?.latitude || ''}
                    onChange={(e) => {
                      const lat = parseFloat(e.target.value);
                      if (!isNaN(lat)) {
                        setUpdateLocation({
                          latitude: lat,
                          longitude: updateLocation?.longitude || 0,
                        });
                      }
                    }}
                    className="modal-input"
                  />
                </div>
                <div className="modal-input-group">
                  <label className="modal-label">Longitude *</label>
                  <input
                    type="number"
                    step="0.0001"
                    placeholder="e.g., 30.8025"
                    value={updateLocation?.longitude || ''}
                    onChange={(e) => {
                      const lng = parseFloat(e.target.value);
                      if (!isNaN(lng)) {
                        setUpdateLocation({
                          latitude: updateLocation?.latitude || 0,
                          longitude: lng,
                        });
                      }
                    }}
                    className="modal-input"
                  />
                </div>
              </div>

              {updateLocation && (
                <div className="location-info" style={{ flexShrink: 0 }}>
                  <p className="location-label">Current Selection:</p>
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