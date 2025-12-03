import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import VehicleManagement from './VehicleManagement';
import IncedintForm from './IncedintForm';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/app/admin" element={<VehicleManagement />} />
        <Route path="/app/user" element={<IncedintForm />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
