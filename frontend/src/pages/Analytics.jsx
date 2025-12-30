import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip
} from '@mui/material';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ScatterChart,
  Scatter
} from 'recharts';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/analytics';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82ca9d'];

const Analytics = () => {
  const [tabValue, setTabValue] = useState(0);
  const [timeRange, setTimeRange] = useState(30);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Data states
  const [responseTimesByType, setResponseTimesByType] = useState([]);
  const [dailyStats, setDailyStats] = useState([]);
  const [monthlyStats, setMonthlyStats] = useState([]);
  const [vehicleUtilization, setVehicleUtilization] = useState([]);
  const [heatmapData, setHeatmapData] = useState([]);
  const [topPerformers, setTopPerformers] = useState([]);

  const user = JSON.parse(localStorage.getItem('user'));
  const token = user?.token;

  const fetchData = async () => {
    if (!token) return;
    
    setLoading(true);
    setError(null);

    try {
      const headers = { Authorization: `Bearer ${token}` };

      const [
        responseTimesRes,
        dailyRes,
        monthlyRes,
        utilizationRes,
        heatmapRes,
        topPerformersRes
      ] = await Promise.all([
        axios.get(`${API_BASE_URL}/response-times/by-type`, { headers }),
        axios.get(`${API_BASE_URL}/response-times/daily?days=${timeRange}`, { headers }),
        axios.get(`${API_BASE_URL}/response-times/monthly?months=6`, { headers }),
        axios.get(`${API_BASE_URL}/vehicle-utilization?days=${timeRange}`, { headers }),
        axios.get(`${API_BASE_URL}/heatmap?days=${timeRange}&minIncidents=2`, { headers }),
        axios.get(`${API_BASE_URL}/top-performing?days=${timeRange}&minIncidents=5&topN=10`, { headers })
      ]);

      setResponseTimesByType(responseTimesRes.data);
      setDailyStats(dailyRes.data);
      setMonthlyStats(monthlyRes.data);
      setVehicleUtilization(utilizationRes.data);
      setHeatmapData(heatmapRes.data);
      setTopPerformers(topPerformersRes.data);
    } catch (err) {
      console.error('Error fetching analytics:', err);
      setError('Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [timeRange]);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" fontWeight="bold">
          Analytics Dashboard
        </Typography>
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Time Range</InputLabel>
          <Select
            value={timeRange}
            label="Time Range"
            onChange={(e) => setTimeRange(e.target.value)}
          >
            <MenuItem value={7}>Last 7 Days</MenuItem>
            <MenuItem value={30}>Last 30 Days</MenuItem>
            <MenuItem value={90}>Last 90 Days</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Tabs value={tabValue} onChange={handleTabChange} sx={{ mb: 3 }}>
        <Tab label="Response Times" />
        <Tab label="Vehicle Utilization" />
        <Tab label="Incident Heatmap" />
        <Tab label="Top Performers" />
      </Tabs>

      {/* Tab 1: Response Times */}
      {tabValue === 0 && (
        <Grid container spacing={3}>
          {/* Response Time Stats by Type */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Average Response Times by Emergency Type
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={responseTimesByType}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="emergencyType" />
                    <YAxis label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="averageResponseTime" fill="#0088FE" name="Avg Time" />
                    <Bar dataKey="minResponseTime" fill="#00C49F" name="Min Time" />
                    <Bar dataKey="maxResponseTime" fill="#FF8042" name="Max Time" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* Response Time Table */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Response Time Statistics
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Emergency Type</TableCell>
                        <TableCell align="right">Avg Time (min)</TableCell>
                        <TableCell align="right">Min Time (min)</TableCell>
                        <TableCell align="right">Max Time (min)</TableCell>
                        <TableCell align="right">Total Incidents</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {responseTimesByType.map((stat) => (
                        <TableRow key={stat.emergencyType}>
                          <TableCell>
                            <Chip label={stat.emergencyType} color="primary" size="small" />
                          </TableCell>
                          <TableCell align="right">{stat.averageResponseTime?.toFixed(2)}</TableCell>
                          <TableCell align="right">{stat.minResponseTime?.toFixed(2)}</TableCell>
                          <TableCell align="right">{stat.maxResponseTime?.toFixed(2)}</TableCell>
                          <TableCell align="right">{stat.totalIncidents}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* Daily Trends */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Daily Response Time Trends
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={dailyStats}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Legend />
                    {[...new Set(dailyStats.map(d => d.emergencyType))].map((type, idx) => (
                      <Line
                        key={type}
                        type="monotone"
                        dataKey="averageResponseTime"
                        data={dailyStats.filter(d => d.emergencyType === type)}
                        name={type}
                        stroke={COLORS[idx % COLORS.length]}
                      />
                    ))}
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* Monthly Trends */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Monthly Response Time Trends
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={monthlyStats}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Legend />
                    {[...new Set(monthlyStats.map(d => d.emergencyType))].map((type, idx) => (
                      <Line
                        key={type}
                        type="monotone"
                        dataKey="averageResponseTime"
                        data={monthlyStats.filter(d => d.emergencyType === type)}
                        name={type}
                        stroke={COLORS[idx % COLORS.length]}
                      />
                    ))}
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Tab 2: Vehicle Utilization */}
      {tabValue === 1 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Vehicle Utilization Rates
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={vehicleUtilization.slice(0, 10)}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="vehiclePlate" />
                    <YAxis label={{ value: 'Utilization %', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Bar dataKey="utilizationRate" fill="#00C49F" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Vehicle Type Distribution
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={vehicleUtilization.reduce((acc, v) => {
                        const existing = acc.find(a => a.name === v.vehicleType);
                        if (existing) {
                          existing.value += 1;
                        } else {
                          acc.push({ name: v.vehicleType, value: 1 });
                        }
                        return acc;
                      }, [])}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={100}
                      label
                    >
                      {vehicleUtilization.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Vehicle Utilization Details
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Vehicle</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell align="right">Assignments</TableCell>
                        <TableCell align="right">Utilization %</TableCell>
                        <TableCell align="right">Avg Response (min)</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {vehicleUtilization.map((vehicle) => (
                        <TableRow key={vehicle.vehicleId}>
                          <TableCell>{vehicle.vehiclePlate}</TableCell>
                          <TableCell>
                            <Chip label={vehicle.vehicleType} size="small" />
                          </TableCell>
                          <TableCell align="right">{vehicle.totalAssignments}</TableCell>
                          <TableCell align="right">
                            <Chip
                              label={`${vehicle.utilizationRate?.toFixed(1)}%`}
                              color={vehicle.utilizationRate > 70 ? 'success' : vehicle.utilizationRate > 40 ? 'warning' : 'error'}
                              size="small"
                            />
                          </TableCell>
                          <TableCell align="right">{vehicle.averageResponseTime?.toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Tab 3: Incident Heatmap */}
      {tabValue === 2 && (
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Incident Distribution Heatmap
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <ScatterChart>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="longitude" name="Longitude" />
                    <YAxis dataKey="latitude" name="Latitude" />
                    <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                    <Legend />
                    {[...new Set(heatmapData.map(d => d.emergencyType))].map((type, idx) => (
                      <Scatter
                        key={type}
                        name={type}
                        data={heatmapData.filter(d => d.emergencyType === type)}
                        fill={COLORS[idx % COLORS.length]}
                      />
                    ))}
                  </ScatterChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Hotspot Details
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Location</TableCell>
                        <TableCell>Emergency Type</TableCell>
                        <TableCell align="right">Incident Count</TableCell>
                        <TableCell align="right">Avg Severity</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {heatmapData.slice(0, 15).map((point, idx) => (
                        <TableRow key={idx}>
                          <TableCell>
                            {`(${point.latitude.toFixed(4)}, ${point.longitude.toFixed(4)})`}
                          </TableCell>
                          <TableCell>
                            <Chip label={point.emergencyType} color="primary" size="small" />
                          </TableCell>
                          <TableCell align="right">{point.incidentCount}</TableCell>
                          <TableCell align="right">
                            <Chip
                              label={point.severity}
                              color={point.severity >= 4 ? 'error' : point.severity >= 3 ? 'warning' : 'success'}
                              size="small"
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Tab 4: Top Performers */}
      {tabValue === 3 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Top 10 Fastest Response Units
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={topPerformers} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" label={{ value: 'Minutes', position: 'insideBottom' }} />
                    <YAxis dataKey="vehiclePlate" type="category" />
                    <Tooltip />
                    <Bar dataKey="averageResponseTime" fill="#0088FE" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Success Rates
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={topPerformers} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" label={{ value: 'Success %', position: 'insideBottom' }} />
                    <YAxis dataKey="vehiclePlate" type="category" />
                    <Tooltip />
                    <Bar dataKey="successRate" fill="#00C49F" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Top Performing Units - Detailed Ranking
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Rank</TableCell>
                        <TableCell>Vehicle</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell align="right">Avg Response (min)</TableCell>
                        <TableCell align="right">Completed</TableCell>
                        <TableCell align="right">Success Rate</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {topPerformers.map((unit, idx) => (
                        <TableRow key={unit.vehicleId}>
                          <TableCell>
                            <Chip
                              label={`#${idx + 1}`}
                              color={idx < 3 ? 'success' : 'default'}
                              size="small"
                            />
                          </TableCell>
                          <TableCell fontWeight="bold">{unit.vehiclePlate}</TableCell>
                          <TableCell>
                            <Chip label={unit.vehicleType} size="small" />
                          </TableCell>
                          <TableCell align="right">
                            <Typography color="success.main" fontWeight="bold">
                              {unit.averageResponseTime?.toFixed(2)}
                            </Typography>
                          </TableCell>
                          <TableCell align="right">{unit.completedIncidents}</TableCell>
                          <TableCell align="right">
                            <Chip
                              label={`${unit.successRate?.toFixed(1)}%`}
                              color={unit.successRate >= 90 ? 'success' : unit.successRate >= 75 ? 'warning' : 'error'}
                              size="small"
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default Analytics;