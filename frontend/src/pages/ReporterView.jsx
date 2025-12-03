import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './ReporterView.css';

// Fix for default marker icon in leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

function LocationMarker({ onLocationSelect, location }) {
  // useMapEvents gives us access to the internal map instance so we can
  // both listen for clicks and programmatically move the view when a
  // location is selected elsewhere.
  const map = useMapEvents({
    click(e) {
      onLocationSelect({
        latitude: e.latlng.lat,
        longitude: e.latlng.lng,
      });
    },
  });

  // When the location prop changes (user selects a location), center the map
  // on that location so the marker is visible to the user.
  useEffect(() => {
    if (location && map) {
      map.setView([location.latitude, location.longitude], map.getZoom());
    }
  }, [location, map]);

  return location ? (
    <Marker position={[location.latitude, location.longitude]}>
      <Popup>
        Location: {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
      </Popup>
    </Marker>
  ) : null;
}

function ReporterView() {
  const [formData, setFormData] = useState({
    description: '',
    type: 'MEDICAL',
    severity: 'medium',
    location: null,
  });

  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleLocationSelect = (location) => {
    setFormData((prev) => ({
      ...prev,
      location,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.description.trim()) {
      setError('Please provide a description');
      return;
    }

    if (!formData.location) {
      setError('Please select a location on the map');
      return;
    }

    setLoading(true);

    try {
      // Map severity string to integer as expected by backend IncidentDTO
      const mapSeverityToInt = (severityStr) => {
        switch ((severityStr || '').toLowerCase()) {
          case 'low': return 1;
          case 'medium': return 2;
          case 'high': return 3;
          case 'critical': return 4;
          default: return 2; // default -> medium
        }
      };

      const payload = {
        description: formData.description,
        type: formData.type,
        // IncidentDTO expects an int severity
        severity: mapSeverityToInt(formData.severity),
        // backend DTO defines longitude, latitude as Doubles
        longitude: formData.location.longitude,
        latitude: formData.location.latitude,
      };

      // Send to backend controller: POST /api/dispatcher/incidents
      const response = await fetch('http://localhost:8080/api/dispatcher/incidents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`Server error: ${response.statusText}`);
      }

      const data = await response.json();
      console.log('Incident reported successfully:', data);

      // Reset form
      setFormData({
        description: '',
        type: 'MEDICAL',
        severity: 'medium',
        location: null,
      });

      setSubmitted(true);
      setTimeout(() => setSubmitted(false), 3000);
    } catch (err) {
      setError(err.message || 'Failed to submit incident report');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="incident-form-container">
      <div className="form-wrapper">
        <h1>Report an Incident</h1>

        {submitted && (
          <div className="success-message">
            ✓ Incident reported successfully!
          </div>
        )}

        {error && (
          <div className="error-message">
            ✗ {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Describe the incident in detail..."
              rows="5"
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="type">Incident Type *</label>
              <select
                id="type"
                name="type"
                value={formData.type}
                onChange={handleInputChange}
                required
              >
                <option value="MEDICAL">Medical</option>
                <option value="FIRE">Fire</option>
                <option value="POLICE">Police</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="severity">Severity *</label>
              <select
                id="severity"
                name="severity"
                value={formData.severity}
                onChange={handleInputChange}
                required
              >
                <option value="low">Low</option>
                <option value="medium">Medium</option>
                <option value="high">High</option>
                <option value="critical">Critical</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Location * (Click on map to select)</label>
            {formData.location && (
              <div className="location-display">
                Selected: {formData.location.latitude.toFixed(4)}, {formData.location.longitude.toFixed(4)}
              </div>
            )}
            <div className="map-container">
              <MapContainer center={[26.8206, 30.8025]} zoom={6} scrollWheelZoom={true} style={{ height: '100%', width: '100%' }}>
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                <LocationMarker
                  onLocationSelect={handleLocationSelect}
                  location={formData.location}
                />
              </MapContainer>
            </div>
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? 'Submitting...' : 'Report Incident'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default ReporterView;