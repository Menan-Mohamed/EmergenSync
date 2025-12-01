package com.example.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.entities.VehicleLocationHistoryID;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;
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

    public boolean updateVehicleLocation(Integer vehicleId, Double latitude, Double longitude){
        Optional<Vehicle> findVehicle = vehicleRepo.findById(vehicleId);

        if (findVehicle.isEmpty()) {
            return false;
        }

        Vehicle vehicle = findVehicle.get();

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
        
        return true;
    }

    public Optional<Vehicle> getVehicleById(Integer id){
        return vehicleRepo.findById(id);
    }

    public List<Vehicle> getAllVehicles(VehicleStatus status, VehicleType type){
        if(status != null && type != null){
            return vehicleRepo.findByStatusAndType(status, type);
        }
        else if(type == null){
            return vehicleRepo.findByStatus(status);
        }
        else if(status == null){
            return vehicleRepo.findByType(type);
        }
        return vehicleRepo.findAll();
    }

    public Vehicle createVehicle(Vehicle vehicle){
        return vehicleRepo.save(vehicle);
    }
}
