package com.hackathon.backend.repository;

import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUser(User user);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.user = :user AND l.status = 'APPROVED' AND l.fromDate <= :date AND l.toDate >= :date")
    long countApprovedLeavesForUserOnDate(@org.springframework.data.repository.query.Param("user") User user, @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}
