import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default function useVehicleSocket() {
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
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
        setVehicles((prev) => {
          const index = prev.findIndex(v => v.id === data.id);
          if (index !== -1) {
            // update existing
            const updated = [...prev];
            updated[index] = data;
            return updated;
          } else {
            // new vehicle
            return [...prev, data];
          }
        });
      });
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return vehicles;
}
