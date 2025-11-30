package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_location_history")
@Data
public class VehicleLocationHistory {

    @EmbeddedId
    private VehicleLocationHistoryID id;

    @ManyToOne
    @MapsId("vehicleID")
    @JoinColumn(name = "vehicleID")
    private Vehicle vehicle;

    @Column(name="longitude")
    private Double longitude;

    @Column(name="latitude" )
    private Double latitude;

}
