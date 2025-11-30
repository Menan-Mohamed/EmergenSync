package com.example.backend.services;

import com.example.backend.entities.Assignment;
import com.example.backend.entities.AssignmentID;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.IncidentStatus;

import com.example.backend.repositories.AssignmentRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.repositories.VehicleRepository;

import com.example.backend.utils.HaversineFormula;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    @Transactional
    public void assignVehicle(Vehicle vehicle, Incident incident) {

        vehicle.setStatus(VehicleStatus.ON_ROUTE);
        vehicleRepository.save(vehicle);

        incident.setStatus(IncidentStatus.ASSIGNED);
        incidentRepository.save(incident);

        AssignmentID assignmentId = new AssignmentID();
        assignmentId.setVehicleID(vehicle.getId());
        assignmentId.setIncidentID(incident.getId());

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setVehicle(vehicle);
        assignment.setIncident(incident);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);

    }

    public void checkIfVehicleReachedIncident(Integer vehicleId, BigDecimal lat, BigDecimal lon){

        Assignment assignment = assignmentRepository.findActiveAssignmentByVehicle(vehicleId);
        if (assignment == null) return;

        Incident incident = assignment.getIncident();

        assignment.setSolvedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        incident.setStatus(IncidentStatus.RESOLVED);
        incidentRepository.save(incident);

        Vehicle vehicle = assignment.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);


    }

    private boolean hasReached(BigDecimal vehicleLatitude, BigDecimal vehicleLongitude,
                               BigDecimal incidentLatitude, BigDecimal incidentLongitude) {

        double distance = haversineFormula.haversine(
                vehicleLatitude.doubleValue(), vehicleLongitude.doubleValue(),
                incidentLatitude.doubleValue(), incidentLongitude.doubleValue()
        );

        return distance < 0.05; //50m
    }

}
