package com.example.backend.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationDto {
    private String type;
    private String category;
    private String msg;
    private LocalDateTime sentAt;
    private Boolean read;
}