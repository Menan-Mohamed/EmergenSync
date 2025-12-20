import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';

// Analytics Component
function Analytics() {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Analytics
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Analytics will appear here
      </Typography>
    </Box>
  );
}
export default Analytics;