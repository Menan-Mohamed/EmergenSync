package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentHeatmapPoint {
    private Double latitude;
    private Double longitude;
    private Long incidentCount;
    private String emergencyType;
    private Integer severity;
}