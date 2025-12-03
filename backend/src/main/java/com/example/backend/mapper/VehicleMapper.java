package com.example.backend.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.backend.dtos.CreateVehicleDto;
import com.example.backend.dtos.VehicleDispatchDto;
import com.example.backend.dtos.VehicleDto;
import com.example.backend.entities.User;
import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.enums.VehicleStatus;

@Component
public class VehicleMapper {
    
    public VehicleDto toDto(Vehicle vehicle){
        if(vehicle == null){
            return null;
        }

        return VehicleDto.builder()
                .id(vehicle.getId())
                .type(vehicle.getType())
                .status(vehicle.getStatus())
                .lastUpdate(vehicle.getLastUpdate())
                .responderId(vehicle.getResponder().getId())
                // .responderUsername(vehicle.getResponder().getUsername())
                // .responderEmail(vehicle.getResponder().getEmail())
                .build();
    }

    public VehicleDto toDtoWithLocation(Vehicle vehicle, VehicleLocationHistory locationHistory){
        VehicleDto dto = toDto(vehicle);

        if(locationHistory !=null){
            dto.setLatitude(locationHistory.getLatitude());
            dto.setLongitude(locationHistory.getLongitude());
            dto.setLocationTimeStamp(locationHistory.getId().getTimeStamp());
        }

        return dto;
    }

    public Vehicle toEntity(CreateVehicleDto dto, User responder){
        if(dto == null){
            return null;
        }

        return Vehicle.builder()
                .type(dto.getType())
                .responder(responder)
                .status(VehicleStatus.AVAILABLE)
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    public VehicleDispatchDto toDispatchDto(Vehicle vehicle, VehicleLocationHistory locationHistory, double distance){
        if(vehicle == null || locationHistory == null){
            return null;
        }

        return VehicleDispatchDto.builder()
                .id(vehicle.getId())
                .type(vehicle.getType())
                .status(vehicle.getStatus())
                .lastUpdate(vehicle.getLastUpdate())
                .latitude(locationHistory.getLatitude())
                .longitude(locationHistory.getLongitude())
                .locationTimeStamp(locationHistory.getId().getTimeStamp())
                .distanceToIncident(distance)
                .responderId(vehicle.getResponder().getId())
                .build();
        
    }
}
