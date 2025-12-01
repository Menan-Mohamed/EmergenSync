package com.example.backend.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.services.VehicleService;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/ems/vehicle")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicle(@PathVariable Integer id){
        Optional<Vehicle> vehicle = vehicleService.getVehicleById(id);
        if(vehicle.isEmpty()){
            return ResponseEntity.status(404).body("Vehicle not found");
        }
        return ResponseEntity.ok(vehicle.get());
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles(@RequestParam(required = false) VehicleStatus status, @RequestParam(required = false) VehicleType type){

        List<Vehicle> vehicles = vehicleService.getAllVehicles(status, type);

        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
        return ResponseEntity.status(201).body(savedVehicle);
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateVehicleLocation(@PathVariable Integer id, @RequestParam double latitude, @RequestParam double longitude){
        boolean updated = vehicleService.updateVehicleLocation(id, latitude, longitude);

        if(!updated){
            return ResponseEntity.status(404).body("Vehicle not found");
        }
        return ResponseEntity.ok("Location update successfully");
    }



}
