package com.hackathon.backend.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PrivateInfoUpdateRequest {
    private LocalDate dateOfBirth;
    private String nationality;
    private String gender;
    private String maritalStatus;
    
    @Email(message = "Invalid email format")
    private String personalEmail;
    
    private String residentialAddress;
    private String bankName;
    
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid account number format")
    private String accountNumber;
    
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format")
    private String ifscCode;
    
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panNumber;
    
    @Pattern(regexp = "^[0-9]{12}$", message = "Invalid UAN format")
    private String uanNumber;
}
