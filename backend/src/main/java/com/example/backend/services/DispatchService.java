package com.example.backend.services;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.utils.HaversineFormula;
import com.example.backend.dtos.VehicleDispatchDto;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.mapper.VehicleMapper;

@Service
public class DispatchService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLHRepo;

    @Autowired
    private HaversineFormula haversineFormula;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Async("dispatchExecutor")
    @Transactional
    public CompletableFuture<Boolean> autoAssignAsync(Incident incident) {
        logger.info("Thread {} - Starting auto-assign for incident {}",
                Thread.currentThread().getName(), incident.getId());

        try {
            VehicleType vehicleType = mapIncidentTypeToVehicleType(incident.getType());

            // Use locked query to get available vehicles
            List<Vehicle> available = vehicleRepo.findAvailableByTypeWithLock(vehicleType);

            if (available == null || available.isEmpty()) {
                logger.warn("No available vehicles of type {} for incident {}",
                        vehicleType, incident.getId());
                return CompletableFuture.completedFuture(false);
            }

            VehicleDispatchDto nearestVehicleDto = findNearestVehicle(available, incident);

            if(nearestVehicleDto == null) {
                logger.warn("Could not find nearest vehicle for incident {}", incident.getId());
                return CompletableFuture.completedFuture(false);
            }

            Vehicle nearest = (Vehicle) vehicleRepo.findByIdWithLock(nearestVehicleDto.getId())
                    .orElse(null);

            if(nearest == null) {
                logger.warn("Vehicle {} not found", nearestVehicleDto.getId());
                return CompletableFuture.completedFuture(false);
            }

            assignmentService.assignVehicle(nearest, incident);
            logger.info("Successfully assigned vehicle {} to incident {}",
                    nearest.getId(), incident.getId());
            return CompletableFuture.completedFuture(true);

        } catch (RuntimeException e) {
            logger.error("Failed to assign vehicle to incident {}: {}",
                    incident.getId(), e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // Synchronous version for backward compatibility
    @Transactional
    public void autoAssign(Incident incident) {
        autoAssignAsync(incident).join(); // Wait for completion
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