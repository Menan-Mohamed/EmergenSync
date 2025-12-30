package com.example.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "vehicle_location_history")
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
