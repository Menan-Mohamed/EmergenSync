package com.example.backend.services;

import java.util.Comparator;
import java.util.List;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.utils.HaversineFormula;
import com.example.backend.dtos.VehicleDispatchDto;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.mapper.VehicleMapper;


@Service
public class DispatchService {

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private HaversineFormula haversineFormula;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleMapper vehicleMapper;

    public void autoAssign(Incident incident){

        VehicleType vehicleType = mapIncidentTypeToVehicleType(incident.getType());

        List<Vehicle> available = vehicleRepo.findAvailableByType(vehicleType);

        if (available == null || available.isEmpty()) {
            return;
        }

        VehicleDispatchDto nearestVehicleDto = findNearestVehicle(available, incident);

        if(nearestVehicleDto == null) return;

        Vehicle nearest = vehicleRepo.findById(nearestVehicleDto.getId()).orElse(null);

        if(nearest == null) return;

        assignmentService.assignVehicle(nearest, incident);
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
