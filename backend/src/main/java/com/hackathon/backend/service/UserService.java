package com.hackathon.backend.service;

import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.hackathon.backend.repository.AttendanceRepository attendanceRepository;
    private final com.hackathon.backend.repository.LeaveRequestRepository leaveRequestRepository;

    public UserService(UserRepository userRepository, 
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                       com.hackathon.backend.repository.AttendanceRepository attendanceRepository,
                       com.hackathon.backend.repository.LeaveRequestRepository leaveRequestRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveResetToken(User user, String token) {
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
    }

    public Optional<User> validateResetToken(String token) {
        return userRepository.findByResetToken(token)
                .filter(user -> user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now()));
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public org.springframework.data.domain.Page<com.hackathon.backend.dto.EmployeeCardResponse> searchEmployees(
            String keyword, String department, org.springframework.data.domain.Pageable pageable) {
        
        return userRepository.searchEmployees(keyword, department, pageable)
                .map(user -> {
                    String status = "Absent"; // Default
                    java.time.LocalDate today = java.time.LocalDate.now();
                    
                    // Check if present today
                    if (attendanceRepository.findByUserAndDate(user, today).isPresent()) {
                        status = "Present";
                    } else {
                        // Check if on leave today
                        long activeLeaves = leaveRequestRepository.countApprovedLeavesForUserOnDate(user, today);
                        if (activeLeaves > 0) {
                            status = "Leave";
                        }
                    }

                    return new com.hackathon.backend.dto.EmployeeCardResponse(
                            user.getId(),
                            user.getUsername(),
                            "EMP" + String.format("%04d", user.getId()),
                            user.getDepartment(),
                            user.getProfilePicture(),
                            status
                    );
                });
    }
}
