package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyResponseStats {
    private String date; // YYYY-MM-DD
    private String emergencyType;
    private Double averageResponseTime;
    private Long incidentCount;
}