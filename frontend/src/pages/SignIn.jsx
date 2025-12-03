import * as React from "react";
import { AppProvider } from "@toolpad/core/AppProvider";
import { SignInPage } from "@toolpad/core/SignInPage";
import { useTheme } from "@mui/material/styles";
import { AuthContext } from "./../auth/AuthContext";

/**
 * Here is where credentials are validate, API is called,
 * and user metadata (like role) is returned.
 */
const providers = [{ id: "credentials", name: "Username and Password" }];

export default function CredentialsSignInPage() {
  const theme = useTheme();
  const { login } = React.useContext(AuthContext);

  const signIn = async (provider, formData) => {
    const username = formData.get("username");
    const password = formData.get("password");

    try {
      console.log("Sending payload:", { username, password });
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          password,
        }),
      });

      if (!response.ok) {
        throw new Error("Invalid username or password");
      }

      const data = await response.json();
      console.log("Server response:", data);

      const userData = {
        username,
        token: data.data.token,  // ✅ Extract token from data.data
        role: data.data.role,    // ✅ Extract role from data.data
      };

      console.log("Extracted userData:", userData); // Add this to verify

      login(userData);

      return { success: true, user: userData };
    } catch (err) {
      console.error("Login error:", err);
      return { success: false, error: err.message };
    }
  };

  return (
    <AppProvider theme={theme}>
      <SignInPage
        signIn={signIn}
        providers={providers}
        slotProps={{
        emailField: {
            label: "Username",
            name: "username",
            placeholder: "",
            autoFocus: true,
        },
        passwordField: { 
                label: "Password", 
                name: "password", 
                placeholder: "" 
        },
        form: { noValidate: true }
        }}
      />
    </AppProvider>
  );
}
