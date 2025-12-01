package com.example.backend.controllers;


import com.example.backend.dtos.UserResponseDto;
import com.example.backend.dtos.UserViewCriteriaDto;
import com.example.backend.services.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserManagementController {

    @Autowired
    private UserManagementService service;

    @PostMapping("/filter")
    public Page<UserResponseDto> getUsers(
            @RequestBody UserViewCriteriaDto criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.getUsers(criteria, page, size);
    }

    @PutMapping("/{id}/approve")
    public String approveUser(@PathVariable int id) {
        service.approveUser(id);
        return "User approved successfully";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable int id) {
        service.deleteUser(id);
        return "User deleted successfully";
    }



}

