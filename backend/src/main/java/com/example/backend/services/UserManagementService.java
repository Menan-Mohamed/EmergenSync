package com.example.backend.services;

import com.example.backend.dtos.UserResponseDto;
import com.example.backend.dtos.UserViewCriteriaDto;
import com.example.backend.entities.User;
import com.example.backend.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class UserManagementService {

    @Autowired
    private UserRepository repo;

    public Page<UserResponseDto> getUsers(UserViewCriteriaDto filter, int page, int size) {

        Pageable pageable;

        // Only add sorting if sortBy is provided and not empty
        if (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, filter.getSortBy());
            pageable = PageRequest.of(page, size, sort);
        } else {
            // No sorting - just pagination
            pageable = PageRequest.of(page, size);
        }

        Page<User> users = repo.findByFilters(
                filter.getSearch(),
                filter.getType() != null ? filter.getType().name() : null,
                filter.getRole() != null ? filter.getRole().name() : null,
                filter.getApproved(),
                pageable
        );

        return users.map(this::toDto);
    }

    public void approveUser(int id) {
        User user = repo.findById(id).orElseThrow();
//        user.setApproved(true);
        repo.save(user);
    }

    private UserResponseDto toDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setType(user.getType());
        dto.setRole(user.getRole());
        return dto;
    }

    public void deleteUser(int id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        repo.deleteById(id);
    }


}