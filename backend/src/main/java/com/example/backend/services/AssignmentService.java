package com.example.backend.services;

import com.example.backend.dtos.AssignmentDto;
import com.example.backend.entities.Assignment;
import com.example.backend.entities.AssignmentID;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.mapper.AssignmentMapper;
import com.example.backend.repositories.AssignmentRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.utils.HaversineFormula;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private HaversineFormula haversineFormula;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Transactional
    public void assignVehicle(Vehicle vehicle, Incident incident) {

        // ✅ Vehicle must already be claimed
        if (vehicle.getStatus() != VehicleStatus.ON_ROUTE_PENDING) {
            return;
        }

        if (incident.getStatus() != IncidentStatus.REPORTED) {
            return;
        }

        Assignment existing = assignmentRepository.findActiveAssignmentByVehicle(vehicle.getId());
        if (existing != null) {
            return;
        }

        // Finalize state transition
        vehicle.setStatus(VehicleStatus.ON_ROUTE);
        vehicleRepository.save(vehicle);

        incident.setStatus(IncidentStatus.ASSIGNED);
        incidentRepository.save(incident);

        AssignmentID id = new AssignmentID();
        id.setVehicleID(vehicle.getId());
        id.setIncidentID(incident.getId());

        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setVehicle(vehicle);
        assignment.setIncident(incident);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);
    }

    // -------- rest of your service unchanged --------

    public void checkIfVehicleReachedIncident(Integer vehicleId, Double lat, Double lon) {

        Assignment assignment = assignmentRepository.findActiveAssignmentByVehicle(vehicleId);
        if (assignment == null) return;

        Incident incident = assignment.getIncident();

        if (!hasReached(lat, lon, incident.getLatitude(), incident.getLongitude())) return;

        assignment.setSolvedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        incident.setStatus(IncidentStatus.RESOLVED);
        incidentRepository.save(incident);

        Vehicle vehicle = assignment.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        assignWaitingIncidents(vehicle);
    }

    private boolean hasReached(Double vLat, Double vLon, Double iLat, Double iLon) {
        return haversineFormula.haversine(vLat, vLon, iLat, iLon) < 0.05;
    }

    public void assignWaitingIncidents(Vehicle vehicle) {
        Incident incident = incidentRepository
                .findMostSevereReportedByType(vehicle.getType().toString());
        if (incident != null) {
            assignVehicle(vehicle, incident);
        }
    }

    public List<AssignmentDto> getAllAssignment() {
        List<AssignmentDto> result = new ArrayList<>();
        for (Assignment a : assignmentRepository.findAll()) {
            result.add(assignmentMapper.assigmenttoassignmentDto(a));
        }
        return result;
    }
}
