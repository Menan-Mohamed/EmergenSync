package com.example.backend.entities;

import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "vehicles")
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



}
