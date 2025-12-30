//package com.example.backend.scheduler;
//
//import com.example.backend.dtos.CachedRoute;
//import com.example.backend.dtos.Point;
//import com.example.backend.entities.Assignment;
//import com.example.backend.repositories.AssignmentRepository;
//import com.example.backend.services.VehicleService;
//import jakarta.transaction.Transactional;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@EnableScheduling
//public class VehicleRouteScheduler {
//
//    private final AssignmentRepository assignmentRepository;
//    private final RedisTemplate<String, CachedRoute> redisTemplate;
//    private final VehicleService vehicleService;
//
//    public VehicleRouteScheduler(
//            AssignmentRepository assignmentRepository,
//            RedisTemplate<String, CachedRoute> redisTemplate,
//            VehicleService vehicleService
//    ) {
//        this.assignmentRepository = assignmentRepository;
//        this.redisTemplate = redisTemplate;
//        this.vehicleService = vehicleService;
//    }
//
//    @Scheduled(fixedRate = 5000)
//    @Transactional
//    public void updateVehiclePositions() {
//
//        List<Assignment> activeAssignments =
//                assignmentRepository.findActiveAssignments();
//
//        for (Assignment assignment : activeAssignments) {
//
//            int vehicleId = assignment.getVehicle().getId();
//            String key = "route:" + vehicleId;
//
//            CachedRoute cachedRoute = redisTemplate.opsForValue().get(key);
//
//            if (cachedRoute == null) {
//                continue;
//            }
//
//            int index = cachedRoute.getIndex();
//            List<Point> points = cachedRoute.getPoints();
//
//            if (index >= points.size()) {
//                continue;
//            }
//
//            Point currentPoint = points.get(index);
//            vehicleService.updateVehicleLocation(vehicleId, currentPoint.getLatitude(), currentPoint.getLongitude());
//
//
//            cachedRoute.setIndex(index + 1);
//            redisTemplate.opsForValue().set(key, cachedRoute);
//        }
//    }
//}
//
