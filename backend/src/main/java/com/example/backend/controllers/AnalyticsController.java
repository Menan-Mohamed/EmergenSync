package com.example.backend.controllers;

import com.example.backend.dtos.*;
import com.example.backend.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/response-times/by-type")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<ResponseTimeStats>> getResponseTimesByType() {
        return ResponseEntity.ok(analyticsService.getResponseTimeStatsByType());
    }

    @GetMapping("/response-times/daily")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<DailyResponseStats>> getDailyResponseStats(
        @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(analyticsService.getDailyResponseStats(days));
    }

    @GetMapping("/response-times/monthly")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<MonthlyResponseStats>> getMonthlyResponseStats(
        @RequestParam(defaultValue = "6") int months
    ) {
        return ResponseEntity.ok(analyticsService.getMonthlyResponseStats(months));
    }

    @GetMapping("/vehicle-utilization")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<VehicleUtilization>> getVehicleUtilization(
        @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(analyticsService.getVehicleUtilization(days));
    }

    @GetMapping("/heatmap")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<IncidentHeatmapPoint>> getIncidentHeatmap(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "2") int minIncidents
    ) {
        return ResponseEntity.ok(analyticsService.getIncidentHeatmap(days, minIncidents));
    }

    @GetMapping("/top-performing")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<TopPerformingUnit>> getTopPerformingUnits(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "5") int minIncidents,
        @RequestParam(defaultValue = "10") int topN
    ) {
        return ResponseEntity.ok(analyticsService.getTopPerformingUnits(days, minIncidents, topN));
    }
}