package com.example.backend.mapper;

import com.example.backend.dtos.IncidentDTO;
import com.example.backend.dtos.authDTO.UserDTO;
import com.example.backend.entities.Incident;
import com.example.backend.entities.User;

public class IncidentMapper {
    public Incident incidentDtoToincident(IncidentDTO newIncident) {
        Incident incident = new Incident();
        incident.setDescription(newIncident.getDescription());
        incident.setType(newIncident.getType());
        incident.setLatitude(newIncident.getLatitude());
        incident.setLongitude(newIncident.getLongitude());
        incident.setSeverity(newIncident.getSeverity());
        return incident;
    }
}
