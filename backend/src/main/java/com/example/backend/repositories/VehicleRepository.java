package com.example.backend.repositories;

import com.example.backend.entities.Vehicle;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    // NEW: Thread-safe version with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.type = :type AND v.status = 'AVAILABLE'")
    List<Vehicle> findAvailableByTypeWithLock(@Param("type") VehicleType type);

    // NEW: Find by ID with lock to prevent concurrent modifications
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
    Optional<Vehicle> findByIdWithLock(@Param("id") Integer id);

    // Your original queries - keep unchanged
    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByType(VehicleType type);

    List<Vehicle> findByStatusAndType(VehicleStatus status, VehicleType type);
}