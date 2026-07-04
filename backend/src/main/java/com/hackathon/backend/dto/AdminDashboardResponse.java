package com.hackathon.backend.dto;

import java.util.List;

public class AdminDashboardResponse {
    
    // Summary Stats
    private long totalEmployees;
    private long presentToday;
    private long absentToday;
    private long employeesOnLeave;
    private long pendingLeaveRequests;
    
    // Quick Access Lists
    private List<LeaveResponseDTO> pendingLeaves;

    // Getters and Setters
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public long getPresentToday() { return presentToday; }
    public void setPresentToday(long presentToday) { this.presentToday = presentToday; }
    public long getAbsentToday() { return absentToday; }
    public void setAbsentToday(long absentToday) { this.absentToday = absentToday; }
    public long getEmployeesOnLeave() { return employeesOnLeave; }
    public void setEmployeesOnLeave(long employeesOnLeave) { this.employeesOnLeave = employeesOnLeave; }
    public long getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(long pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }
    public List<LeaveResponseDTO> getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(List<LeaveResponseDTO> pendingLeaves) { this.pendingLeaves = pendingLeaves; }
}
