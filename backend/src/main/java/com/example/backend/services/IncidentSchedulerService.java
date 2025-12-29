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

    /**
     * Runs every 30 seconds to check for unassigned incidents
     * that have been reported for more than 2 minutes
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void checkUnassignedIncidents() {
        try {
            // Get current time minus 2 minutes
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
            
            // Find incidents that are:
            // 1. Still in REPORTED status (not assigned)
            // 2. Reported more than 2 minutes ago
            List<Incident> unassignedIncidents = incidentRepository
                .findByStatusAndReportedAtBefore(IncidentStatus.REPORTED, twoMinutesAgo);
            
            if (!unassignedIncidents.isEmpty()) {
                System.out.println("⚠️ Found " + unassignedIncidents.size() + 
                    " unassigned incidents older than 2 minutes");
                
                for (Incident incident : unassignedIncidents) {
                    // Check if we've already sent a notification for this incident
                    // to avoid spamming admins
                    if (!incident.getNotificationSent()) {
                        sendUnassignedAlert(incident);
                        
                        // Mark that we've sent notification for this incident
                        incident.setNotificationSent(true);
                        incidentRepository.save(incident);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in incident scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send alert notification for unassigned incident
     */
    private void sendUnassignedAlert(Incident incident) {
        // Format location from coordinates
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

    /**
     * Optional: Method to manually trigger the check
     * Useful for testing
     */
    public void checkNow() {
        System.out.println("🔍 Manual check triggered");
        checkUnassignedIncidents();
    }
}