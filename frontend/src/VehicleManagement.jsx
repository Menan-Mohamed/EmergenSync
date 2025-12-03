import React, { useState } from 'react';
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

// Custom icons for different vehicle types using SVG data URLs
const vehicleIcons = {
  medical: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23E74C3C"/><path d="M10 15H40C41.1046 15 42 15.8954 42 17V35C42 36.1046 41.1046 37 40 37H10C8.89543 37 8 36.1046 8 35V17C8 15.8954 8.89543 15 10 15Z" fill="%23FFFFFF"/><path d="M12 28H20M16 24V32" stroke="%23E74C3C" stroke-width="2" stroke-linecap="round"/><circle cx="14" cy="38" r="3" fill="%23555"/><circle cx="36" cy="38" r="3" fill="%23555"/><rect x="35" y="20" width="5" height="8" fill="%23444"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  fire: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FF8C00"/><path d="M10 18H40C41.1046 18 42 18.8954 42 20V33C42 34.1046 41.1046 35 40 35H10C8.89543 35 8 34.1046 8 33V20C8 18.8954 8.89543 18 10 18Z" fill="%23FFFFFF"/><circle cx="15" cy="10" r="2" fill="%23FF0000"/><circle cx="20" cy="8" r="2" fill="%23FF0000"/><circle cx="25" cy="9" r="2" fill="%23FF0000"/><circle cx="30" cy="8" r="2" fill="%23FF0000"/><circle cx="35" cy="10" r="2" fill="%23FF0000"/><circle cx="14" cy="37" r="3" fill="%23555"/><circle cx="36" cy="37" r="3" fill="%23555"/><rect x="18" y="22" width="14" height="8" fill="%23FFD700" opacity="0.7"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  police: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%230047AB"/><path d="M10 18H40C41.1046 18 42 18.8954 42 20V33C42 34.1046 41.1046 35 40 35H10C8.89543 35 8 34.1046 8 33V20C8 18.8954 8.89543 18 10 18Z" fill="%23FFFFFF"/><rect x="16" y="8" width="8" height="8" fill="%23FF0000" rx="1"/><rect x="26" y="8" width="8" height="8" fill="%23FF0000" rx="1"/><rect x="18" y="10" width="4" height="4" fill="%230047AB"/><rect x="28" y="10" width="4" height="4" fill="%230047AB"/><circle cx="14" cy="37" r="3" fill="%23555"/><circle cx="36" cy="37" r="3" fill="%23555"/><path d="M22 22H28M25 19V25" stroke="%230047AB" stroke-width="1.5" stroke-linecap="round"/></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
};

// Vehicle markers component that displays all vehicles on the map
function VehicleMarkers({ vehicles }) {
  const vehicleEmojis = {
    medical: '🚑',
    fire: '🚒',
    police: '🚓',
  };

  return vehicles.map((vehicle) => (
    <Marker
      key={vehicle.id}
      position={[vehicle.latitude, vehicle.longitude]}
      icon={vehicleIcons[vehicle.type.toLowerCase()] || vehicleIcons.police}
    >
      <Popup>
        <div className="marker-popup">
          <strong>{vehicleEmojis[vehicle.type.toLowerCase()] || '🚗'} {vehicle.type}</strong>
          <p>Unit: {vehicle.unitId}</p>
          <p>Status: {vehicle.status}</p>
        </div>
      </Popup>
    </Marker>
  ));
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
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [formData, setFormData] = useState({
    unitId: '',
    type: 'Medical',
    status: 'Available',
  });

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

  const handleCreateVehicle = (e) => {
    e.preventDefault();

    if (!formData.unitId.trim()) {
      alert('Please enter a Unit ID');
      return;
    }

    if (!selectedLocation) {
      alert('Please select a location on the map');
      return;
    }

    const newVehicle = {
      id: Date.now(),
      ...formData,
      latitude: selectedLocation.latitude,
      longitude: selectedLocation.longitude,
    };

    setVehicles((prev) => [...prev, newVehicle]);

    // Reset form
    setFormData({
      unitId: '',
      type: 'Medical',
      status: 'Available',
    });
    setSelectedLocation(null);
  };

  const handleDeleteVehicle = (vehicleId) => {
    setVehicles((prev) => prev.filter((v) => v.id !== vehicleId));
  };

  return (
    <div className="vehicle-management">
      {/* Left Side - Map */}
      <div className="map-section">
        <div className="map-header">
          <h2>Vehicle Dispatch Map</h2>
          <p className="vehicle-count">Active Units: {vehicles.length}</p>
        </div>
        <MapContainer
          center={[26.8206, 30.8025]}
          zoom={6}
          scrollWheelZoom={true}
          className="management-map"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <VehicleMarkers vehicles={vehicles} />
          <LocationSelectMarker
            onLocationSelect={handleLocationSelect}
            selectedLocation={selectedLocation}
          />
        </MapContainer>
      </div>

      {/* Right Side - Form */}
      <div className="form-section">
        <div className="form-container">
          <h1>Create Vehicle Unit</h1>

          <form onSubmit={handleCreateVehicle}>
            <div className="form-group">
              <label htmlFor="unitId">Unit ID *</label>
              <input
                type="text"
                id="unitId"
                name="unitId"
                value={formData.unitId}
                onChange={handleInputChange}
                placeholder="e.g., MED-001, FIRE-05"
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
                <option value="Medical">🚑 Medical</option>
                <option value="Fire">🚒 Fire</option>
                <option value="Police">🚓 Police</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="status">Status *</label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleInputChange}
                required
              >
                <option value="Available">Available</option>
                <option value="On Mission">On Mission</option>
                <option value="Returning">Returning</option>
                <option value="Maintenance">Maintenance</option>
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
              disabled={!selectedLocation}
            >
              Create Unit
            </button>
          </form>

          {vehicles.length > 0 && (
            <div className="vehicles-list">
              <h3>Active Vehicles ({vehicles.length})</h3>
              <div className="list-content">
                {vehicles.map((vehicle) => (
                  <div key={vehicle.id} className="vehicle-item">
                    <div className="vehicle-icon" data-type={vehicle.type.toLowerCase()}>
                      {vehicle.type === 'Medical' && '🚑'}
                      {vehicle.type === 'Fire' && '🚒'}
                      {vehicle.type === 'Police' && '🚓'}
                    </div>
                    <div className="vehicle-info">
                      <p className="vehicle-id">{vehicle.unitId}</p>
                      <p className="vehicle-status">{vehicle.status}</p>
                    </div>
                    <button
                      className="delete-btn"
                      onClick={() => handleDeleteVehicle(vehicle.id)}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default VehicleManagement;
