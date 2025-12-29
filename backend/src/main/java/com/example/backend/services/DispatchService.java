package com.example.backend.services;

import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleType;
import com.example.backend.repositories.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DispatchService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * Concurrency-safe auto assignment.
     */
    @Transactional
    public void autoAssign(Incident incident) {

        VehicleType type = mapIncidentTypeToVehicleType(incident.getType());

        Integer vehicleId = vehicleRepository.findNearestAvailableForUpdate(
                type.name(),
                incident.getLatitude(),
                incident.getLongitude()
        );

        if (vehicleId == null) return;

        int claimed = vehicleRepository.claimById(vehicleId);
        if (claimed == 0) return;

        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) return;

        assignmentService.assignVehicle(vehicle, incident);
    }

    private VehicleType mapIncidentTypeToVehicleType(IncidentType incidentType) {
        return switch (incidentType) {
            case FIRE -> VehicleType.FIRE;
            case POLICE -> VehicleType.POLICE;
            default -> VehicleType.MEDICAL;
        };
    }
}
