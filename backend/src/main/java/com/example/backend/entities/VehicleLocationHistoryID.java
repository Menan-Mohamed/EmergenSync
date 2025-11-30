package com.example.backend.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@Data
public class VehicleLocationHistoryID implements Serializable {
    private Integer vehicleID;
    private LocalDateTime timeStamp;
}
