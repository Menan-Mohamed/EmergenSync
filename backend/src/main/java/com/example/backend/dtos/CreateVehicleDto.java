package com.example.backend.dtos;

import com.example.backend.enums.VehicleType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleDto {
    @NotNull(message = "Vehicle type missing")
    private VehicleType type;

    @NotNull(message = "Responder ID missing")
    private int responderId;

    private Double longitude;
    private Double latitude;
}
