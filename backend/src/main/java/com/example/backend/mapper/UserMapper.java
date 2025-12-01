package com.example.backend.mapper;

import com.example.backend.dtos.authDTO.UserDTO;
import com.example.backend.entities.User;

public class UserMapper {
    public User signupToUser(UserDTO newUser) {
        User user = new User();
        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail());
        user.setPassword(newUser.getPassword());
        user.setType(newUser.getType());
        user.setRole(newUser.getRole());
        user.setApproved(false);
        return user;
    }
}
