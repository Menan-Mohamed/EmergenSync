package com.example.backend.dtos;

import com.example.backend.enums.IncidentType;

import lombok.Data;


@Data
public class IncidentDTO {
    private String description;
    private IncidentType type;
    private int severity;
    private Double longitude;
    private Double latitude;
}
