package com.hackathon.backend.dto;

import java.util.List;

public class EmployeeDashboardResponse {
    private String employeeName;
    private String role;
    
    // Summary
    private long presentDays;
    private long absentDays;
    private long pendingLeaves;
    private long approvedLeaves;
    
    // Payroll Summary
    private Double basicSalary;
    private Double netSalary;

    // Recent Activity / Records
    private List<AttendanceResponse> recentAttendance;
    private List<LeaveResponseDTO> recentLeaves;

    // Getters and Setters
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public long getPresentDays() { return presentDays; }
    public void setPresentDays(long presentDays) { this.presentDays = presentDays; }
    public long getAbsentDays() { return absentDays; }
    public void setAbsentDays(long absentDays) { this.absentDays = absentDays; }
    public long getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(long pendingLeaves) { this.pendingLeaves = pendingLeaves; }
    public long getApprovedLeaves() { return approvedLeaves; }
    public void setApprovedLeaves(long approvedLeaves) { this.approvedLeaves = approvedLeaves; }
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }
    public List<AttendanceResponse> getRecentAttendance() { return recentAttendance; }
    public void setRecentAttendance(List<AttendanceResponse> recentAttendance) { this.recentAttendance = recentAttendance; }
    public List<LeaveResponseDTO> getRecentLeaves() { return recentLeaves; }
    public void setRecentLeaves(List<LeaveResponseDTO> recentLeaves) { this.recentLeaves = recentLeaves; }
}
