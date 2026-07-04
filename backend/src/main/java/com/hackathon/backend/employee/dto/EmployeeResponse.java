package com.hackathon.backend.employee.dto;

import com.hackathon.backend.model.Employee;
import java.time.LocalDate;

public class EmployeeResponse {

    private Long id;
    private String loginId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String department;
    private String designation;
    private LocalDate joiningDate;
    private String role;
    private String profilePicture;
    private Long companyId;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.loginId = employee.getLoginId();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.address = employee.getAddress();
        this.department = employee.getDepartment();
        this.designation = employee.getDesignation();
        this.joiningDate = employee.getJoiningDate();
        this.role = employee.getRole().name();
        this.profilePicture = employee.getProfilePicture();
        this.companyId = employee.getCompany().getId();
    }

    // Getters

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public String getRole() { return role; }
    public String getProfilePicture() { return profilePicture; }
    public Long getCompanyId() { return companyId; }
}
