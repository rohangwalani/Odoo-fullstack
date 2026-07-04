package com.hackathon.backend.repository;

import com.hackathon.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByLoginId(String loginId);
    Optional<Employee> findByResetToken(String resetToken);
    
    // Find all employees for a specific company
    List<Employee> findByCompanyId(Long companyId);
    
    // Check if an email exists
    boolean existsByEmail(String email);
    
    // Find the latest employee in a company (for ID generation serial number)
    Employee findTopByCompanyIdOrderByJoiningDateDescIdDesc(Long companyId);
}
