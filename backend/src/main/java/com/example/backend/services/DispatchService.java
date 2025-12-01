package com.example.backend.services;

import java.util.Comparator;
import java.util.List;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.utils.HaversineFormula;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;


@Service
public class DispatchService {

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLHRepo;

    @Autowired
    private HaversineFormula haversineFormula;

    @Autowired
    private AssignmentService assignmentService;

    public void autoAssign(Incident incident){

        VehicleType vehicleType = mapIncidentTypeToVehicleType(incident.getType());


        List<Vehicle> available = vehicleRepo.findAvailableByType(vehicleType);

        Vehicle nearest = findNearestVehicle(available, incident);

        if(nearest == null) return;

        assignmentService.assignVehicle(nearest, incident);
    }

    public Vehicle findNearestVehicle(List<Vehicle> vehicles, Incident incident) {

        return vehicles.stream()
                .min(Comparator.comparing(vehicle -> {

                    // Fetch latest location safely
                    VehicleLocationHistory latest =
                            vehicleLHRepo.findFirstByIdVehicleIDOrderByIdTimeStampDesc(vehicle.getId());

                    // If the vehicle has no location history → ignore by setting huge distance
                    if (latest == null || latest.getLatitude() == null || latest.getLongitude() == null) {
                        return Double.MAX_VALUE;
                    }

                    // Calculate Haversine distance
                    return haversineFormula.haversine(
                            latest.getLatitude(),
                            latest.getLongitude(),
                            incident.getLatitude(),
                            incident.getLongitude()
                    );
                }))
                .orElse(null);
    }

    private VehicleType mapIncidentTypeToVehicleType(IncidentType incidentType) {
        switch (incidentType) {
            case FIRE:
                return VehicleType.FIRE;
            case MEDICAL:
                return VehicleType.MEDICAL;
            case POLICE:
                return VehicleType.POLICE;
            default:
                return VehicleType.MEDICAL;
        }
    }


}
