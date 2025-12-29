package com.example.backend.services;

import com.example.backend.dtos.*;
import com.example.backend.repositories.AnalyticsRepository;
import com.example.backend.repositories.VehicleAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private VehicleAnalyticsRepository vehicleAnalyticsRepository;

    public List<ResponseTimeStats> getResponseTimeStatsByType() {
        List<Object[]> results = analyticsRepository.getResponseTimeStatsByType();
        List<ResponseTimeStats> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            stats.add(new ResponseTimeStats(
                (String) row[0],           
                ((Number) row[1]).doubleValue(), 
                ((Number) row[2]).doubleValue(), 
                ((Number) row[3]).doubleValue(), 
                ((Number) row[4]).longValue()    
            ));
        }
        
        return stats;
    }

    public List<DailyResponseStats> getDailyResponseStats(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> results = analyticsRepository.getDailyResponseStats(startDate, endDate);
        List<DailyResponseStats> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            stats.add(new DailyResponseStats(
                row[0].toString(),         
                (String) row[1],           
                ((Number) row[2]).doubleValue(), 
                ((Number) row[3]).longValue()    
            ));
        }
        
        return stats;
    }

    public List<MonthlyResponseStats> getMonthlyResponseStats(int months) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);
        
        List<Object[]> results = analyticsRepository.getMonthlyResponseStats(startDate, endDate);
        List<MonthlyResponseStats> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            stats.add(new MonthlyResponseStats(
                (String) row[0],           
                (String) row[1],           
                ((Number) row[2]).doubleValue(), 
                ((Number) row[3]).longValue()    
            ));
        }
        
        return stats;
    }

    public List<VehicleUtilization> getVehicleUtilization(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> results = vehicleAnalyticsRepository.getVehicleUtilizationStats(
            startDate, endDate, days
        );
        List<VehicleUtilization> utilization = new ArrayList<>();
        
        for (Object[] row : results) {
            utilization.add(new VehicleUtilization(
                ((Number) row[0]).intValue(),    
                String.valueOf(row[1]),          
                (String) row[2],                 
                ((Number) row[3]).longValue(),   
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0, 
                row[5] != null ? ((Number) row[5]).doubleValue() : 0.0  
            ));
        }
        
        return utilization;
    }

    public List<IncidentHeatmapPoint> getIncidentHeatmap(int days, int minIncidents) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> results = analyticsRepository.getIncidentHeatmapData(
            startDate, endDate, minIncidents
        );
        List<IncidentHeatmapPoint> heatmap = new ArrayList<>();
        
        for (Object[] row : results) {
            heatmap.add(new IncidentHeatmapPoint(
                ((Number) row[0]).doubleValue(), 
                ((Number) row[1]).doubleValue(), 
                ((Number) row[2]).longValue(),   
                (String) row[3],                 
                ((Number) row[4]).intValue()     
            ));
        }
        
        return heatmap;
    }
    public List<TopPerformingUnit> getTopPerformingUnits(int days, int minIncidents, int topN) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> results = vehicleAnalyticsRepository.getTopPerformingUnits(
            startDate, endDate, minIncidents
        );
        List<TopPerformingUnit> units = new ArrayList<>();
        
        for (Object[] row : results) {
            units.add(new TopPerformingUnit(
                ((Number) row[0]).intValue(),    
                String.valueOf(row[1]),          
                (String) row[2],                 
                ((Number) row[3]).doubleValue(), 
                ((Number) row[4]).longValue(),   
                ((Number) row[5]).doubleValue()  
            ));
        }
        
        return units;
    }
}