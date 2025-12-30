package com.example.backend.entities;


import com.example.backend.enums.IncidentStatus;
import com.example.backend.enums.IncidentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incidentID")
    private Integer id;

    @Column(name= "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false)
    private IncidentType type;

    @Column(name="severity")
    private int severity;

    @Column(name="longitude", nullable = false)
    private Double longitude;

    @Column(name="latitude", nullable = false)
    private Double latitude;

    @Column(name="reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name="notification_sent", nullable = false)
    private Boolean notificationSent = false;

}
