package com.example.backend.repositories;

import com.example.backend.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleAnalyticsRepository extends JpaRepository<Vehicle, Integer> {

    @Query(value = """
        SELECT
            v.vehicleID as vehicleId,
            v.vehicleID as vehiclePlate,
            v.type as vehicleType,
            COUNT(a.incidentID) as totalAssignments,
            (COUNT(a.incidentID) * 100.0 / :days) as utilizationRate,
            AVG(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as averageResponseTime
        FROM vehicles v
        LEFT JOIN assignments a ON v.vehicleID = a.vehicleID
        LEFT JOIN incidents i ON a.incidentID = i.incidentID
        WHERE v.status IN ('AVAILABLE', 'DISPATCHED', 'EN_ROUTE')
        AND (a.assigned_at IS NULL OR a.assigned_at BETWEEN :startDate AND :endDate)
        GROUP BY v.vehicleID, v.type
        ORDER BY utilizationRate DESC
        """, nativeQuery = true)
    List<Object[]> getVehicleUtilizationStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("days") int days
    );

    @Query(value = """
        SELECT
            v.vehicleID as vehicleId,
            v.vehicleID as vehiclePlate,
            v.type as vehicleType,
            AVG(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as averageResponseTime,
            COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END) as completedIncidents,
            (COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END) * 100.0 / COUNT(i.incidentID)) as successRate
        FROM vehicles v
        INNER JOIN assignments a ON v.vehicleID = a.vehicleID
        INNER JOIN incidents i ON a.incidentID = i.incidentID
        WHERE a.assigned_at BETWEEN :startDate AND :endDate
        AND a.solved_at IS NOT NULL
        GROUP BY v.vehicleID, v.type
        HAVING COUNT(i.incidentID) >= :minIncidents
        ORDER BY averageResponseTime ASC, successRate DESC
        """, nativeQuery = true)
    List<Object[]> getTopPerformingUnits(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minIncidents") int minIncidents
    );
}