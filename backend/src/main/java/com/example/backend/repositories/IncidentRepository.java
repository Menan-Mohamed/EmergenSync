package com.example.backend.repositories;

import com.example.backend.entities.Incident;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.enums.IncidentType;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    // Your original native query - keep as is for non-concurrent use
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

    List<Incident> findByStatusAndReportedAtBefore(
        @Param("status") IncidentStatus status, 
        @Param("reportedBefore") LocalDateTime reportedBefore
    );

    // NEW: Find by ID with lock to prevent concurrent assignment
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Incident i WHERE i.id = :id")
    Optional<Incident> findByIdWithLock(@Param("id") Integer id);

    // NEW: Thread-safe version to find reported incidents with lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Incident i WHERE i.status = 'REPORTED' AND i.type = :type ORDER BY i.severity DESC")
    List<Incident> findReportedByTypeWithLock(@Param("type") String type);

    // NEW: Find incidents by status (useful for batch processing)
    List<Incident> findByStatus(IncidentStatus status);
}
