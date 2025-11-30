package com.example.backend.repository;

import com.example.backend.entity.VehicleLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleLocationHistoryRepository extends JpaRepository<VehicleLocationHistory, Integer> {
}
