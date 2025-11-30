package com.example.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;


import java.io.Serializable;

@Embeddable
@Data
public class AssignmentID implements Serializable {
    private Integer vehicleID;
    private Integer incidentID;
}