package com.hackathon.backend.employee.dto;

import org.springframework.web.multipart.MultipartFile;

public class ProfileUpdateRequest {

    private String phone;
    private String address;
    private MultipartFile profilePicture;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public MultipartFile getProfilePicture() { return profilePicture; }
    public void setProfilePicture(MultipartFile profilePicture) { this.profilePicture = profilePicture; }
}
