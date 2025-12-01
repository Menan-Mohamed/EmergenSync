package com.example.backend.repositories;

import com.example.backend.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    @Query(
            value = "SELECT * FROM users u WHERE " +
                    "(:search IS NULL OR u.username LIKE CONCAT('%', :search, '%') OR u.email LIKE CONCAT('%', :search, '%')) AND " +
                    "(:type IS NULL OR u.type = :type) AND " +
                    "(:role IS NULL OR u.role = :role) AND " +
                    "(:approved IS NULL OR u.approved = :approved)",

            countQuery = "SELECT COUNT(*) FROM users u WHERE " +
                    "(:search IS NULL OR u.username LIKE CONCAT('%', :search, '%') OR u.email LIKE CONCAT('%', :search, '%')) AND " +
                    "(:type IS NULL OR u.type = :type) AND " +
                    "(:role IS NULL OR u.role = :role) AND " +
                    "(:approved IS NULL OR u.approved = :approved)",

            nativeQuery = true
    )
    Page<User> findByFilters(
            @Param("search") String search,
            @Param("type") String type,
            @Param("role") String role,
            @Param("approved") Boolean approved,
            Pageable pageable
    );
}
