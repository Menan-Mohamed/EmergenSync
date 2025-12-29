import { useEffect, useState, useContext } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { AuthContext } from '../auth/AuthContext';

export default function useIncidentSocket() {
  const [incidents, setIncidents] = useState([]);
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user?.token) return;

    // STEP 1: Fetch all existing incidents from REST API
    const loadInitialIncidents = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/dispatcher/incidents', {
          headers: {
            'Authorization': `Bearer ${user.token}`,
            'Content-Type': 'application/json',
          },
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch incidents');
        }
        
        const incidentsData = await response.json();
        console.log('Loaded', incidentsData.length, 'incidents from API');
        setIncidents(incidentsData);
      } catch (err) {
        console.error('Failed to fetch incidents:', err);
      }
    };

    // Load all incidents immediately on mount
    loadInitialIncidents();

    // STEP 2: Set up WebSocket for real-time incident updates
    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      console.log('Connected to WebSocket for incidents');
      
      // Subscribe to incident updates - adjust the topic as needed
      // Common patterns: /topic/incidents, /topic/incident-updates, etc.
      client.subscribe('/topic/incidents', (message) => {
        const data = JSON.parse(message.body);
        console.log('Incident update via WebSocket:', data.id);
        
        setIncidents((prev) => {
          const index = prev.findIndex(i => i.id === data.id);
          if (index !== -1) {
            // Update existing incident
            const updated = [...prev];
            updated[index] = data;
            console.log('Updated incident:', data.id);
            return updated;
          } else {
            // Add new incident if it doesn't exist
            console.log('Added new incident:', data.id);
            return [...prev, data];
          }
        });
      });
    };

    client.activate();

    return () => {
      console.log('Disconnecting incident WebSocket');
      client.deactivate();
    };
  }, [user?.token]);

  return incidents;
}