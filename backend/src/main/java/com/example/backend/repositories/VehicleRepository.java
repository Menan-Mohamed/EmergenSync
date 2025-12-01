package com.example.backend.repositories;

import com.example.backend.entities.Vehicle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;



@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    
    @Query("SELECT v FROM Vehicle v WHERE v.type = :type AND v.status = VehicleStatus.AVAILABLE")
    List<Vehicle> findAvailableByType(@Param("type") IncidentType type);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByType(VehicleType type);

    List<Vehicle> findByStatusAndType(VehicleStatus status, VehicleType type);

}
