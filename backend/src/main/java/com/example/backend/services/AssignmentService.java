package com.example.backend.services;

import com.example.backend.dtos.AssignmentDto;
import com.example.backend.entities.Assignment;
import com.example.backend.entities.AssignmentID;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.IncidentStatus;

import com.example.backend.mapper.AssignmentMapper;
import com.example.backend.mapper.VehicleMapper;
import com.example.backend.repositories.AssignmentRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.repositories.VehicleRepository;

import com.example.backend.utils.HaversineFormula;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @Autowired
    private WebSocketPublisherService socketPublisherService;

    @Autowired
    private VehicleMapper vehicleMapper;


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

        String pythonUrl = "http://localhost:8000/simulate";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("vehicleId", vehicle.getId());
        body.put("startLat", vehicle.getLatitude());
        body.put("startLon", vehicle.getLongitude());
        body.put("endLat", incident.getLatitude());
        body.put("endLon", incident.getLongitude());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForObject(pythonUrl, request, String.class);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
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

        //Publish Updates
        socketPublisherService.sendIncidentUpdate(incident);
        socketPublisherService.sendVehicleLocation(vehicleMapper.toDto(vehicle));

        // Check waiting Incidents
        assignWaitingIncidents(vehicle);

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


    public void assignWaitingIncidents(Vehicle availableVehicle){
        Incident waitingIncident = incidentRepository.findMostSevereReportedByType(availableVehicle.getType().toString());
        if(waitingIncident == null){
            return;
        }
        assignVehicle(availableVehicle, waitingIncident);
    }

    public List<AssignmentDto> getAllAssignment (){
        List<Assignment> assignments = assignmentRepository.findAll();
        List<AssignmentDto> assignmentDtos = new ArrayList<>();
        for(Assignment i : assignments){
            assignmentDtos.add(assignmentMapper.assigmenttoassignmentDto(i));
        }
        return assignmentDtos;
    }


}