package com.hackathon.backend.repository;

import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployee(Employee employee);
}
