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


        //check if vehicle is actually available
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available for assignment. Status: " + vehicle.getStatus());
        }

        //check if incident is in a valid state for assignment
        if (incident.getStatus() != IncidentStatus.REPORTED) {
            throw new RuntimeException("Incident cannot be assigned - current status: " + incident.getStatus());
        }

        //check if vehicle already has an active assignment
        Assignment existingAssignment = assignmentRepository.findActiveAssignmentByVehicle(vehicle.getId());
        if (existingAssignment != null) {
            throw new RuntimeException("Vehicle already has an active assignment: " + vehicle.getId());
        }

        //verify vehicle type matches incident type
        if (!isVehicleTypeMatchesIncident(vehicle, incident)) {
            throw new RuntimeException(
                    "Vehicle type mismatch! Cannot assign " + vehicle.getType() +
                            " vehicle to " + incident.getType() + " incident. Vehicle ID: " +
                            vehicle.getId() + ", Incident ID: " + incident.getId()
            );
        }

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

    public void checkIfVehicleReachedIncident(Integer vehicleId, Double lat, Double lon){

        Assignment assignment = assignmentRepository.findActiveAssignmentByVehicle(vehicleId);
        if (assignment == null) return;

        Incident incident = assignment.getIncident();

        if (!hasReached(lat, lon, incident.getLatitude(), incident.getLongitude())) {
            return;
        }

        assignment.setSolvedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        incident.setStatus(IncidentStatus.RESOLVED);
        incidentRepository.save(incident);

        Vehicle vehicle = assignment.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);


    }

    private boolean hasReached(Double vehicleLatitude, Double vehicleLongitude,
                               Double incidentLatitude, Double incidentLongitude) {

        double distance = haversineFormula.haversine(
                vehicleLatitude, vehicleLongitude,
                incidentLatitude, incidentLongitude
        );

        return distance < 0.05; //50m
    }

    private boolean isVehicleTypeMatchesIncident(Vehicle vehicle, Incident incident) {
        switch (incident.getType()) {
            case FIRE:
                return vehicle.getType().name().equals("FIRE");
            case POLICE:
                return vehicle.getType().name().equals("POLICE");
            case MEDICAL:
                return vehicle.getType().name().equals("MEDICAL");
            default:
                return false;
        }
    }


}