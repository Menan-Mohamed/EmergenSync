package com.example.backend.dtos.authDTO;

import com.example.backend.enums.UserRole;
import com.example.backend.enums.UserType;
import lombok.Data;

@Data
public class UserDTO {

    private String username;
    private String email;
    private String password;
    private UserType type;
    private UserRole role;

}
