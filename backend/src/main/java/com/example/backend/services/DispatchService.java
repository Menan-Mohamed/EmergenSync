package com.example.backend.services;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleType;
import com.example.backend.events.IncidentCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.utils.HaversineFormula;
import com.example.backend.dtos.VehicleDispatchDto;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.mapper.VehicleMapper;

@Service
public class DispatchService {

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLHRepo;

    @Autowired
    private HaversineFormula haversineFormula;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleMapper vehicleMapper;

    private final DispatchService self;

    @Autowired
    public DispatchService(@Lazy DispatchService self) {
        this.self = self;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIncidentCreated(IncidentCreatedEvent event) {
        try {
            Incident incident = incidentRepository.findById(event.getIncidentId()).orElse(null);

            if (incident == null) {
                return;
            }

            self.autoAssignAsync(incident);

        } catch (Exception e) {
            // Error handling - continue silently
        }
    }

    @Async("dispatchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 15)
    public CompletableFuture<Boolean> autoAssignAsync(Incident incident) {
        try {
            VehicleType vehicleType = mapIncidentTypeToVehicleType(incident.getType());

            List<Vehicle> available = vehicleRepo.findAvailableByTypeWithLock(vehicleType);

            if (available == null || available.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            VehicleDispatchDto nearestVehicleDto = findNearestVehicle(available, incident);

            if(nearestVehicleDto == null) {
                return CompletableFuture.completedFuture(false);
            }

            Vehicle lockedVehicle = vehicleRepo.findByIdWithLock(nearestVehicleDto.getId()).orElse(null);

            if(lockedVehicle == null) {
                return CompletableFuture.completedFuture(false);
            }

            Incident lockedIncident = incidentRepository.findByIdWithLock(incident.getId()).orElse(null);

            if(lockedIncident == null) {
                return CompletableFuture.completedFuture(false);
            }

            assignmentService.assignVehicle(lockedVehicle, lockedIncident);

            return CompletableFuture.completedFuture(true);

        } catch (org.springframework.dao.CannotAcquireLockException e) {
            return CompletableFuture.completedFuture(false);
        } catch (RuntimeException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Transactional
    public void autoAssign(Incident incident) {
        self.autoAssignAsync(incident);
    }

    public VehicleDispatchDto findNearestVehicle(List<Vehicle> vehicles, Incident incident) {
        return vehicles.stream()
                .filter(vehicle -> vehicle.getLatitude() != null && vehicle.getLongitude() != null)
                .map(vehicle -> {
                    double distance = haversineFormula.haversine(
                            vehicle.getLatitude(),
                            vehicle.getLongitude(),
                            incident.getLatitude(),
                            incident.getLongitude()
                    );
                    return vehicleMapper.toDispatchDto(vehicle, distance);
                })
                .min(Comparator.comparing(VehicleDispatchDto::getDistanceToIncident))
                .orElse(null);
    }

    private VehicleType mapIncidentTypeToVehicleType(IncidentType incidentType) {
        switch (incidentType) {
            case FIRE:
                return VehicleType.FIRE;
            case POLICE:
                return VehicleType.POLICE;
            default:
                return VehicleType.MEDICAL;
        }
    }
}