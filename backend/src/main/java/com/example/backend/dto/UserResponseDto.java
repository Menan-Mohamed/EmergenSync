package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private int id;
    private String username;
    private String email;
    private User.UserType type;
    private User.UserRole role;
    private boolean approved;
}
