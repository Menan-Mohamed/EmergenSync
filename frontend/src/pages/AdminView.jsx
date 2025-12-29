import * as React from 'react';
import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Badge from '@mui/material/Badge';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { createTheme } from '@mui/material/styles';
import PeopleIcon from '@mui/icons-material/People';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import MapIcon from '@mui/icons-material/Map';
import BarChartIcon from '@mui/icons-material/BarChart';
import { AppProvider } from '@toolpad/core/AppProvider';
import { DashboardLayout } from '@toolpad/core/DashboardLayout';
import { ThemeSwitcher } from '@toolpad/core/DashboardLayout';
import VehicleManagement from './VehicleManagement';
import LiveMap from './Livemap';
import UserManagement from './UserManagement';
import Analytics from './Analytics';
import NotificationPanel from '../components/NotificationPanel';
import { NotificationProvider, useNotifications } from '../context/NotificationContext';

const NAVIGATION = [
  {
    segment: 'user-management',
    title: 'User Management',
    icon: <PeopleIcon />,
  },
  {
    segment: 'vehicle-management',
    title: 'Vehicle Management',
    icon: <DirectionsCarIcon />,
  },
  {
    segment: 'map-view',
    title: 'Live Map',
    icon: <MapIcon />,
  },
  {
    segment: 'analytics',
    title: 'Analytics',
    icon: <BarChartIcon />,
  },
];

const demoTheme = createTheme({
  cssVariables: {
    colorSchemeSelector: 'data-toolpad-color-scheme',
  },
  colorSchemes: { light: true, dark: true },
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
      md: 600,
      lg: 1200,
      xl: 1536,
    },
  },
});

// Page Content Router
function PageContent({ pathname }) {
  switch (pathname) {
    case '/user-management':
      return <UserManagement />;
    case '/vehicle-management':
      return (
        <Box sx={{ height: '100%', overflow: 'hidden' }}>
          <VehicleManagement />
        </Box>
      );
    case '/map-view':
      return (
        <Box sx={{ height: '100%', overflow: 'hidden' }}>
          <LiveMap />
        </Box>
      );
    case '/analytics':
      return <Analytics />;
    default:
      return <UserManagement />;
  }
}

PageContent.propTypes = {
  pathname: PropTypes.string.isRequired,
};

// Custom toolbar actions component
function ToolbarActions() {
  const { unreadCount } = useNotifications();
  const [panelOpen, setPanelOpen] = useState(false);

  const handleNotificationClick = () => {
    setPanelOpen(prev => !prev);
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <IconButton color="inherit" onClick={handleNotificationClick}>
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>
      <ThemeSwitcher />
      <NotificationPanel open={panelOpen} onClose={() => setPanelOpen(false)} />
    </Box>
  );
}

function AdminView(props) {
  const { window } = props;
  const [pathname, setPathname] = useState('/vehicle-management');
  const [currentUserId, setCurrentUserId] = useState(null);

  useEffect(() => {
    const getUserId = () => {
      try {
        // First, try to get from the user object in localStorage
        const userStr = localStorage.getItem('user');
        if (userStr) {
          const user = JSON.parse(userStr);
          
          // Extract token from user object
          const token = user.token;
          if (token) {
            // Decode JWT token to get user_id
            const payload = JSON.parse(atob(token.split('.')[1]));
            console.log('Token payload:', payload);
            
            // Your token has user_id field
            if (payload.user_id) {
              return payload.user_id;
            }
          }
        }
        
        // Fallback: try direct token in localStorage
        const token = localStorage.getItem('token');
        if (token) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          return payload.user_id || payload.userId || payload.sub;
        }
        
        console.error('No user ID found in localStorage');
        return null;
      } catch (err) {
        console.error('Error extracting user ID:', err);
        return null;
      }
    };

    const userId = getUserId();
    if (userId) {
      setCurrentUserId(userId);
      console.log('✅ Current admin user ID:', userId);
    } else {
      console.error('❌ Failed to retrieve user ID');
    }
  }, []);

  const router = {
    pathname,
    searchParams: new URLSearchParams(),
    navigate: (path) => setPathname(path),
  };

  const demoWindow = window !== undefined ? window() : undefined;

  return (
    <NotificationProvider adminId={1}>
      <AppProvider
        navigation={NAVIGATION}
        router={router}
        theme={demoTheme}
        window={demoWindow}
        branding={{
          logo: <img src="/logo.jpeg" alt="EmergenSync" style={{ height: 30, width: 'auto' }} />,
          title: 'EmergenSync',
        }}
      >
        <DashboardLayout 
          disableCollapsibleSidebar
          slots={{
            toolbarActions: ToolbarActions,
          }}
        >
          <PageContent pathname={pathname} />
        </DashboardLayout>
      </AppProvider>
    </NotificationProvider>
  );
}

AdminView.propTypes = {
  window: PropTypes.func,
};

export default AdminView;