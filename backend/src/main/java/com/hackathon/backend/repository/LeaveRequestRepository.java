package com.hackathon.backend.repository;

import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUser(User user);
    List<LeaveRequest> findByStatus(LeaveStatus status);
}
