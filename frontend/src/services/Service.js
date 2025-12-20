// API base URLs
const VEHICLE_API_BASE_URL = 'http://localhost:8080/api/responder/vehicle';
const INCIDENT_API_BASE_URL = 'http://localhost:8080/api/dispatcher/incidents';
const ASSIGNMENT_API_BASE_URL = 'http://localhost:8080/api/assignments';

// Fetch all vehicles
export const fetchVehicles = async (authToken = null) => {
  const headers = {};
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;
  
  const response = await fetch(`${VEHICLE_API_BASE_URL}/all`, { headers });
  
  if (!response.ok) {
    throw new Error(`Failed to fetch vehicles: ${response.statusText}`);
  }
  
  const data = await response.json();
  console.log('Fetched vehicles:', data);
  console.log('Vehicle count:', data ? data.length : 0);
  
  // Log first vehicle structure for debugging
  if (data && data.length > 0) {
    console.log('First vehicle structure:', JSON.stringify(data[0], null, 2));
  }
  
  return data || [];
};

// Fetch all incidents
export const fetchIncidents = async (authToken = null) => {
  const headers = {};
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;
  
  const response = await fetch(INCIDENT_API_BASE_URL, { headers });
  
  if (!response.ok) {
    throw new Error(`Failed to fetch incidents: ${response.statusText}`);
  }
  
  const data = await response.json();
  console.log('Fetched incidents:', data);
  console.log('Incident count:', data ? data.length : 0);
  
  // Log first incident structure for debugging
  if (data && data.length > 0) {
    console.log('First incident structure:', JSON.stringify(data[0], null, 2));
  }
  
  return data || [];
};

// Fetch all assignments
export const fetchAssignments = async (authToken = null) => {
  const headers = {};
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;
  
  const response = await fetch(`${ASSIGNMENT_API_BASE_URL}/all`, { headers });

  if (!response.ok) {
    // Try to parse error body for message
    let errText = `HTTP ${response.status}`;
    try {
      const ct = response.headers.get('content-type') || '';
      if (ct.includes('application/json')) {
        const errBody = await response.json();
        errText = errBody.message || JSON.stringify(errBody);
      } else {
        const txt = await response.text();
        errText = txt.substring(0, 400);
      }
    } catch (e) {
      console.error('Error parsing assignments error body', e);
    }
    throw new Error(`Failed to fetch assignments: ${errText}`);
  }

  // Ensure we only parse JSON when server sends JSON
  const contentType = response.headers.get('content-type') || '';
  let data = null;
  if (contentType.includes('application/json')) {
    data = await response.json();
  } else {
    const text = await response.text();
    console.warn('Expected JSON for assignments but received:', text.slice(0, 200));
    throw new Error('Assignments endpoint returned non-JSON (likely an HTML error or redirect)');
  }

  console.log('Fetched assignments:', data);
  return data || [];
};

// Create a new vehicle
export const createVehicle = async (payload, authToken) => {
  console.log('Sending payload:', JSON.stringify(payload, null, 2));

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

  console.log('Response status:', response.status);
  console.log('Response headers:', response.headers);

  const contentType = response.headers.get('content-type');
  console.log('Content-Type:', contentType);

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`;
    
    try {
      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json();
        errorMessage = errorData.message || JSON.stringify(errorData);
      } else {
        const errorText = await response.text();
        errorMessage = errorText.substring(0, 200);
      }
    } catch (parseErr) {
      console.error('Error parsing response:', parseErr);
    }

    throw new Error(`Failed to create vehicle: ${errorMessage}`);
  }

  const responseText = await response.text();
  console.log('Response text:', responseText);

  if (!responseText) {
    throw new Error('Empty response from server');
  }

  return JSON.parse(responseText);
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