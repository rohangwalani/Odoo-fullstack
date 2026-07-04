package com.hackathon.backend.repository;

import com.hackathon.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByResetToken(String resetToken);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:department IS NULL OR LOWER(u.department) = LOWER(:department)) AND " +
            "u.role = 'ROLE_EMPLOYEE'")
    org.springframework.data.domain.Page<User> searchEmployees(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("department") String department,
            org.springframework.data.domain.Pageable pageable);
}
