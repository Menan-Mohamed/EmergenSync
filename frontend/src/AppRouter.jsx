import { Routes, Route } from "react-router-dom";
import SignIn from "./pages/SignIn";
import AdminView from "./pages/AdminView";
import DispatcherView from "./pages/DispatcherView";
import ResponderView from "./pages/ResponderView";
import ReporterView from "./pages/ReporterView";
import ProtectedRoute from "./auth/ProtectedRoute";

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<SignIn />} />

      <Route
        path="/admin"
        element={
          <ProtectedRoute role="SYSTEM_ADMIN">
            <AdminView />
          </ProtectedRoute>
        }
      />

      <Route
        path="/dispatcher"
        element={
          <ProtectedRoute role="DISPATCHER">
            <DispatcherView />
          </ProtectedRoute>
        }
      />

      <Route
        path="/responder"
        element={
          <ProtectedRoute role="EMERGENCY_RESPONDER">
            <ResponderView />
          </ProtectedRoute>
        }
      />

      <Route
        path="/user"
        element={
          <ProtectedRoute role="user">
            <ReporterView />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<h1>404 Not Found</h1>} />
    </Routes>
  );
}