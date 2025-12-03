import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import VehicleManagement from './VehicleManagement';
import IncedintForm from './IncedintForm';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<VehicleManagement />} />
        <Route path="/admin" element={<IncedintForm />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
