package com.hackathon.backend.dto;

public class EmployeeCardResponse {
    private Long id;
    private String employeeName;
    private String employeeId;
    private String department;
    private String profilePicture;
    private String attendanceStatus; // Present (Green), Leave (Blue), Absent (Yellow)

    public EmployeeCardResponse() {}

    public EmployeeCardResponse(Long id, String employeeName, String employeeId, String department, String profilePicture, String attendanceStatus) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.department = department;
        this.profilePicture = profilePicture;
        this.attendanceStatus = attendanceStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
}
