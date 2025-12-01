package com.example.backend.service;

import com.example.backend.dto.VehicleResponseDto;
import com.example.backend.dto.VehicleViewCriteriaDto;
import com.example.backend.entity.Vehicle;
import com.example.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class VehicleManagementService {

    @Autowired
    private VehicleRepository repo;

    public Page<VehicleResponseDto> getVehicles(VehicleViewCriteriaDto filter, int page, int size) {
        Pageable pageable;


        if (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, filter.getSortBy());
            pageable = PageRequest.of(page, size, sort);
        } else {

            pageable = PageRequest.of(page, size);
        }


        Page<Vehicle> vehicles = repo.findByFilters(
                filter.getSearch(),
                filter.getType() != null ? filter.getType().name() : null,
                filter.getStatus() != null ? filter.getStatus().name() : null,
                filter.getResponderUsername(),
                pageable
        );

        return vehicles.map(this::toDto);
    }

    public void updateVehicleStatus(int id, Vehicle.VehicleStatus status) {
        Vehicle vehicle = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));

        vehicle.setStatus(status);
        vehicle.setLastUpdate(java.time.LocalDateTime.now());
        repo.save(vehicle);
    }

    private VehicleResponseDto toDto(Vehicle vehicle) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setType(vehicle.getType());
        dto.setResponderUsername(vehicle.getResponder().getUsername());
        dto.setStatus(vehicle.getStatus());
        dto.setLastUpdate(vehicle.getLastUpdate());
        return dto;
    }

    public void deleteVehicle(int id) {
        Vehicle vehicle = repo.findById(id).orElseThrow();
        repo.delete(vehicle);
    }
}
