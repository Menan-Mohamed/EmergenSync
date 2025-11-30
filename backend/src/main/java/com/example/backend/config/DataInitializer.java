package com.example.backend.config;

import com.example.backend.entities.User;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("SystemAdmin")) {
            User admin = new User();
            admin.setUsername("SystemAdmin");
            admin.setType(User.UserType.fire);
            admin.setRole(User.UserRole.SYSTEM_ADMIN);
            admin.setApproved(true);

            userRepository.save(admin);
            System.out.println("✓ Default admin user created successfully!");
        } else {
            System.out.println("✓ Default admin user already exists.");
        }
    }
}
