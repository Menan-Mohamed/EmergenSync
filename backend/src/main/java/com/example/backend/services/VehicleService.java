package com.example.backend.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dtos.CreateVehicleDto;
import com.example.backend.dtos.VehicleDto;
import com.example.backend.entities.Vehicle;
import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.entities.VehicleLocationHistoryID;
import com.example.backend.entities.User;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;
import com.example.backend.mapper.VehicleMapper;
import com.example.backend.repositories.UserRepository;
import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.repositories.VehicleRepository;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLHRepo;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private WebSocketPublisherService webSocketPublisherService;

    @Transactional
    public boolean updateVehicleLocation(Integer vehicleId, Double latitude, Double longitude){
        Optional<Vehicle> findVehicle = vehicleRepository.findById(vehicleId);

        if (findVehicle.isEmpty()) {
            return false;
        }

        Vehicle vehicle = findVehicle.get();

        vehicle.setLastUpdate(LocalDateTime.now());
        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicleRepository.save(vehicle);

        VehicleDto update = vehicleMapper.toDto(vehicle);
        webSocketPublisherService.sendVehicleLocation(update);

        assignmentService.checkIfVehicleReachedIncident(vehicleId, latitude, longitude);
        
        return true;
    }

    public Optional<VehicleDto> getVehicleById(Integer id){
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);

        if (vehicleOpt.isEmpty()) {
            return Optional.empty();
        }

        Vehicle vehicle = vehicleOpt.get();
        VehicleLocationHistory latest = vehicleLHRepo.findFirstByIdVehicleIDOrderByIdTimeStampDesc(vehicle.getId());

        VehicleDto dto = vehicleMapper.toDtoWithLocation(vehicle, latest);

        return Optional.of(dto);


    }

    public List<VehicleDto> getVehiclesByFilter(VehicleStatus status, VehicleType type){
        List<Vehicle> vehicles;
        if(status != null && type != null){
            vehicles = vehicleRepository.findByStatusAndType(status, type);
        }
        else if(status != null){
            vehicles = vehicleRepository.findByStatus(status);
        }
        else if(type != null){
            vehicles = vehicleRepository.findByType(type);
        }
        else {
            vehicles = vehicleRepository.findAll();
        }

        return vehicles.stream()
                .map(vehicle -> {
                    VehicleLocationHistory latest = vehicleLHRepo.findFirstByIdVehicleIDOrderByIdTimeStampDesc(vehicle.getId());

                    if (latest == null || latest.getLatitude() == null || latest.getLongitude() == null) {
                        return null;
                    }

                    return vehicleMapper.toDtoWithLocation(vehicle, latest);
                })
                .collect(Collectors.toList());
        
    }

    public VehicleDto createVehicle(CreateVehicleDto createVehicleDto){
        User responder = userRepository.findById(createVehicleDto.getResponderId())
                .orElseThrow(() -> new RuntimeException("Responder not found with ID: " + createVehicleDto.getResponderId()));

        Vehicle vehicle = vehicleMapper.toEntity(createVehicleDto, responder);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        if (createVehicleDto.getLatitude() != null && createVehicleDto.getLongitude() != null) {
            VehicleLocationHistoryID locationId = new VehicleLocationHistoryID(
                    savedVehicle.getId(),
                    LocalDateTime.now()
            );

            VehicleLocationHistory initialLocation = VehicleLocationHistory.builder()
                    .id(locationId)
                    .vehicle(savedVehicle)
                    .latitude(createVehicleDto.getLatitude())
                    .longitude(createVehicleDto.getLongitude())
                    .build();

            vehicleLHRepo.save(initialLocation);

            VehicleDto update = vehicleMapper.toDto(vehicle);
            webSocketPublisherService.sendVehicleLocation(update);

            // Check waiting Incidents
            assignmentService.assignWaitingIncidents(vehicle);

            return vehicleMapper.toDtoWithLocation(savedVehicle, initialLocation);
        }

        return vehicleMapper.toDto(savedVehicle);
    }

    public List<VehicleDto> getAllVehicles (){
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<VehicleDto> vehicleDtos = new ArrayList<>();
        for(Vehicle i : vehicles){
            vehicleDtos.add(vehicleMapper.toDto(i));
        }
        return vehicleDtos;
    }
}
