package com.example.backend.repository;

import com.example.backend.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    @Query(
            value = "SELECT v.* FROM vehicles v " +
                    "JOIN users u ON v.userID = u.userID " +
                    "WHERE (:search IS NULL OR u.username LIKE CONCAT('%', :search, '%')) AND " +
                    "(:type IS NULL OR v.type = :type) AND " +
                    "(:status IS NULL OR v.status = :status) AND " +
                    "(:responderUsername IS NULL OR u.username LIKE CONCAT('%', :responderUsername, '%'))",
            countQuery = "SELECT COUNT(*) FROM vehicles v " +
                    "JOIN users u ON v.userID = u.userID " +
                    "WHERE (:search IS NULL OR u.username LIKE CONCAT('%', :search, '%')) AND " +
                    "(:type IS NULL OR v.type = :type) AND " +
                    "(:status IS NULL OR v.status = :status) AND " +
                    "(:responderUsername IS NULL OR u.username LIKE CONCAT('%', :responderUsername, '%'))",
            nativeQuery = true
    )
    Page<Vehicle> findByFilters(
            @Param("search") String search,
            @Param("type") String type,
            @Param("status") String status,
            @Param("responderUsername") String responderUsername,
            Pageable pageable
    );
}
