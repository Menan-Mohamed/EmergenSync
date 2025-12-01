package com.example.backend.repositories;

import com.example.backend.entities.Assignment;
import com.example.backend.entities.AssignmentID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, AssignmentID> {

    @Query("""
           SELECT a FROM Assignment a
           WHERE a.vehicle.id = :vehicleId
           AND a.solvedAt IS NULL
           """)
    Assignment findActiveAssignmentByVehicle(@Param("vehicleId") Integer vehicleId);
}
