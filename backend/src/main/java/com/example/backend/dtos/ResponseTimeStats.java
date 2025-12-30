package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTimeStats {
    private String emergencyType;
    private Double averageResponseTime;
    private Double minResponseTime;
    private Double maxResponseTime;
    private Long totalIncidents;
}