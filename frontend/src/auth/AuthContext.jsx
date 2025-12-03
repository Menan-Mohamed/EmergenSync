import { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  // Initialize user synchronously from localStorage to avoid a flash redirect
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem("user");
      return stored ? JSON.parse(stored) : null;
    } catch (e) {
      console.error('Error parsing stored user', e);
      return null;
    }
  });
  const navigate = useNavigate();

  const login = (userData) => {
    console.log("Login called with userData:", userData); // Debug log
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));

    // Redirect by role - MUST match backend role names exactly
    switch (userData.role) {
      case "SYSTEM_ADMIN":
        navigate("/admin");
        break;
      case "DISPATCHER":
        navigate("/dispatcher");
        break;
      case "EMERGENCY_RESPONDER":
        navigate("/responder");
        break;
      default:
        navigate("/user");
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
    navigate("/");
  };

  // Load from storage on refresh
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) setUser(JSON.parse(storedUser));
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}