import React, { useState, useEffect, useContext } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import './Livemap.css';
import { AuthContext } from '../auth/AuthContext';
import {
  fetchVehicles,
  fetchIncidents,
} from '../services/Service';

// Fix for default marker icon in leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

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
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FF6F00"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28"> %F0%9F%A9%B8 </text></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  FIRE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%23FFC107"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28"> %F0%9F%94%A5 </text></svg>',
    iconSize: [50, 50],
    iconAnchor: [25, 50],
    popupAnchor: [0, -50],
  }),
  POLICE: new L.Icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg width="50" height="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><rect width="50" height="50" rx="4" fill="%232196F3"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28"> %F0%9F%9A%A8 </text></svg>',
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

    if (!incident || incident.status === 'RESOLVED') {
      return null;
    }

    const lat = parseFloat(incident.latitude);
    const lng = parseFloat(incident.longitude);

    console.log(`Incident ${index}: lat=${lat}, lng=${lng}, type=${incident.type}`);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn(`Invalid coordinates for incident ${index}:`, { latitude: incident.latitude, longitude: incident.longitude });
      return null;
    }

    const incidentId = incident.id ?? incident.incidentId ?? 'N/A';

    return (
      <Marker
        key={`incident-${incidentId}`}
        position={[lat, lng]}
        icon={incidentIcons[incident.type] || incidentIcons.POLICE}
      >
        <Popup>
          <div className="marker-popup">
            <strong>{incidentEmojis[incident.type] || '📍'} {incident.type} Incident</strong>
            <p>ID: #{incidentId}</p>
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
function LocationSelectMarker({ onLocationSelect, selectedLocation, setLocation }) {
  useMapEvents({
    click(e) {
      if (setLocation) {
        onLocationSelect({
          latitude: e.latlng.lat,
          longitude: e.latlng.lng,
        });
      }
    },
  });

  if (!setLocation || !selectedLocation) return null;

  return (
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
  );
}

// Main LiveMap component
function LiveMap({
  setLocation = false,
  selectedLocation = null,
  onLocationSelect = () => { }
}) {
  const [vehicles, setVehicles] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user?.token) return;

    const loadData = async () => {
      try {
        setLoading(true);
        setError('');

        const [vehiclesData, incidentsData] = await Promise.all([
          fetchVehicles(user.token),
          fetchIncidents(user.token)
        ]);

        setVehicles(vehiclesData);
        setIncidents(incidentsData);
      } catch (err) {
        console.error(err);
        setError('Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user?.token]);


  return (
    <div className="map-section">
      {loading && fetchVehicles && fetchIncidents ? (
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
          {incidents && incidents.length > 0 && (
            <IncidentMarkers incidents={incidents} />
          )}
          <LocationSelectMarker
            onLocationSelect={onLocationSelect}
            selectedLocation={selectedLocation}
            setLocation={setLocation}
          />
        </MapContainer>
      )}
      {error && <div className="map-error">Error: {error}</div>}
    </div>
  );
}

export default LiveMap;