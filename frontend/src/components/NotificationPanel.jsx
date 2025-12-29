import React from 'react';
import {
    Drawer,
    List,
    ListItem,
    Typography,
    IconButton,
    Box,
    Chip,
    Divider,
    CircularProgress,
    Alert,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useNotifications } from '../context/NotificationContext';

const NotificationPanel = ({ open, onClose }) => {
    const { 
        notifications, 
        refreshNotifications,
        loading,
        connected,
        error 
    } = useNotifications();

    const getTypeColor = (type) => {
        switch (type?.toUpperCase()) {
            case 'ERROR':
            case 'SYSTEM':
                return 'error';
            case 'WARNING':
                return 'warning';
            case 'SUCCESS':
                return 'success';
            default:
                return 'info';
        }
    };

    const getCategoryIcon = (category) => {
        switch (category) {
            case 'VEHICLE_ALERT':
                return '🚗';
            case 'SYSTEM_ALERT':
                return '⚠️';
            case 'USER_UPDATE':
                return '👤';
            default:
                return '📢';
        }
    };

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins} min ago`;
        if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
        if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
        
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    };

    return (
        <Drawer anchor="right" open={open} onClose={onClose}>
            <Box sx={{ width: 420, height: '100%', display: 'flex', flexDirection: 'column' }}>
                {/* Header */}
                <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                        <Typography variant="h6">Notifications</Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <IconButton size="small" onClick={refreshNotifications}>
                                <RefreshIcon />
                            </IconButton>
                            <IconButton onClick={onClose}>
                                <CloseIcon />
                            </IconButton>
                        </Box>
                    </Box>
                    
                    {/* Connection Status */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box
                            sx={{
                                width: 8,
                                height: 8,
                                borderRadius: '50%',
                                bgcolor: connected ? 'success.main' : 'error.main',
                            }}
                        />
                        <Typography variant="caption" color="text.secondary">
                            {connected ? 'Connected' : 'Disconnected'}
                        </Typography>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mt: 1 }}>
                            {error}
                        </Alert>
                    )}
                </Box>

                {/* Notifications List */}
                <Box sx={{ flex: 1, overflow: 'auto' }}>
                    {loading ? (
                        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                            <CircularProgress />
                        </Box>
                    ) : notifications.length === 0 ? (
                        <Box sx={{ textAlign: 'center', py: 8 }}>
                            <Typography variant="body2" color="text.secondary">
                                No notifications
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                You're all caught up! 
                            </Typography>
                        </Box>
                    ) : (
                        <List sx={{ p: 0 }}>
                            {notifications.map((notification) => (
                                <React.Fragment key={notification.id}>
                                    <ListItem
                                        sx={{
                                            bgcolor: notification.read ? 'transparent' : 'action.hover',
                                            flexDirection: 'column',
                                            alignItems: 'flex-start',
                                            py: 2,
                                            '&:hover': {
                                                bgcolor: 'action.selected',
                                            },
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', width: '100%', justifyContent: 'space-between', mb: 1 }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Typography variant="h6" sx={{ fontSize: '1.2rem' }}>
                                                    {getCategoryIcon(notification.category)}
                                                </Typography>
                                                <Typography variant="subtitle2" fontWeight="bold">
                                                    {notification.category?.replace('_', ' ')}
                                                </Typography>
                                            </Box>
                                        </Box>
                                        
                                        <Typography variant="body2" sx={{ mb: 1, width: '100%' }}>
                                            {notification.msg}
                                        </Typography>
                                        
                                        <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                                            <Typography variant="caption" color="text.secondary">
                                                {formatTimestamp(notification.sentAt)}
                                            </Typography>
                                            <Chip
                                                label={notification.type}
                                                size="small"
                                                color={getTypeColor(notification.type)}
                                                sx={{ height: 20, fontSize: '0.7rem' }}
                                            />
                                        </Box>
                                    </ListItem>
                                    <Divider />
                                </React.Fragment>
                            ))}
                        </List>
                    )}
                </Box>
            </Box>
        </Drawer>
    );
};

export default NotificationPanel;