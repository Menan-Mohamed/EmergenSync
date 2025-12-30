package com.example.backend.dtos;

import com.example.backend.enums.UserRole;
import com.example.backend.enums.UserType;
import lombok.Data;

@Data
public class UserResponseDto {
    private int id;
    private String username;
    private String email;
    private UserType type;
    private UserRole role;
}
