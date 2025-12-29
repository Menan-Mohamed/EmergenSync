// API base URLs
const VEHICLE_API_BASE_URL = 'http://localhost:8080/api/responder/vehicle';
const ASSIGNMENT_API_BASE_URL = 'http://localhost:8080/api/assignments';

// Fetch all assignments
export const fetchAssignments = async (authToken = null) => {
  const headers = {};
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${ASSIGNMENT_API_BASE_URL}/all`, { headers });

  if (!response.ok) {
    throw new Error(`Failed to fetch assignments: ${response.statusText}`);
  }

  return await response.json();
};

// Create a new vehicle
export const createVehicle = async (payload, authToken) => {
  const headers = {
    'Content-Type': 'application/json',
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(VEHICLE_API_BASE_URL, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(`Failed to create vehicle: ${response.statusText}`);
  }

  return await response.json();
};

// Update vehicle location
export const updateVehicleLocation = async (vehicleId, latitude, longitude, authToken) => {
  const url = `${VEHICLE_API_BASE_URL}/${vehicleId}/location?latitude=${latitude}&longitude=${longitude}`;

  const headers = {};
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(url, {
    method: 'PUT',
    headers,
  });

  if (!response.ok) {
    throw new Error(`Failed to update location: ${response.statusText}`);
  }

  return true;
};