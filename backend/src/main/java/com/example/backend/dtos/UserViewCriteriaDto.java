package com.example.backend.dtos;

import com.example.backend.enums.UserRole;
import com.example.backend.enums.UserType;
import lombok.Data;

@Data
public class UserViewCriteriaDto {

    private UserType type;
    private UserRole role;
    private Boolean approved;
    private String search;
    private String sortBy ;
    private String sortDir ;
}
