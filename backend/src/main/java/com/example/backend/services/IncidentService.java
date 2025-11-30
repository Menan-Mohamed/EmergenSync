package com.example.backend.services;


import com.example.backend.entities.Incident;
import com.example.backend.repositories.IncidentRepository;
import static com.example.backend.enums.IncidentStatus.REPORTED;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

   @Autowired
   private DispatchService dispatchService;

   public Incident createIncident(Incident incident){

       incident.setStatus(REPORTED);
       incident.setReportedAt(LocalDateTime.now());
       Incident savedIncident = incidentRepository.save(incident);

       dispatchService.autoAssign(savedIncident);

       return savedIncident;
   }
}
