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

    // Vehicle Utilization Statistics
    @Query(value = """
        SELECT 
            v.vehicleID as vehicleId,
            v.plate as vehiclePlate,
            v.type as vehicleType,
            COUNT(d.dispatchID) as totalAssignments,
            (COUNT(d.dispatchID) * 100.0 / :totalDays) as utilizationRate,
            AVG(TIMESTAMPDIFF(MINUTE, i.reportedAt, i.resolvedAt)) as averageResponseTime
        FROM vehicles v
        LEFT JOIN dispatch d ON v.vehicleID = d.vehicleID
        LEFT JOIN incidents i ON d.incidentID = i.incidentID
        WHERE v.status IN ('AVAILABLE', 'DISPATCHED', 'EN_ROUTE')
        AND (d.assignedAt IS NULL OR d.assignedAt BETWEEN :startDate AND :endDate)
        GROUP BY v.vehicleID, v.plate, v.type
        ORDER BY utilizationRate DESC
        """, nativeQuery = true)
    List<Object[]> getVehicleUtilizationStats(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("totalDays") int totalDays
    );

    // Top Performing Units
    @Query(value = """
        SELECT 
            v.vehicleID as vehicleId,
            v.plate as vehiclePlate,
            v.type as vehicleType,
            AVG(TIMESTAMPDIFF(MINUTE, i.reportedAt, i.resolvedAt)) as averageResponseTime,
            COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END) as completedIncidents,
            (COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END) * 100.0 / COUNT(*)) as successRate
        FROM vehicles v
        INNER JOIN dispatch d ON v.vehicleID = d.vehicleID
        INNER JOIN incidents i ON d.incidentID = i.incidentID
        WHERE d.assignedAt BETWEEN :startDate AND :endDate
        AND i.resolvedAt IS NOT NULL
        GROUP BY v.vehicleID, v.plate, v.type
        HAVING COUNT(*) >= :minIncidents
        ORDER BY averageResponseTime ASC
        """, nativeQuery = true)
    List<Object[]> getTopPerformingUnits(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("minIncidents") int minIncidents
    );

    // Overloaded method to support limiting results in Java
    default List<Object[]> getTopPerformingUnitsLimited(LocalDateTime startDate, LocalDateTime endDate, int minIncidents, int topN) {
        List<Object[]> results = getTopPerformingUnits(startDate, endDate, minIncidents);
        return results.subList(0, Math.min(topN, results.size()));
    }
}