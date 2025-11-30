package com.example.backend.repositories;

import com.example.backend.entities.VehicleLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleLocationHistoryRepository extends JpaRepository<VehicleLocationHistory, Integer> {
}
