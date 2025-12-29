import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useNotificationSocket } from '../hooks/NotificationSocket';
import axios from 'axios';

const NotificationContext = createContext();
const API_BASE_URL = 'http://localhost:8080/api/notifications';

export const NotificationProvider = ({ children, adminId: propAdminId }) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);
    
    // Get from localStorage
    const user = JSON.parse(localStorage.getItem('user'));
    const token = user?.token;
    
    // Use prop if provided, otherwise fallback to localStorage
    const adminId = propAdminId || user?.user_id || user?.id;

    /** Load notifications for this admin */
    const loadNotifications = useCallback(async () => {
        if (!token || !adminId) {
            console.error('Missing token or adminId');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            console.log(`📡 Fetching notifications for admin ${adminId}...`);
            
            // FIX: Add parentheses around template literal
            const res = await axios.get(`${API_BASE_URL}/admin/${adminId}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            
            console.log('✅ Notifications loaded:', res.data);
            setNotifications(res.data);
            const unread = res.data.filter(n => !n.read).length;
            setUnreadCount(unread);
        } catch (error) {
            console.error('❌ Error loading notifications:', error);
            console.error('Response:', error.response?.data);
        } finally {
            setLoading(false);
        }
    }, [token, adminId]);

    const handleNotificationReceived = useCallback((notification) => {
        console.log('🔔 New notification received:', notification);
        setNotifications(prev => [notification, ...prev]);
        if (!notification.read) setUnreadCount(prev => prev + 1);
        
        if (Notification.permission === 'granted') {
            new Notification('EmergenSync Alert', {
                body: notification.msg || notification.message,
                icon: '/logo.jpeg',
                tag: notification.id?.toString()
            });
        }
    }, []);

    const { connected, error } = useNotificationSocket(adminId, handleNotificationReceived);

    useEffect(() => {
        if (!token || !adminId) {
            console.error('Cannot load notifications: missing token or adminId');
            return;
        }

        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }

        loadNotifications();
    }, [token, adminId, loadNotifications]);

    return (
        <NotificationContext.Provider
            value={{
                notifications,
                unreadCount,
                connected,
                error,
                loading,
                refreshNotifications: loadNotifications,
            }}
        >
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotifications = () => {
    const context = useContext(NotificationContext);
    if (!context) throw new Error('useNotifications must be used within NotificationProvider');
    return context;
};