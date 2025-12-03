package com.example.backend.services;


import com.example.backend.dtos.IncidentDTO;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.enums.IncidentType;
import com.example.backend.mapper.IncidentMapper;
import com.example.backend.repositories.IncidentRepository;
import static com.example.backend.enums.IncidentStatus.REPORTED;

import io.swagger.v3.oas.models.security.SecurityScheme;
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

   public Incident createIncident(IncidentDTO incidentdto){

       IncidentMapper incidentMapper = new IncidentMapper();
       Incident incident = incidentMapper.incidentDtoToincident(incidentdto);
       incident.setStatus(REPORTED);
       incident.setReportedAt(LocalDateTime.now());
       Incident savedIncident = incidentRepository.save(incident);

       dispatchService.autoAssign(savedIncident);

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
        return incidentRepository.save(incident);
    }
}
