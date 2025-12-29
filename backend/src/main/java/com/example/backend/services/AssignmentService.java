package com.example.backend.services;

import com.example.backend.dtos.AssignmentDto;
import com.example.backend.dtos.CachedRoute;
import com.example.backend.dtos.Point;
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
import com.example.backend.utils.OsrmRouting;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

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
    private OsrmRouting routingFind;

    @Autowired
    private RedisTemplate<String, CachedRoute> redisTemplate;

    @Autowired
    private NotificationsService notificationsService;

    @Transactional
    public synchronized void assignVehicle(Vehicle vehicle, Incident incident) {
        logger.info("Thread {} - Attempting to assign vehicle {} to incident {}",
                Thread.currentThread().getName(), vehicle.getId(), incident.getId());

        // Re-fetch vehicle with lock to ensure we have the latest state
        Vehicle lockedVehicle = (Vehicle) vehicleRepository.findByIdWithLock(vehicle.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicle.getId()));

        // Re-fetch incident with lock
        Incident lockedIncident = (Incident) incidentRepository.findByIdWithLock(incident.getId())
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incident.getId()));

        // Check if vehicle is actually available
        if (lockedVehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available for assignment. Status: " + lockedVehicle.getStatus());
        }

        // Check if incident is in a valid state for assignment
        if (lockedIncident.getStatus() != IncidentStatus.REPORTED) {
            throw new RuntimeException("Incident cannot be assigned - current status: " + lockedIncident.getStatus());
        }

        // Check if vehicle already has an active assignment
        Assignment existingAssignment = assignmentRepository.findActiveAssignmentByVehicle(lockedVehicle.getId());
        if (existingAssignment != null) {
            throw new RuntimeException("Vehicle already has an active assignment: " + lockedVehicle.getId());
        }

        // Verify vehicle type matches incident type
        if (!isVehicleTypeMatchesIncident(lockedVehicle, lockedIncident)) {
            throw new RuntimeException(
                    "Vehicle type mismatch! Cannot assign " + lockedVehicle.getType() +
                            " vehicle to " + lockedIncident.getType() + " incident."
            );
        }

        lockedVehicle.setStatus(VehicleStatus.ON_ROUTE);
        vehicleRepository.save(lockedVehicle);

        lockedIncident.setStatus(IncidentStatus.ASSIGNED);
        incidentRepository.save(lockedIncident);

        AssignmentID assignmentId = new AssignmentID();
        assignmentId.setVehicleID(lockedVehicle.getId());
        assignmentId.setIncidentID(lockedIncident.getId());

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setVehicle(lockedVehicle);
        assignment.setIncident(lockedIncident);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);

        logger.info("Successfully assigned vehicle {} to incident {}",
                lockedVehicle.getId(), lockedIncident.getId());

        // Trigger simulation asynchronously
        triggerSimulationAsync(lockedVehicle, lockedIncident);
    }

    @Async("dispatchExecutor")
    public void triggerSimulationAsync(Vehicle vehicle, Incident incident) {
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
            logger.info("Simulation triggered for vehicle {}", vehicle.getId());
        } catch(Exception e){
            logger.error("Failed to trigger simulation for vehicle {}: {}",
                    vehicle.getId(), e.getMessage());
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

        String redisKey = "route:" + vehicleId;
        redisTemplate.delete(redisKey);

        // Check waiting Incidents
        assignWaitingIncidents(vehicle);

        notificationsService.sendSystemAlert(
            "Incident updated: " + incident.getId(),
            "INCIDENT"
        );

        // Check waiting Incidents asynchronously - PASS VEHICLE ID, NOT ENTITY
        assignWaitingIncidentsByVehicleIdAsync(vehicleId);
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

    // FIX: Accept vehicle ID instead of detached entity
    @Async("dispatchExecutor")
    @Transactional
    public CompletableFuture<Void> assignWaitingIncidentsByVehicleIdAsync(Integer vehicleId){
        logger.info("Checking waiting incidents for vehicle {}", vehicleId);

        // Re-fetch vehicle in this transaction
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        if (vehicle == null || vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            logger.warn("Vehicle {} not available for assignment", vehicleId);
            return CompletableFuture.completedFuture(null);
        }

        Incident waitingIncident = incidentRepository.findMostSevereReportedByType(
                vehicle.getType().toString()
        );

        if(waitingIncident == null){
            logger.info("No waiting incidents for vehicle type {}", vehicle.getType());
            return CompletableFuture.completedFuture(null);
        }

        try {
            assignVehicle(vehicle, waitingIncident);
        } catch (RuntimeException e) {
            logger.error("Failed to assign waiting incident: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    // Keep backward compatibility but deprecate
    @Deprecated
    @Async("dispatchExecutor")
    @Transactional
    public CompletableFuture<Void> assignWaitingIncidentsAsync(Vehicle availableVehicle){
        return assignWaitingIncidentsByVehicleIdAsync(availableVehicle.getId());
    }

    // Synchronous version
    public void assignWaitingIncidents(Integer vehicleId){
        assignWaitingIncidentsByVehicleIdAsync(vehicleId).join();
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