package com.example.backend.services;


import com.example.backend.dtos.NotificationDto;
import com.example.backend.dtos.VehicleDto;
import com.example.backend.entities.Notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketPublisherService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPublisherService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendVehicleLocation(VehicleDto update) {
        messagingTemplate.convertAndSend("/topic/vehicles", update);
    }

    public void sendAdminNotification(Integer adminId, Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setType(notification.getType());
        dto.setCategory(notification.getCategory());
        dto.setMsg(notification.getMsg());
        dto.setSentAt(notification.getSentAt());
        dto.setRead(notification.getRead());
        
        messagingTemplate.convertAndSend(
            "/topic/admin-notifications/" + adminId,
            dto
        );
    }
}
