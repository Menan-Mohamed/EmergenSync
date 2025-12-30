package com.example.backend.services;


import com.example.backend.entities.Notification;
import com.example.backend.entities.User;
import com.example.backend.enums.UserRole;
import com.example.backend.repositories.NotificationRepository;
import com.example.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class NotificationsService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketPublisherService publisher;

    public void sendSystemAlert(String msg, String type) {
        List<User> admins = userRepository.findByRole(UserRole.SYSTEM_ADMIN);

        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setType(type != null ? type : "SYSTEM");
            notification.setCategory("SYSTEM_ALERT");
            notification.setMsg(msg);
            notification.setRead(false);
            notification.setSentAt(LocalDateTime.now());

            Notification saved = notificationRepository.save(notification);
            publisher.sendAdminNotification(admin.getId(), saved);

        }
    }

    public List<Notification> getAdminNotifications(Integer adminId) {
        return notificationRepository
            .findByUserIdOrderBySentAtDesc(adminId);
    }

   
}
