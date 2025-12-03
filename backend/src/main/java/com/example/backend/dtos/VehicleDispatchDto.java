package com.example.backend.dtos;

import java.time.LocalDateTime;

import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDispatchDto {

    private Integer id;
    private VehicleType type;
    private VehicleStatus status;
    private LocalDateTime lastUpdate;
    private Double longitude;
    private Double latitude;
    private LocalDateTime locationTimeStamp;
    private Double distanceToIncident;
    
    private int responderId;

}
