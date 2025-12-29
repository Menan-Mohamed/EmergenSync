package com.example.backend;

import com.example.backend.entities.Assignment;
import com.example.backend.entities.Incident;
import com.example.backend.entities.User;
import com.example.backend.entities.Vehicle;
import com.example.backend.enums.IncidentStatus;
import com.example.backend.enums.IncidentType;
import com.example.backend.enums.UserRole;
import com.example.backend.enums.UserType;
import com.example.backend.enums.VehicleStatus;
import com.example.backend.enums.VehicleType;
import com.example.backend.repositories.AssignmentRepository;
import com.example.backend.repositories.IncidentRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.repositories.VehicleRepository;
import com.example.backend.repositories.VehicleLocationHistoryRepository;
import com.example.backend.services.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DispatchConcurrencyTest {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private VehicleLocationHistoryRepository vehicleLocationHistoryRepository;

    private User testResponder;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up data from previous tests - respect foreign key constraints
        assignmentRepository.deleteAll();
        vehicleLocationHistoryRepository.deleteAll(); // Delete this BEFORE vehicles
        incidentRepository.deleteAll();
        vehicleRepository.deleteAll();

        // Create a test responder that can be reused
        testResponder = new User();
        testResponder.setUsername("test_responder_" + System.currentTimeMillis());
        testResponder.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testResponder.setPassword("password123");
        testResponder.setType(UserType.FIRE); // Match vehicle type for FIRE tests
        testResponder.setRole(UserRole.EMERGENCY_RESPONDER);
        testResponder = userRepository.saveAndFlush(testResponder);
    }

    @Test
    public void testConcurrentDispatch_NoDoubleAssignment() throws InterruptedException, ExecutionException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: No Double Assignment - Single Vehicle, Multiple Incidents");
        System.out.println("=".repeat(60));

        // Create 1 available FIRE vehicle
        Vehicle vehicle = Vehicle.builder()
                .type(VehicleType.FIRE)
                .status(VehicleStatus.AVAILABLE)
                .latitude(30.0444)
                .longitude(31.2357)
                .responder(testResponder)
                .lastUpdate(LocalDateTime.now())
                .build();
        vehicle = vehicleRepository.saveAndFlush(vehicle);

        System.out.println("✓ Created vehicle: ID=" + vehicle.getId() + ", Type=" + vehicle.getType() + ", Status=" + vehicle.getStatus());

        // Create 3 FIRE incidents
        List<Incident> incidents = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Incident incident = new Incident();
            incident.setType(IncidentType.FIRE);
            incident.setStatus(IncidentStatus.REPORTED);
            incident.setLatitude(30.0 + (i * 0.01));
            incident.setLongitude(31.0 + (i * 0.01));
            incident.setSeverity(5 - i); // Different severities: 5, 4, 3
            incident.setDescription("Concurrent test fire incident " + i);
            incident.setReportedAt(LocalDateTime.now()); // FIX: Add reportedAt
            incidents.add(incidentRepository.saveAndFlush(incident));
            System.out.println("✓ Created incident " + i + ": ID=" + incident.getId() + ", Severity=" + incident.getSeverity());
        }

        // Try to assign all 3 incidents concurrently to the same vehicle
        System.out.println("\n⚡ Attempting concurrent assignments (3 threads)...");
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < incidents.size(); i++) {
            final int index = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return dispatchService.autoAssignAsync(incidents.get(index)).get();
                } catch (Exception e) {
                    System.err.println("Thread " + index + " error: " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }

        // Wait for all attempts to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Small delay to ensure all transactions are committed
        Thread.sleep(500);

        // Count successful assignments
        long successCount = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(success -> success)
                .count();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS:");
        System.out.println("=".repeat(60));
        System.out.println("✓ Successful assignments: " + successCount + " out of 3 attempts");

        // Verify: Only ONE assignment should succeed
        assertEquals(1, successCount,
                "Expected exactly 1 successful assignment, but got " + successCount);

        // Verify vehicle is ON_ROUTE, not AVAILABLE
        Vehicle updatedVehicle = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        System.out.println("✓ Vehicle status: " + updatedVehicle.getStatus());
        assertEquals(VehicleStatus.ON_ROUTE, updatedVehicle.getStatus(),
                "Vehicle should be ON_ROUTE after assignment");

        // Verify only 1 incident is ASSIGNED, others remain REPORTED
        long assignedCount = 0;
        long reportedCount = 0;
        for (Incident incident : incidents) {
            Incident updated = incidentRepository.findById(incident.getId()).orElseThrow();
            System.out.println("✓ Incident " + updated.getId() + " status: " + updated.getStatus());
            if (updated.getStatus() == IncidentStatus.ASSIGNED) {
                assignedCount++;
            } else if (updated.getStatus() == IncidentStatus.REPORTED) {
                reportedCount++;
            }
        }

        assertEquals(1, assignedCount,
                "Expected exactly 1 ASSIGNED incident, but got " + assignedCount);
        assertEquals(2, reportedCount,
                "Expected exactly 2 REPORTED incidents, but got " + reportedCount);

        // Verify assignment record was created
        List<Assignment> assignments = assignmentRepository.findAll();
        assertEquals(1, assignments.size(), "Should have exactly 1 assignment record");

        Assignment assignment = assignments.get(0);
        assertNotNull(assignment.getAssignedAt(), "Assignment should have assignedAt timestamp");
        assertNull(assignment.getSolvedAt(), "Assignment should not have solvedAt timestamp yet");

        System.out.println("✓ Assignment record created: Vehicle " + assignment.getVehicle().getId() +
                " → Incident " + assignment.getIncident().getId());
        System.out.println("\n✅ TEST PASSED: No double assignment occurred!");
        System.out.println("=".repeat(60) + "\n");
    }

    @Test
    public void testConcurrentDispatch_MultipleVehiclesMultipleIncidents()
            throws ExecutionException, InterruptedException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: Multiple Vehicles & Multiple Incidents");
        System.out.println("=".repeat(60));

        // Create responder for POLICE vehicles
        User policeResponder = new User();
        policeResponder.setUsername("police_responder_" + System.currentTimeMillis());
        policeResponder.setEmail("police" + System.currentTimeMillis() + "@example.com");
        policeResponder.setPassword("password123");
        policeResponder.setType(UserType.POLICE);
        policeResponder.setRole(UserRole.EMERGENCY_RESPONDER);
        policeResponder = userRepository.saveAndFlush(policeResponder);

        // Create 2 available POLICE vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Vehicle vehicle = Vehicle.builder()
                    .type(VehicleType.POLICE)
                    .status(VehicleStatus.AVAILABLE)
                    .latitude(30.0 + (i * 0.1))
                    .longitude(31.0 + (i * 0.1))
                    .responder(policeResponder)
                    .lastUpdate(LocalDateTime.now())
                    .build();
            vehicles.add(vehicleRepository.saveAndFlush(vehicle));
            System.out.println("✓ Created vehicle " + i + ": ID=" + vehicle.getId() + ", Type=" + vehicle.getType());
        }

        // Create 4 POLICE incidents (more than available vehicles)
        List<Incident> incidents = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Incident incident = new Incident();
            incident.setType(IncidentType.POLICE);
            incident.setStatus(IncidentStatus.REPORTED);
            incident.setLatitude(30.0 + (i * 0.05));
            incident.setLongitude(31.0 + (i * 0.05));
            incident.setSeverity(5);
            incident.setDescription("Multi-vehicle test incident " + i);
            incident.setReportedAt(LocalDateTime.now()); // FIX: Add reportedAt
            incidents.add(incidentRepository.saveAndFlush(incident));
            System.out.println("✓ Created incident " + i + ": ID=" + incident.getId());
        }

        // Try to assign all 4 incidents concurrently
        System.out.println("\n⚡ Attempting concurrent assignments (4 threads)...");
        List<CompletableFuture<Boolean>> futures = incidents.stream()
                .map(incident -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return dispatchService.autoAssignAsync(incident).get();
                    } catch (Exception e) {
                        return false;
                    }
                }))
                .collect(Collectors.toList());

        // Wait for all attempts
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Thread.sleep(500);

        // Count successes
        long successCount = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(success -> success)
                .count();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS:");
        System.out.println("=".repeat(60));
        System.out.println("✓ Successful assignments: " + successCount + " out of 4 attempts");

        // Verify: At most 2 assignments (we only have 2 vehicles)
        assertTrue(successCount <= 2,
                "Expected at most 2 successful assignments, but got " + successCount);

        // Count ON_ROUTE vehicles
        long onRouteVehicles = vehicles.stream()
                .map(v -> vehicleRepository.findById(v.getId()).orElseThrow())
                .filter(v -> v.getStatus() == VehicleStatus.ON_ROUTE)
                .count();

        System.out.println("✓ Vehicles ON_ROUTE: " + onRouteVehicles);
        assertEquals(successCount, onRouteVehicles,
                "Number of ON_ROUTE vehicles should match successful assignments");

        // Count ASSIGNED incidents
        long assignedCount = incidents.stream()
                .map(i -> incidentRepository.findById(i.getId()).orElseThrow())
                .filter(i -> i.getStatus() == IncidentStatus.ASSIGNED)
                .count();

        System.out.println("✓ Incidents ASSIGNED: " + assignedCount);
        assertEquals(successCount, assignedCount,
                "Number of ASSIGNED incidents should match successful assignments");

        // Verify assignment records
        List<Assignment> assignments = assignmentRepository.findAll();
        assertEquals(successCount, assignments.size(),
                "Number of assignment records should match successful assignments");

        System.out.println("✓ Assignment records created: " + assignments.size());

        System.out.println("\n✅ TEST PASSED: Multiple vehicles correctly assigned!");
        System.out.println("=".repeat(60) + "\n");
    }

    @Test
    public void testConcurrentDispatch_DifferentVehicleTypes()
            throws ExecutionException, InterruptedException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: Different Vehicle Types (Type Matching)");
        System.out.println("=".repeat(60));

        // Create FIRE responder
        User fireResponder = new User();
        fireResponder.setUsername("fire_resp_" + System.currentTimeMillis());
        fireResponder.setEmail("fire" + System.currentTimeMillis() + "@example.com");
        fireResponder.setPassword("password123");
        fireResponder.setType(UserType.FIRE);
        fireResponder.setRole(UserRole.EMERGENCY_RESPONDER);
        fireResponder = userRepository.saveAndFlush(fireResponder);

        // Create POLICE responder
        User policeResponder = new User();
        policeResponder.setUsername("police_resp_" + System.currentTimeMillis());
        policeResponder.setEmail("police" + System.currentTimeMillis() + "@example.com");
        policeResponder.setPassword("password123");
        policeResponder.setType(UserType.POLICE);
        policeResponder.setRole(UserRole.EMERGENCY_RESPONDER);
        policeResponder = userRepository.saveAndFlush(policeResponder);

        // Create 1 FIRE vehicle
        Vehicle fireVehicle = Vehicle.builder()
                .type(VehicleType.FIRE)
                .status(VehicleStatus.AVAILABLE)
                .latitude(30.0)
                .longitude(31.0)
                .responder(fireResponder)
                .lastUpdate(LocalDateTime.now())
                .build();
        fireVehicle = vehicleRepository.saveAndFlush(fireVehicle);
        System.out.println("✓ Created FIRE vehicle: ID=" + fireVehicle.getId());

        // Create 1 POLICE vehicle
        Vehicle policeVehicle = Vehicle.builder()
                .type(VehicleType.POLICE)
                .status(VehicleStatus.AVAILABLE)
                .latitude(30.1)
                .longitude(31.1)
                .responder(policeResponder)
                .lastUpdate(LocalDateTime.now())
                .build();
        policeVehicle = vehicleRepository.saveAndFlush(policeVehicle);
        System.out.println("✓ Created POLICE vehicle: ID=" + policeVehicle.getId());

        // Create 2 FIRE incidents
        List<Incident> fireIncidents = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Incident incident = new Incident();
            incident.setType(IncidentType.FIRE);
            incident.setStatus(IncidentStatus.REPORTED);
            incident.setLatitude(30.0 + (i * 0.01));
            incident.setLongitude(31.0 + (i * 0.01));
            incident.setSeverity(5);
            incident.setDescription("Fire incident " + i);
            incident.setReportedAt(LocalDateTime.now()); // FIX: Add reportedAt
            fireIncidents.add(incidentRepository.saveAndFlush(incident));
            System.out.println("✓ Created FIRE incident " + i + ": ID=" + incident.getId());
        }

        // Create 1 POLICE incident
        Incident policeIncident = new Incident();
        policeIncident.setType(IncidentType.POLICE);
        policeIncident.setStatus(IncidentStatus.REPORTED);
        policeIncident.setLatitude(30.1);
        policeIncident.setLongitude(31.1);
        policeIncident.setSeverity(5);
        policeIncident.setDescription("Police incident");
        policeIncident.setReportedAt(LocalDateTime.now()); // FIX: Add reportedAt
        policeIncident = incidentRepository.saveAndFlush(policeIncident);
        System.out.println("✓ Created POLICE incident: ID=" + policeIncident.getId());

        // Assign all concurrently
        System.out.println("\n⚡ Attempting concurrent assignments (3 threads)...");
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return dispatchService.autoAssignAsync(fireIncidents.get(0)).get();
            } catch (Exception e) {
                return false;
            }
        }));
        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return dispatchService.autoAssignAsync(fireIncidents.get(1)).get();
            } catch (Exception e) {
                return false;
            }
        }));
        Incident finalPoliceIncident = policeIncident;
        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return dispatchService.autoAssignAsync(finalPoliceIncident).get();
            } catch (Exception e) {
                return false;
            }
        }));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Thread.sleep(500);

        long successCount = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(success -> success)
                .count();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS:");
        System.out.println("=".repeat(60));
        System.out.println("✓ Successful assignments: " + successCount);

        // Should be exactly 2: 1 fire vehicle assigned, 1 police vehicle assigned
        assertEquals(2, successCount,
                "Expected 2 successful assignments (1 fire, 1 police)");

        // Verify fire vehicle is assigned
        Vehicle updatedFireVehicle = vehicleRepository.findById(fireVehicle.getId()).orElseThrow();
        System.out.println("✓ FIRE vehicle status: " + updatedFireVehicle.getStatus());
        assertEquals(VehicleStatus.ON_ROUTE, updatedFireVehicle.getStatus());

        // Verify police vehicle is assigned
        Vehicle updatedPoliceVehicle = vehicleRepository.findById(policeVehicle.getId()).orElseThrow();
        System.out.println("✓ POLICE vehicle status: " + updatedPoliceVehicle.getStatus());
        assertEquals(VehicleStatus.ON_ROUTE, updatedPoliceVehicle.getStatus());

        // Verify 1 fire incident is assigned, 1 remains reported
        long fireAssigned = fireIncidents.stream()
                .map(i -> incidentRepository.findById(i.getId()).orElseThrow())
                .filter(i -> i.getStatus() == IncidentStatus.ASSIGNED)
                .count();
        assertEquals(1, fireAssigned, "Exactly 1 fire incident should be assigned");

        // Verify police incident is assigned
        Incident updatedPoliceIncident = incidentRepository.findById(policeIncident.getId()).orElseThrow();
        assertEquals(IncidentStatus.ASSIGNED, updatedPoliceIncident.getStatus());

        System.out.println("✓ 1 FIRE incident assigned, 1 FIRE incident waiting");
        System.out.println("✓ 1 POLICE incident assigned");

        System.out.println("\n✅ TEST PASSED: Different vehicle types correctly assigned!");
        System.out.println("=".repeat(60) + "\n");
    }
}