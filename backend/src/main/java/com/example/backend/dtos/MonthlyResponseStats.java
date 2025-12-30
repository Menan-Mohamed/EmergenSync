package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyResponseStats {
    private String month; // YYYY-MM
    private String emergencyType;
    private Double averageResponseTime;
    private Long incidentCount;
}