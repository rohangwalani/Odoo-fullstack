package com.hackathon.backend.profile.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PrivateInfoResponse {
    private LocalDate dateOfBirth;
    private String nationality;
    private String gender;
    private String maritalStatus;
    private String personalEmail;
    private String residentialAddress;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String panNumber;
    private String uanNumber;
    private String employeeCode;
}
