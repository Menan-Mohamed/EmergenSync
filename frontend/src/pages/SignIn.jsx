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
      // If response is not ok, try to parse the error body for details
      if (!response.ok) {
        let errText = `HTTP ${response.status}`;
        try {
          const errBody = await response.json();
          errText = errBody.message || JSON.stringify(errBody);
        } catch (e) {
          try {
            const errBody = await response.text();
            errText = errBody;
          } catch (_e) {}
        }
        throw new Error(errText || "Invalid username or password");
      }

      const data = await response.json();
      console.log("Server response:", data);

      // Be flexible with response shape: token may live at data.data.token, data.token, or data.accessToken
      const token = data?.data?.token ?? data?.token ?? data?.accessToken ?? null;
      const role = data?.data?.role ?? data?.role ?? null;

      if (!token) {
        console.warn('No token found in login response:', data);
        throw new Error('Login succeeded but no token returned by server');
      }

      const userData = {
        username,
        token,
        role,
      };

      console.log("Extracted userData:", userData);

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
