package com.example.backend.controllers;

import com.example.backend.entities.Incident;
import com.example.backend.services.IncidentService;
import com.example.backend.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ems/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    public Incident createIncident(@RequestBody Incident incident) {
        return incidentService.createIncident(incident);
    }

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Incident getIncident(@PathVariable Integer id) {
        return incidentService.getIncidentById(id);
    }

    @PutMapping("/{id}/state")
    public Incident updateIncidentState(@PathVariable Integer id,
                                        @RequestParam String state) {
        return incidentService.updateIncidentState(id, state);
    }

}