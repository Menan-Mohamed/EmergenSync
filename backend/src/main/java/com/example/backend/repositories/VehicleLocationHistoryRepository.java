package com.example.backend.repositories;

import com.example.backend.entities.VehicleLocationHistory;
import com.example.backend.entities.VehicleLocationHistoryID;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleLocationHistoryRepository extends JpaRepository<VehicleLocationHistory, VehicleLocationHistoryID> {
    
    @Query("SELECT vehicle FROM VehicleLocationHistory vehicle WHERE vehicle.vehicleId = :vehicleId ORDER BY vehicle.id.timeStamp DESC")
    List<VehicleLocationHistory> findLocationHistory(@Param("vehicleId") Integer vehicleId);

    @Query("SELECT vehicle FROM VehicleLocationHistory vehicle WHERE vehicle.vehicleId = :vehicleId ORDER BY vehicle.id.timeStamp DESC LIMIT 1")
    VehicleLocationHistory findLatestLocation(@Param("vehicleId") Integer vehicleId);


}
