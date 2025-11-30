package com.example.backend.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.entities.VehicleLocationHistoryID;
import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.repositories.VehicleRepository;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLHRepo;

    @Autowired
    private AssignmentService assignmentService;

    public void updateVehicleLocation(Integer vehicleId, Double latitude, Double longitude){
        Vehicle vehicle = vehicleRepo.findById(vehicleId).orElseThrow();

        VehicleLocationHistoryID id = new VehicleLocationHistoryID(vehicleId, LocalDateTime.now());
        
        VehicleLocationHistory locationHistory = VehicleLocationHistory.builder()
                .id(id)
                .vehicle(vehicle)
                .latitude(latitude.doubleValue())
                .longitude(longitude.doubleValue())
                .build();
        
        vehicleLHRepo.save(locationHistory);

        vehicle.setLastUpdate(LocalDateTime.now());
        vehicleRepo.save(vehicle);

        assignmentService.checkIfVehicleReachedIncident(vehicleId, latitude, longitude);
    }
}
