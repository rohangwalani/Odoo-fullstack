package com.hackathon.backend.repository;

import com.hackathon.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND (" +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployees(@Param("companyId") Long companyId, @Param("keyword") String keyword, Pageable pageable);
}
