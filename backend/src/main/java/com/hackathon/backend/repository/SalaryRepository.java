package com.hackathon.backend.repository;

import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {
    Optional<Salary> findByEmployee(Employee employee);
}
