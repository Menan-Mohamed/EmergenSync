package com.example.backend.config;

import com.example.backend.entities.User;
import com.example.backend.enums.UserRole;
import com.example.backend.enums.UserType;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;


    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("SystemAdmin")) {
            User admin = new User();
            admin.setUsername("SystemAdmin");
            admin.setPassword(encoder.encode("SystemAdmin2025"));
            admin.setType(UserType.FIRE);
            admin.setRole(UserRole.SYSTEM_ADMIN);
            userRepository.save(admin);
            System.out.println("✓ Default admin user created successfully!");
        } else {
            System.out.println("✓ Default admin user already exists.");
        }
    }
}
