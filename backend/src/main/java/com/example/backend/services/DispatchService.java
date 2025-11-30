package com.example.backend.services;

import java.util.Comparator;
import java.util.List;

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
        List<Vehicle> available = vehicleRepo.findAvailableByType((incident.getType()));

        Vehicle nearest = findNearestVehicle(available, incident);

        if(nearest == null) return;

        assignmentService.assignVehicle(nearest, incident);
    }

    public Vehicle findNearestVehicle(List<Vehicle> vehicles, Incident incident){
        return vehicles.stream()
                .min(Comparator.comparing(vehicle -> {
                    VehicleLocationHistory latest = vehicleLHRepo.findLatestLocation(vehicle.getId());
                    if(latest == null) return Double.MAX_VALUE;
                    return haversineFormula.haversine(
                        latest.getLatitude(),
                        latest.getLongitude(),
                        incident.getLatitude(),
                        incident.getLongitude()
                    );
                }))
                .orElse(null);
    }
    
}
