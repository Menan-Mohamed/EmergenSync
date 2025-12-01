package com.example.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Data
public class VehicleLocationHistoryID implements Serializable {
    
    @Column(name = "vehicleID")
    private Integer vehicleID;

    @Column(name = "timeStamp")
    private LocalDateTime timeStamp;
}
