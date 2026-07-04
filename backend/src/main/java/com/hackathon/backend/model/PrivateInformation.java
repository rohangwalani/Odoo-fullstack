package com.hackathon.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "private_information")
public class PrivateInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    @JsonIgnore
    private Employee employee;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String nationality;
    
    private String gender;
    
    @Column(name = "marital_status")
    private String maritalStatus;
    
    @Column(name = "personal_email")
    private String personalEmail;
    
    @Column(name = "residential_address", columnDefinition = "TEXT")
    private String residentialAddress;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "ifsc_code")
    private String ifscCode;
    
    @Column(name = "pan_number")
    private String panNumber;
    
    @Column(name = "uan_number")
    private String uanNumber;
}
