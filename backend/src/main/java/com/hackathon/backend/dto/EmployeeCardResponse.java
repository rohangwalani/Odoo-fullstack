package com.hackathon.backend.dto;

public class EmployeeCardResponse {
    private Long id;
    private String name;
    private String employeeId;
    private String department;
    private String profilePicture;
    private String attendanceStatus; // Present (Green), Leave (Blue), Absent (Yellow)

    public EmployeeCardResponse() {}

    public EmployeeCardResponse(Long id, String name, String employeeId, String department, String profilePicture, String attendanceStatus) {
        this.id = id;
        this.name = name;
        this.employeeId = employeeId;
        this.department = department;
        this.profilePicture = profilePicture;
        this.attendanceStatus = attendanceStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
}
