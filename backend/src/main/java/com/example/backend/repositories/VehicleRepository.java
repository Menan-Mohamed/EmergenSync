package com.example.backend.repositories;

import com.example.backend.entities.Vehicle;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    @Query(value = """
        SELECT vehicleid FROM vehicles
        WHERE status = 'AVAILABLE'
          AND type = :type
          AND latitude IS NOT NULL
          AND longitude IS NOT NULL
        ORDER BY
          ST_Distance_Sphere(
            point(longitude, latitude),
            point(:lng, :lat)
          )
        LIMIT 1
        FOR UPDATE
        """, nativeQuery = true)
    Integer findNearestAvailableForUpdate(
            @Param("type") String type,
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    @Modifying
    @Query("""
        UPDATE Vehicle v
        SET v.status = com.example.backend.enums.VehicleStatus.ON_ROUTE_PENDING
        WHERE v.id = :id
          AND v.status = com.example.backend.enums.VehicleStatus.AVAILABLE
    """)
    int claimById(@Param("id") Integer id);

    java.util.List<Vehicle> findByStatus(VehicleStatus status);

    java.util.List<Vehicle> findByType(VehicleType type);

    java.util.List<Vehicle> findByStatusAndType(VehicleStatus status, VehicleType type);
}
