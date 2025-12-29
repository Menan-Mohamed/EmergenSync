package com.example.backend.repositories;

import com.example.backend.entities.Notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderBySentAtDesc(Integer userId);
    
    List<Notification> findByUserIdAndReadFalseOrderBySentAtDesc(Integer userId);
    
    List<Notification> findByUserIdAndReadFalse(Integer userId);
    
    Long countByUserIdAndReadFalse(Integer userId);
    
    List<Notification> findByType(String type);
    
    List<Notification> findByCategory(String category);
    
    void deleteByUserId(Integer userId);
}
