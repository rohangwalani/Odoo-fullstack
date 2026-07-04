package com.hackathon.backend.service;

import com.hackathon.backend.dto.AdminDashboardResponse;
import com.hackathon.backend.dto.EmployeeDashboardResponse;
import com.hackathon.backend.model.AttendanceStatus;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.hackathon.backend.repository.PayrollRepository;
import com.hackathon.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;

    public DashboardService(UserRepository userRepository, AttendanceRepository attendanceRepository, 
                            LeaveRequestRepository leaveRequestRepository, PayrollRepository payrollRepository,
                            AttendanceService attendanceService, LeaveService leaveService) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payrollRepository = payrollRepository;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
    }

    public EmployeeDashboardResponse getEmployeeDashboard(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        EmployeeDashboardResponse response = new EmployeeDashboardResponse();
        
        response.setEmployeeName(user.getUsername());
        response.setRole(user.getRole());
        
        // Attendance stats
        var attendances = attendanceRepository.findByUser(user);
        response.setPresentDays(attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count());
        response.setAbsentDays(attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count());
        response.setRecentAttendance(attendanceService.getMyAttendance(email)); // reusing logic
        
        // Leave stats
        var leaves = leaveRequestRepository.findByUser(user);
        response.setPendingLeaves(leaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count());
        response.setApprovedLeaves(leaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count());
        response.setRecentLeaves(leaveService.getMyLeaves(email));
        
        // Payroll stats
        payrollRepository.findByUser(user).ifPresent(p -> {
            response.setBasicSalary(p.getBasicSalary());
            response.setNetSalary(p.getNetSalary());
        });

        return response;
    }

    public AdminDashboardResponse getAdminDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        
        response.setTotalEmployees(userRepository.count());
        
        LocalDate today = LocalDate.now();
        var todaysAttendance = attendanceRepository.findByDate(today);
        response.setPresentToday(todaysAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count());
        response.setAbsentToday(todaysAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count());
        response.setEmployeesOnLeave(todaysAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.LEAVE).count());
        
        var pendingLeaves = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        response.setPendingLeaveRequests(pendingLeaves.size());
        
        // Just borrowing LeaveService's mapper logic essentially
        response.setPendingLeaves(leaveService.getAllLeaves().stream()
                .filter(l -> "PENDING".equals(l.getStatus()))
                .collect(Collectors.toList()));
        
        return response;
    }
}
