package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveRequestDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.LeaveType;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.hackathon.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    public LeaveResponseDTO applyForLeave(String email, LeaveRequestDTO dto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        LeaveRequest request = new LeaveRequest();
        request.setUser(user);
        request.setLeaveType(LeaveType.valueOf(dto.getLeaveType().toUpperCase()));
        request.setFromDate(dto.getFromDate());
        request.setToDate(dto.getToDate());
        request.setRemarks(dto.getRemarks());
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        return mapToResponse(saved);
    }

    public List<LeaveResponseDTO> getMyLeaves(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return leaveRequestRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LeaveResponseDTO approveLeave(Long id, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminComments(comments);
        return mapToResponse(leaveRequestRepository.save(request));
    }

    public LeaveResponseDTO rejectLeave(Long id, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus(LeaveStatus.REJECTED);
        request.setAdminComments(comments);
        return mapToResponse(leaveRequestRepository.save(request));
    }

    private LeaveResponseDTO mapToResponse(LeaveRequest l) {
        return new LeaveResponseDTO(
                l.getId(),
                l.getUser().getId(),
                l.getUser().getUsername(),
                l.getLeaveType().name(),
                l.getFromDate(),
                l.getToDate(),
                l.getRemarks(),
                l.getStatus().name(),
                l.getAdminComments()
        );
    }
}
