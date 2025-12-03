import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import './IncidentForm.css';

// Fix for default marker icon in leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

function LocationMarker({ onLocationSelect, location }) {
  useMapEvents({
    click(e) {
      onLocationSelect({
        latitude: e.latlng.lat,
        longitude: e.latlng.lng,
      });
    },
  });

  return location ? (
    <Marker position={[location.latitude, location.longitude]}>
      <Popup>
        Location: {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
      </Popup>
    </Marker>
  ) : null;
}

function IncidentForm() {
  const [formData, setFormData] = useState({
    description: '',
    type: 'medical',
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
      const payload = {
        description: formData.description,
        type: formData.type,
        severity: formData.severity,
        latitude: formData.location.latitude,
        longitude: formData.location.longitude,
      };

      // Send to backend
      const response = await fetch('http://localhost:5000/api/incidents', {
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
        type: 'medical',
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
                <option value="medical">Medical</option>
                <option value="fire">Fire</option>
                <option value="police">Police</option>
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

export default IncidentForm;
