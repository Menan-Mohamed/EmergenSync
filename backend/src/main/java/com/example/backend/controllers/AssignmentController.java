package com.example.backend.controllers;


import com.example.backend.dtos.AssignmentDto;
import com.example.backend.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.example.backend.dtos.VehicleDto;
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/all")
    public ResponseEntity<List<AssignmentDto>> getAllAssignment(){

        List<AssignmentDto> assignmentDtos = assignmentService.getAllAssignment();

        return ResponseEntity.ok(assignmentDtos);
    }

}
