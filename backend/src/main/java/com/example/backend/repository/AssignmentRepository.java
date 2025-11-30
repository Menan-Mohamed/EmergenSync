package com.example.backend.repository;

import com.example.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
}
