package com.example.backend.mapper;

import com.example.backend.dtos.AssignmentDto;
import com.example.backend.dtos.IncidentDTO;
import com.example.backend.entities.Assignment;
import com.example.backend.entities.Incident;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {
    public AssignmentDto assigmenttoassignmentDto(Assignment assignment) {
        AssignmentDto assignmentDto=new AssignmentDto();
        assignmentDto.setIncidentId(assignment.getIncident().getId());
        assignmentDto.setVehicleId(assignment.getVehicle().getId());
        assignmentDto.setAssignedAt(assignment.getAssignedAt());
        assignmentDto.setSolvedAt(assignment.getSolvedAt());
        return assignmentDto;
    }
}
