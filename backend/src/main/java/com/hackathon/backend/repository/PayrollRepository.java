package com.hackathon.backend.repository;

import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByUser(User user);
}
