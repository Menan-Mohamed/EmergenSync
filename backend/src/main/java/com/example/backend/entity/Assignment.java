package com.example.backend.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Data
public class Assignment {

    @EmbeddedId
    private AssignmentID id;

    @ManyToOne
    @MapsId("vehicleID") // maps composite key
    @JoinColumn(name = "vehicleID")
    private Vehicle vehicle;

    @ManyToOne
    @MapsId("incidentID") // maps composite key
    @JoinColumn(name = "incidentID")
    private Incident incident;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;
}