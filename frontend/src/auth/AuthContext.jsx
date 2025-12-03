import { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
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