package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleUtilization {
    private Integer vehicleId;
    private String vehiclePlate;
    private String vehicleType;
    private Long totalAssignments;
    private Double utilizationRate; 
    private Double averageResponseTime;
}