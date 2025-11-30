package com.example.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name= "type", length = 50)
    private String type;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name="msg", length = 500, nullable = false)
    private String msg;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;
}