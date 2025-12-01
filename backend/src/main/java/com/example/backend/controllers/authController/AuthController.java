package com.example.backend.controllers.authController;

import com.example.backend.dtos.authDTO.LoginDTO;
import com.example.backend.dtos.authDTO.UserDTO;
import com.example.backend.dtos.response.SuccessResponse;
import com.example.backend.services.authService.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        authService.SignUp(user);
        return ResponseEntity.ok(SuccessResponse.of("User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginController(@RequestBody LoginDTO user) {
        String authToken = authService.Login(user);
        return ResponseEntity.ok(SuccessResponse.of("Logged in successfully", authToken));
    }
}
