package com.hackathon.backend.dto;

import java.time.LocalDate;

public class LeaveRequestDTO {
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String remarks;

    // Getters and Setters
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
