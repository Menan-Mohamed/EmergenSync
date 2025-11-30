package com.example.backend.repositories;

import com.example.backend.entities.Vehicle;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.enums.IncidentType;
import com.example.backend.enums.VehicleStatus;



@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    
    @Query("SELECT vehicle FROM Vehicle vehicle WHERE vehicle.type = :type AND vehicle.status = 'available'")
    List<Vehicle> findAvailableByType(@Param("type") IncidentType type);

    List<Vehicle> findByStatus(VehicleStatus status);

    Optional<Vehicle> findById(Integer id);

}
