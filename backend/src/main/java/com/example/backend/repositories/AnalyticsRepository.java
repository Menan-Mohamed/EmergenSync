package com.example.backend.repositories;

import com.example.backend.entities.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Incident, Integer> {

    @Query(value = """
        SELECT 
            i.type as emergencyType,
            AVG(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as averageResponseTime,
            MIN(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as minResponseTime,
            MAX(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as maxResponseTime,
            COUNT(*) as totalIncidents
        FROM incidents i
        INNER JOIN assignments a ON i.incidentID = a.incidentID
        WHERE a.solved_at IS NOT NULL
        AND i.reported_at IS NOT NULL
        GROUP BY i.type
        """, nativeQuery = true)
    List<Object[]> getResponseTimeStatsByType();

    @Query(value = """
        SELECT 
            DATE(i.reported_at) as date,
            i.type as emergencyType,
            AVG(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as averageResponseTime,
            COUNT(*) as incidentCount
        FROM incidents i
        INNER JOIN assignments a ON i.incidentID = a.incidentID
        WHERE a.solved_at IS NOT NULL
        AND i.reported_at BETWEEN :startDate AND :endDate
        GROUP BY DATE(i.reported_at), i.type
        ORDER BY DATE(i.reported_at) DESC
        """, nativeQuery = true)
    List<Object[]> getDailyResponseStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
        SELECT 
            DATE_FORMAT(i.reported_at, '%Y-%m') as month,
            i.type as emergencyType,
            AVG(TIMESTAMPDIFF(MINUTE, i.reported_at, a.solved_at)) as averageResponseTime,
            COUNT(*) as incidentCount
        FROM incidents i
        INNER JOIN assignments a ON i.incidentID = a.incidentID
        WHERE a.solved_at IS NOT NULL
        AND i.reported_at BETWEEN :startDate AND :endDate
        GROUP BY DATE_FORMAT(i.reported_at, '%Y-%m'), i.type
        ORDER BY month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlyResponseStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
        SELECT 
            i.latitude,
            i.longitude,
            COUNT(*) as incidentCount,
            i.type as emergencyType,
            CAST(AVG(i.severity) AS SIGNED) as severity
        FROM incidents i
        WHERE i.reported_at BETWEEN :startDate AND :endDate
        GROUP BY i.latitude, i.longitude, i.type
        HAVING COUNT(*) >= :minIncidents
        ORDER BY incidentCount DESC
        """, nativeQuery = true)
    List<Object[]> getIncidentHeatmapData(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minIncidents") int minIncidents
    );
}