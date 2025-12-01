package com.example.backend.dto;

import com.example.backend.entity.Vehicle;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VehicleResponseDto {
    private Integer id;
    private Vehicle.VehicleType type;
    private String responderUsername;
    private Vehicle.VehicleStatus status;
    private LocalDateTime lastUpdate;
}
