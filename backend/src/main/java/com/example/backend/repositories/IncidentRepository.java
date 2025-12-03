package com.example.backend.repositories;

import com.example.backend.entities.Incident;
import com.example.backend.enums.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    @Query(value = """
        SELECT *
        FROM incidents
        WHERE type = :type
          AND status = 'REPORTED'
        ORDER BY severity DESC
        LIMIT 1
        """,
            nativeQuery = true)
    Incident findMostSevereReportedByType(@Param("type") String type);
}
