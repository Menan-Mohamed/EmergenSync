package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopPerformingUnit {
    private Integer vehicleId;
    private String vehiclePlate;
    private String vehicleType;
    private Double averageResponseTime;
    private Long completedIncidents;
    private Double successRate;
}