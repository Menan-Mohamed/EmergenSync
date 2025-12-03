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
import com.example.backend.dtos.VehicleDispatchDto;
import com.example.backend.entities.Incident;
import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.mapper.VehicleMapper;


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

    @Autowired
    private VehicleMapper vehicleMapper;

    public void autoAssign(Incident incident){

        VehicleType vehicleType = mapIncidentTypeToVehicleType(incident.getType());

        List<Vehicle> available = vehicleRepo.findAvailableByType(vehicleType);

        if (available == null || available.isEmpty()) {
            return;
        }

        VehicleDispatchDto nearest = findNearestVehicle(available, incident);

        if(nearest == null) return;

        assignmentService.assignVehicle(nearest, incident);
    }

    public VehicleDispatchDto findNearestVehicle(List<Vehicle> vehicles, Incident incident) {

        return vehicles.stream()
        .map(vehicle -> {
            VehicleLocationHistory latest = vehicleLHRepo.findFirstByIdVehicleIDOrderByIdTimeStampDesc(vehicle.getId());

            if (latest == null || latest.getLatitude() == null || latest.getLongitude() == null) {
                return null;
            }

            double distance = haversineFormula.haversine(latest.getLatitude(), latest.getLongitude(), incident.getLatitude(), incident.getLongitude());

            return vehicleMapper.toDispatchDto(vehicle, latest, distance);
        })
        .filter(dto -> dto != null)
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
