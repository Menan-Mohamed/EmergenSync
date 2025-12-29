import { useEffect, useState, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useNotificationSocket = (adminId, onNotificationReceived) => {
    const clientRef = useRef(null);
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    
    const user = JSON.parse(localStorage.getItem('user'));
    const token = user?.token;

    useEffect(() => {
        if (!adminId || !token) {
            console.error('Cannot connect WebSocket: missing adminId or token');
            return;
        }

        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => { console.log(str); },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log("✅ WS CONNECTED");
                console.log("🔌 SUBSCRIBING → /topic/admin-notifications/" + adminId);
                setConnected(true);
                setError(null);
                
                // FIX: Add parentheses around template literal
                client.subscribe(`/topic/admin-notifications/${adminId}`, (message) => {
                    console.log("📨 WS RECEIVED:", message.body);
                    try {
                        const notification = JSON.parse(message.body);
                        console.log("📦 Parsed notification:", notification);
                        onNotificationReceived(notification);
                    } catch (err) {
                        console.error("❌ Failed to parse notification:", err);
                    }
                });
            },
            onStompError: (frame) => {
                console.error('❌ Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                setConnected(false);
                setError('WebSocket error');
            },
            onWebSocketClose: () => {
                console.log('🔌 WebSocket connection closed');
                setConnected(false);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            if (clientRef.current) {
                console.log('🔌 Deactivating WebSocket...');
                clientRef.current.deactivate();
            }
        };
    }, [adminId, onNotificationReceived, token]);

    const sendNotification = useCallback((destination, notification) => {
        if (clientRef.current && connected) {
            clientRef.current.publish({
                destination,
                body: JSON.stringify(notification)
            });
        }
    }, [connected]);

    return { connected, error, client: clientRef.current, sendNotification };
};