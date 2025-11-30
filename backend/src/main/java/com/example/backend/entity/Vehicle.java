package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicleID")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false)
    private VehicleType type;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User responder;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private VehicleStatus status;

    @Column(name="lastUpdate")
    private LocalDateTime lastUpdate;


    public enum VehicleType {
        medical, fire, police
    }

    public enum VehicleStatus {
        available, on_route, busy, maintenance
    }
}
