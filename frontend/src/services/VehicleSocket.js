import { useEffect, useState, useContext } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { AuthContext } from '../auth/AuthContext';

export default function useVehicleSocket() {
  const [vehicles, setVehicles] = useState([]);
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user?.token) return;

    // STEP 1: Fetch all existing vehicles from REST API
    const loadInitialVehicles = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/responder/vehicle/all', {
          headers: {
            'Authorization': `Bearer ${user.token}`,
            'Content-Type': 'application/json',
          },
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch vehicles');
        }
        
        const vehiclesData = await response.json();
        console.log('Loaded', vehiclesData.length, 'vehicles from API');
        setVehicles(vehiclesData);
      } catch (err) {
        console.error('Failed to fetch vehicles:', err);
      }
    };

    // Load all vehicles immediately on mount
    loadInitialVehicles();

    // STEP 2: Set up WebSocket for real-time location updates
    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      console.log('Connected to WebSocket');
      
      client.subscribe('/topic/vehicles', (message) => {
        const data = JSON.parse(message.body);
        console.log('Vehicle update via WebSocket:', data.id);
        
        setVehicles((prev) => {
          const index = prev.findIndex(v => v.id === data.id);
          if (index !== -1) {
            // Update existing vehicle location
            const updated = [...prev];
            updated[index] = data;
            console.log('Updated vehicle location:', data.id);
            return updated;
          } else {
            // Add new vehicle if it doesn't exist
            console.log('Added new vehicle:', data.id);
            return [...prev, data];
          }
        });
      });
    };

    client.activate();

    return () => {
      console.log('Disconnecting WebSocket');
      client.deactivate();
    };
  }, [user?.token]);

  return vehicles;
}