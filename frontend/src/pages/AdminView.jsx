import * as React from 'react';
import { useState } from 'react';
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
  const [notificationCount] = useState(5); // You can manage this state as needed

  const handleNotificationClick = () => {
    console.log('Notification clicked');
    // Add your notification handler logic here
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <IconButton color="inherit" onClick={handleNotificationClick}>
        <Badge badgeContent={notificationCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>
      <ThemeSwitcher />
    </Box>
  );
}

function AdminView(props) {
  const { window } = props;
  const [pathname, setPathname] = useState('/vehicle-management');

  const router = {
    pathname,
    searchParams: new URLSearchParams(),
    navigate: (path) => setPathname(path),
  };

  const demoWindow = window !== undefined ? window() : undefined;

  return (
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
  );
}

AdminView.propTypes = {
  window: PropTypes.func,
};

export default AdminView;