package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.CompanySignupRequest;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.VerifyOtpRequest;
import com.hackathon.backend.auth.dto.ChangePasswordRequest;
import com.hackathon.backend.auth.dto.ForgotPasswordRequest;
import com.hackathon.backend.auth.dto.ResetPasswordRequest;
import com.hackathon.backend.auth.exception.RateLimitExceededException;
import com.hackathon.backend.model.Company;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Role;
import com.hackathon.backend.repository.CompanyRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.security.JwtUtils;
import com.hackathon.backend.service.FileStorageService;
import com.hackathon.backend.service.RateLimitService;
import com.hackathon.backend.service.TwoFactorService;
import com.hackathon.backend.util.EmployeeIdGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final EmployeeIdGenerator employeeIdGenerator;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RateLimitService rateLimitService;
    private final TwoFactorService twoFactorService;

    public AuthService(CompanyRepository companyRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService,
                       EmployeeIdGenerator employeeIdGenerator,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       RateLimitService rateLimitService,
                       TwoFactorService twoFactorService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.employeeIdGenerator = employeeIdGenerator;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.rateLimitService = rateLimitService;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public AuthResponse registerCompany(CompanySignupRequest request) {
        if (companyRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent() ||
            employeeRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (companyRepository.findByCompanyName(request.getCompanyName().trim()).isPresent()) {
            throw new IllegalArgumentException("Company name is already registered.");
        }

        String logoPath = null;
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            logoPath = fileStorageService.storeFile(request.getLogo());
        }

        Company company = new Company();
        company.setCompanyName(request.getCompanyName().trim());
        company.setEmail(request.getEmail().trim().toLowerCase());
        company.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        company.setPassword(passwordEncoder.encode(request.getPassword()));
        company.setCompanyLogo(logoPath);

        company = companyRepository.save(company);

        // Generate Admin Employee
        Employee admin = new Employee();
        admin.setCompany(company);
        admin.setFirstName(request.getFirstName().trim());
        admin.setLastName(request.getLastName().trim());
        admin.setEmail(request.getEmail().trim().toLowerCase());
        admin.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        admin.setPassword(passwordEncoder.encode(request.getPassword())); // Admin uses same pass initially
        admin.setRole(Role.ADMIN);
        admin.setTemporaryPassword(false);
        admin.setJoiningDate(LocalDate.now());

        String loginId = employeeIdGenerator.generate(company, admin.getFirstName(), admin.getLastName(), admin.getJoiningDate().getYear(), null);
        while (employeeRepository.findByLoginId(loginId).isPresent()) {
            Employee fakeLatest = new Employee();
            fakeLatest.setLoginId(loginId);
            loginId = employeeIdGenerator.generate(company, admin.getFirstName(), admin.getLastName(), admin.getJoiningDate().getYear(), fakeLatest);
        }
        admin.setLoginId(loginId);

        employeeRepository.save(admin);

        return AuthResponse.success("Company registered successfully. You can now login.", admin.getId(), admin.getLoginId(), admin.getEmail(), admin.getRole().name(), company.getId(), null);
    }

    public AuthResponse login(LoginRequest request) {
        String emailOrLoginId = request.getEmail().trim();

        if (rateLimitService.isLoginRateLimited(emailOrLoginId)) {
            throw new RateLimitExceededException("Too many failed login attempts. Please wait 5 minutes before trying again.");
        }

        Employee employee = employeeRepository.findByEmail(emailOrLoginId.toLowerCase())
                .orElseGet(() -> employeeRepository.findByLoginId(emailOrLoginId)
                        .orElseThrow(() -> {
                            rateLimitService.recordLoginAttempt(emailOrLoginId);
                            return new IllegalArgumentException("Invalid credentials.");
                        }));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            rateLimitService.recordLoginAttempt(emailOrLoginId);
            throw new IllegalArgumentException("Invalid credentials.");
        }

        rateLimitService.clearLoginAttempts(emailOrLoginId);

        if (employee.isTwoFactorEnabled()) {
            twoFactorService.generateAndStoreOtp(employee.getId());
            // TODO: In a real flow, send OTP via EmailService here
            return AuthResponse.pending2FA(employee.getId());
        }

        return getAuthResponseWithJwt(employee);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Long userId = request.getUserId();

        if (rateLimitService.isOtpRateLimited(userId)) {
            throw new RateLimitExceededException("Too many OTP attempts.");
        }

        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session."));

        rateLimitService.recordOtpAttempt(userId);

        if (!twoFactorService.verifyOtp(userId, request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        rateLimitService.clearOtpAttempts(userId);

        return getAuthResponseWithJwt(employee);
    }

    private AuthResponse getAuthResponseWithJwt(Employee employee) {
        com.hackathon.backend.security.CustomUserDetails userDetails = com.hackathon.backend.security.CustomUserDetails.build(employee);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        return AuthResponse.success(
                "Login successful.",
                employee.getId(),
                employee.getLoginId(),
                employee.getEmail(),
                employee.getRole().name(),
                employee.getCompany().getId(),
                jwt
        );
    }

    @Transactional
    public AuthResponse enableTwoFactor(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setTwoFactorEnabled(true);
        employeeRepository.save(employee);
        return AuthResponse.success("Two-factor authentication enabled.");
    }

    @Transactional
    public AuthResponse disableTwoFactor(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setTwoFactorEnabled(false);
        twoFactorService.invalidateOtp(employeeId);
        employeeRepository.save(employee);
        return AuthResponse.success("Two-factor authentication disabled.");
    }

    public AuthResponse logout() {
        return AuthResponse.success("Logged out successfully.");
    }

    @Transactional
    public AuthResponse changePassword(Long employeeId, ChangePasswordRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("Invalid old password.");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employee.setTemporaryPassword(false);
        employeeRepository.save(employee);

        return AuthResponse.success("Password changed successfully.");
    }

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        Employee employee = employeeRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Email not found."));

        String resetToken = java.util.UUID.randomUUID().toString();
        employee.setResetToken(resetToken);
        employee.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        employeeRepository.save(employee);

        // TODO: Send email with resetToken
        // emailService.sendPasswordResetEmail(employee.getEmail(), resetToken);

        return AuthResponse.success("Password reset link sent to your email.");
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Employee employee = employeeRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token."));

        if (employee.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired.");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employee.setResetToken(null);
        employee.setResetTokenExpiry(null);
        employee.setTemporaryPassword(false);
        employeeRepository.save(employee);

        return AuthResponse.success("Password reset successfully.");
    }
}
