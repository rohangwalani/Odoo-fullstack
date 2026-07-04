package com.hackathon.backend.profile.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileUpdateRequest {
    // Optional multipart file for avatar upload if sent via form-data
    private MultipartFile avatar;

    // Basic details
    private String phone;
    private String address;

    // Resume details
    private String about;
    private String jobDescription;
    private String hobbies;
}
