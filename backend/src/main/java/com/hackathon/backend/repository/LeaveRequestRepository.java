package com.hackathon.backend.repository;

import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    long countByStatus(LeaveStatus status);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE (YEAR(l.fromDate) = YEAR(CURRENT_DATE) AND MONTH(l.fromDate) = MONTH(CURRENT_DATE)) OR (YEAR(l.toDate) = YEAR(CURRENT_DATE) AND MONTH(l.toDate) = MONTH(CURRENT_DATE))")
    long countTotalLeavesThisMonth();

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee = :employee AND l.status = 'APPROVED' AND :date BETWEEN l.fromDate AND l.toDate")
    long countApprovedLeavesForEmployeeOnDate(@Param("employee") Employee employee, @Param("date") LocalDate date);
}
