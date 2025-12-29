package com.example.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.backend.entities.Notification;
import com.example.backend.services.NotificationsService;
import java.util.List;



@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationsController {

    @Autowired
    private NotificationsService notificationsService;

    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<Notification>> getAdminNotifications(
        @PathVariable Integer adminId
    ) {
        return ResponseEntity.ok(
            notificationsService.getAdminNotifications(adminId)
        );
    }

}