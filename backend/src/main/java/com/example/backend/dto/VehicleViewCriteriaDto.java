package com.example.backend.dto;

import com.example.backend.entity.Vehicle;
import lombok.Data;

@Data
public class VehicleViewCriteriaDto {

    private Vehicle.VehicleType type;
    private Vehicle.VehicleStatus status;
    private String responderUsername;
    private String search;
    private String sortBy;
    private String sortDir;
}
