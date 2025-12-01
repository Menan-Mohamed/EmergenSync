package com.example.backend.controller;

import com.example.backend.dto.VehicleResponseDto;
import com.example.backend.dto.VehicleViewCriteriaDto;
import com.example.backend.entity.Vehicle;
import com.example.backend.service.VehicleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin("*")
public class VehicleManagementController {

    @Autowired
    private VehicleManagementService service;

    @PostMapping("/filter")
    public Page<VehicleResponseDto> getVehicles(
            @RequestBody VehicleViewCriteriaDto criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.getVehicles(criteria, page, size);
    }

    @PutMapping("/{id}/status")
    public String updateVehicleStatus(
            @PathVariable int id,
            @RequestParam Vehicle.VehicleStatus status) {

        service.updateVehicleStatus(id, status);
        return "Vehicle status updated successfully";
    }

    @DeleteMapping("/{id}")
    public String deleteVehicle(@PathVariable int id) {
        service.deleteVehicle(id);
        return "Vehicle deleted successfully";
    }
}
