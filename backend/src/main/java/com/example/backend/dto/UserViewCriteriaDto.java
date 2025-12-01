package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.Data;

@Data
public class UserViewCriteriaDto {

    private User.UserType type;
    private User.UserRole role;
    private Boolean approved;
    private String search;
    private String sortBy ;
    private String sortDir ;
}
