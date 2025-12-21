package com.example.backend.services;


import com.example.backend.dtos.VehicleDto;
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
}
