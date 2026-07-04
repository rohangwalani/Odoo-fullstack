package com.hackathon.backend.profile;

import com.hackathon.backend.model.*;
import com.hackathon.backend.profile.dto.*;
import com.hackathon.backend.profile.exception.ProfileNotFoundException;
import com.hackathon.backend.repository.*;
import com.hackathon.backend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final EmployeeRepository employeeRepository;
    private final ProfileRepository profileRepository;
    private final PrivateInformationRepository privateInfoRepository;
    private final SkillRepository skillRepository;
    private final CertificationRepository certificationRepository;
    private final FileStorageService fileStorageService;

    public ProfileService(EmployeeRepository employeeRepository,
                          ProfileRepository profileRepository,
                          PrivateInformationRepository privateInfoRepository,
                          SkillRepository skillRepository,
                          CertificationRepository certificationRepository,
                          FileStorageService fileStorageService) {
        this.employeeRepository = employeeRepository;
        this.profileRepository = profileRepository;
        this.privateInfoRepository = privateInfoRepository;
        this.skillRepository = skillRepository;
        this.certificationRepository = certificationRepository;
        this.fileStorageService = fileStorageService;
    }

    // ==============================================
    // RESUME / BASIC PROFILE
    // ==============================================

    public ProfileResponse getProfile(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Profile profile = profileRepository.findByEmployeeId(employeeId).orElse(new Profile());

        ProfileResponse response = new ProfileResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeCode(employee.getLoginId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDepartment(employee.getDepartment());
        response.setDesignation(employee.getDesignation());
        response.setAvatar(employee.getProfilePicture());
        response.setRole(employee.getRole().name());
        response.setJoiningDate(employee.getJoiningDate());

        response.setAbout(profile.getAbout());
        response.setJobDescription(profile.getJobDescription());
        response.setHobbies(profile.getHobbies());

        if (profile.getSkills() != null) {
            response.setSkills(profile.getSkills().stream().map(s -> {
                SkillDTO dto = new SkillDTO();
                dto.setId(s.getId());
                dto.setName(s.getName());
                return dto;
            }).collect(Collectors.toList()));
        }

        if (profile.getCertifications() != null) {
            response.setCertifications(profile.getCertifications().stream().map(c -> {
                CertificationDTO dto = new CertificationDTO();
                dto.setId(c.getId());
                dto.setName(c.getName());
                dto.setIssuer(c.getIssuer());
                return dto;
            }).collect(Collectors.toList()));
        }

        return response;
    }

    @Transactional
    public ProfileResponse updateProfile(Long employeeId, ProfileUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress().trim());
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String path = fileStorageService.storeFile(request.getAvatar());
            employee.setProfilePicture(path);
        }
        employeeRepository.save(employee);

        Profile profile = profileRepository.findByEmployeeId(employeeId).orElseGet(() -> {
            Profile p = new Profile();
            p.setEmployee(employee);
            return p;
        });

        if (request.getAbout() != null) profile.setAbout(request.getAbout().trim());
        if (request.getJobDescription() != null) profile.setJobDescription(request.getJobDescription().trim());
        if (request.getHobbies() != null) profile.setHobbies(request.getHobbies().trim());

        profileRepository.save(profile);

        return getProfile(employeeId);
    }

    // ==============================================
    // SKILLS
    // ==============================================

    @Transactional
    public ProfileResponse addSkill(Long employeeId, SkillDTO dto) {
        Profile profile = getOrCreateProfile(employeeId);
        Skill skill = new Skill();
        skill.setProfile(profile);
        skill.setName(dto.getName());
        skillRepository.save(skill);
        return getProfile(employeeId);
    }

    @Transactional
    public ProfileResponse updateSkill(Long employeeId, Long skillId, SkillDTO dto) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        if (!skill.getProfile().getEmployee().getId().equals(employeeId)) {
            throw new SecurityException("Unauthorized");
        }
        skill.setName(dto.getName());
        skillRepository.save(skill);
        return getProfile(employeeId);
    }

    @Transactional
    public ProfileResponse deleteSkill(Long employeeId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        if (!skill.getProfile().getEmployee().getId().equals(employeeId)) {
            throw new SecurityException("Unauthorized");
        }
        skillRepository.delete(skill);
        return getProfile(employeeId);
    }

    // ==============================================
    // CERTIFICATIONS
    // ==============================================

    @Transactional
    public ProfileResponse addCertification(Long employeeId, CertificationDTO dto) {
        Profile profile = getOrCreateProfile(employeeId);
        Certification cert = new Certification();
        cert.setProfile(profile);
        cert.setName(dto.getName());
        cert.setIssuer(dto.getIssuer());
        certificationRepository.save(cert);
        return getProfile(employeeId);
    }

    @Transactional
    public ProfileResponse updateCertification(Long employeeId, Long certId, CertificationDTO dto) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found"));
        if (!cert.getProfile().getEmployee().getId().equals(employeeId)) {
            throw new SecurityException("Unauthorized");
        }
        cert.setName(dto.getName());
        cert.setIssuer(dto.getIssuer());
        certificationRepository.save(cert);
        return getProfile(employeeId);
    }

    @Transactional
    public ProfileResponse deleteCertification(Long employeeId, Long certId) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found"));
        if (!cert.getProfile().getEmployee().getId().equals(employeeId)) {
            throw new SecurityException("Unauthorized");
        }
        certificationRepository.delete(cert);
        return getProfile(employeeId);
    }

    private Profile getOrCreateProfile(Long employeeId) {
        return profileRepository.findByEmployeeId(employeeId).orElseGet(() -> {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            Profile profile = new Profile();
            profile.setEmployee(employee);
            return profileRepository.save(profile);
        });
    }

    // ==============================================
    // PRIVATE INFORMATION
    // ==============================================

    public PrivateInfoResponse getPrivateInfo(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        PrivateInformation info = privateInfoRepository.findByEmployeeId(employeeId).orElse(new PrivateInformation());

        PrivateInfoResponse response = new PrivateInfoResponse();
        response.setDateOfBirth(info.getDateOfBirth());
        response.setNationality(info.getNationality());
        response.setGender(info.getGender());
        response.setMaritalStatus(info.getMaritalStatus());
        response.setPersonalEmail(info.getPersonalEmail());
        response.setResidentialAddress(info.getResidentialAddress());
        response.setBankName(info.getBankName());
        response.setAccountNumber(info.getAccountNumber());
        response.setIfscCode(info.getIfscCode());
        response.setPanNumber(info.getPanNumber());
        response.setUanNumber(info.getUanNumber());
        response.setEmployeeCode(employee.getLoginId());
        return response;
    }

    @Transactional
    public PrivateInfoResponse updatePrivateInfo(Long employeeId, PrivateInfoUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        PrivateInformation info = privateInfoRepository.findByEmployeeId(employeeId).orElseGet(() -> {
            PrivateInformation i = new PrivateInformation();
            i.setEmployee(employee);
            return i;
        });

        if (request.getDateOfBirth() != null) info.setDateOfBirth(request.getDateOfBirth());
        if (request.getNationality() != null) info.setNationality(request.getNationality());
        if (request.getGender() != null) info.setGender(request.getGender());
        if (request.getMaritalStatus() != null) info.setMaritalStatus(request.getMaritalStatus());
        if (request.getPersonalEmail() != null) info.setPersonalEmail(request.getPersonalEmail());
        if (request.getResidentialAddress() != null) info.setResidentialAddress(request.getResidentialAddress());
        if (request.getBankName() != null) info.setBankName(request.getBankName());
        if (request.getAccountNumber() != null) info.setAccountNumber(request.getAccountNumber());
        if (request.getIfscCode() != null) info.setIfscCode(request.getIfscCode());
        if (request.getPanNumber() != null) info.setPanNumber(request.getPanNumber());
        if (request.getUanNumber() != null) info.setUanNumber(request.getUanNumber());

        privateInfoRepository.save(info);
        return getPrivateInfo(employeeId);
    }
}
