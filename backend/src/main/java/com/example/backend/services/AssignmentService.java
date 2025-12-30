package com.example.backend.services;

import com.example.backend.dtos.AssignmentDto;
import com.example.backend.entities.Assignment;
import com.example.backend.entities.AssignmentID;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.events.VehicleCreatedEvent;
import com.example.backend.events.VehicleAvailableEvent;

import com.example.backend.mapper.AssignmentMapper;
import com.example.backend.mapper.VehicleMapper;
import com.example.backend.repositories.AssignmentRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.repositories.VehicleRepository;

import com.example.backend.utils.HaversineFormula;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final AssignmentService self;

    @Autowired
    public AssignmentService(@Lazy AssignmentService self) {
        this.self = self;
    }

    @Transactional(propagation = Propagation.MANDATORY, timeout = 5)
    public void assignVehicle(Vehicle vehicle, Incident incident) {
        try {
            if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
                throw new RuntimeException("Vehicle is not available for assignment. Status: " + vehicle.getStatus());
            }

            if (incident.getStatus() != IncidentStatus.REPORTED) {
                throw new RuntimeException("Incident cannot be assigned - current status: " + incident.getStatus());
            }

            Assignment existingAssignment = assignmentRepository.findActiveAssignmentByVehicle(vehicle.getId());
            if (existingAssignment != null) {
                throw new RuntimeException("Vehicle already has an active assignment: " + vehicle.getId());
            }

            if (!isVehicleTypeMatchesIncident(vehicle, incident)) {
                throw new RuntimeException(
                        "Vehicle type mismatch! Cannot assign " + vehicle.getType() +
                                " vehicle to " + incident.getType() + " incident."
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

            socketPublisherService.sendVehicleLocation(vehicleMapper.toDto(vehicle));
            socketPublisherService.sendIncidentUpdate(incident);

            triggerSimulationAsync(vehicle, incident);

        } catch (RuntimeException e) {
            throw e;
        }
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
        } catch(Exception e){
            // Simulation service error - continue silently
        }
    }

    @Transactional(timeout = 5)
    public void checkIfVehicleReachedIncident(Integer vehicleId, Double lat, Double lon){
        try {
            Assignment assignment = assignmentRepository.findActiveAssignmentByVehicle(vehicleId);
            if (assignment == null) {
                return;
            }

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

            notificationsService.sendSystemAlert(
                    "Incident updated: " + incident.getId(),
                    "INCIDENT"
            );

            socketPublisherService.sendIncidentUpdate(incident);
            socketPublisherService.sendVehicleLocation(vehicleMapper.toDto(vehicle));

            eventPublisher.publishEvent(new VehicleAvailableEvent(vehicleId));
        } catch (Exception e) {
            // Error handling - continue silently
        }

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
        return distance < 0.05;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        try {
            self.assignWaitingIncidentsByVehicleIdAsync(event.getVehicleId());
        } catch (Exception e) {
            // Error handling - continue silently
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVehicleAvailable(VehicleAvailableEvent event) {
        try {
            self.assignWaitingIncidentsByVehicleIdAsync(event.getVehicleId());
        } catch (Exception e) {
            // Error handling - continue silently
        }
    }

    @Async("dispatchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> assignWaitingIncidentsByVehicleIdAsync(Integer vehicleId){
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        if (vehicle == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            return CompletableFuture.completedFuture(null);
        }

        Incident waitingIncident = incidentRepository.findMostSevereReportedByType(
                vehicle.getType().toString()
        );

        if(waitingIncident == null){
            return CompletableFuture.completedFuture(null);
        }

        try {
            Vehicle lockedVehicle = vehicleRepository.findByIdWithLock(vehicleId).orElse(null);
            if (lockedVehicle == null || lockedVehicle.getStatus() != VehicleStatus.AVAILABLE) {
                return CompletableFuture.completedFuture(null);
            }

            Incident lockedIncident = incidentRepository.findByIdWithLock(waitingIncident.getId()).orElse(null);
            if (lockedIncident == null || lockedIncident.getStatus() != IncidentStatus.REPORTED) {
                return CompletableFuture.completedFuture(null);
            }

            assignVehicle(lockedVehicle, lockedIncident);
        } catch (RuntimeException e) {
            // Error handling - continue silently
        }

        return CompletableFuture.completedFuture(null);
    }

    @Deprecated
    @Async("dispatchExecutor")
    @Transactional
    public CompletableFuture<Void> assignWaitingIncidentsAsync(Vehicle availableVehicle){
        return assignWaitingIncidentsByVehicleIdAsync(availableVehicle.getId());
    }

    public void assignWaitingIncidents(Integer vehicleId){
        self.assignWaitingIncidentsByVehicleIdAsync(vehicleId).join();
    }

    public List<AssignmentDto> getAllAssignment(){
        List<Assignment> assignments = assignmentRepository.findAll();
        List<AssignmentDto> assignmentDtos = new ArrayList<>();
        for(Assignment i : assignments){
            assignmentDtos.add(assignmentMapper.assigmenttoassignmentDto(i));
        }
        return assignmentDtos;
    }
}