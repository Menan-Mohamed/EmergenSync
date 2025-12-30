package com.example.backend.services;

import com.example.backend.entities.Incident;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.repositories.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentSchedulerService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private NotificationsService notificationsService;

    @Scheduled(fixedRate = 30000)
    public void checkUnassignedIncidents() {
        try {
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
            
            List<Incident> unassignedIncidents = incidentRepository
                .findByStatusAndReportedAtBefore(IncidentStatus.REPORTED, twoMinutesAgo);
            
            if (!unassignedIncidents.isEmpty()) {
                for (Incident incident : unassignedIncidents) {
                    if (!incident.getNotificationSent()) {
                        sendUnassignedAlert(incident);
                        
                        incident.setNotificationSent(true);
                        incidentRepository.save(incident);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in incident scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendUnassignedAlert(Incident incident) {
        String location = String.format("(%.4f, %.4f)", 
            incident.getLatitude(), 
            incident.getLongitude()
        );
        
        String message = String.format(
            "⚠️ URGENT: Incident #%d (%s - Severity %d) has not been assigned for over 2 minutes! Location: %s",
            incident.getId(),
            incident.getType().toString(),
            incident.getSeverity(),
            location
        );
        
        System.out.println("📢 " + message);
        
        notificationsService.sendSystemAlert(
            message,
            "WARNING"
        );
    }

    public void checkNow() {
        System.out.println("🔍 Manual check triggered");
        checkUnassignedIncidents();
    }
}