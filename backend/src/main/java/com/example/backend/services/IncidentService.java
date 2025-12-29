package com.example.backend.services;


import com.example.backend.dtos.IncidentDTO;
import com.example.backend.entities.Incident;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.mapper.IncidentMapper;
import com.example.backend.repositories.IncidentRepository;
import static com.example.backend.enums.IncidentStatus.REPORTED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private WebSocketPublisherService publisherService;

   @Autowired
    private NotificationsService notificationsService;

   public Incident createIncident(IncidentDTO incidentdto){

       IncidentMapper incidentMapper = new IncidentMapper();
       Incident incident = incidentMapper.incidentDtoToincident(incidentdto);
       incident.setStatus(REPORTED);
       incident.setReportedAt(LocalDateTime.now());
       Incident savedIncident = incidentRepository.save(incident);

       dispatchService.autoAssign(savedIncident);
       publisherService.sendIncidentUpdate(savedIncident);

       notificationsService.sendSystemAlert(
            "New incident reported: " + savedIncident.getId(),
            "INCIDENT"
        );

       return savedIncident;
   }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Incident getIncidentById(Integer id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + id));
    }

    public Incident updateIncidentState(Integer id, String state) {

        Incident incident = getIncidentById(id);

        IncidentStatus newStatus;

        try {
            newStatus = IncidentStatus.valueOf(state.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid incident status: " + state);
        }

        incident.setStatus(newStatus);

        if (newStatus != IncidentStatus.REPORTED) {
            incident.setNotificationSent(true);
        }
        
        return incidentRepository.save(incident);
    }
}
