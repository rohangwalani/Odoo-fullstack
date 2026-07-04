package com.hackathon.backend.dto;

import java.time.LocalDate;

public class LeaveResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String remarks;
    private String status;
    private String adminComments;

    public LeaveResponseDTO(Long id, Long employeeId, String employeeName, String leaveType, LocalDate fromDate, LocalDate toDate, String remarks, String status, String adminComments) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.remarks = remarks;
        this.status = status;
        this.adminComments = adminComments;
    }

    // Getters
    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getLeaveType() { return leaveType; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public String getRemarks() { return remarks; }
    public String getStatus() { return status; }
    public String getAdminComments() { return adminComments; }
}
