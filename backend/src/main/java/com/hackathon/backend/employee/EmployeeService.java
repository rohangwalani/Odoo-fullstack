package com.hackathon.backend.employee;

import com.hackathon.backend.employee.dto.EmployeeRequest;
import com.hackathon.backend.employee.dto.EmployeeResponse;
import com.hackathon.backend.employee.dto.ProfileUpdateRequest;
import com.hackathon.backend.model.Company;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Role;
import com.hackathon.backend.repository.CompanyRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.service.EmailService;
import com.hackathon.backend.service.FileStorageService;
import com.hackathon.backend.util.EmployeeIdGenerator;
import com.hackathon.backend.util.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.dto.EmployeeCardResponse;
import com.hackathon.backend.model.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeIdGenerator idGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final AttendanceRepository attendanceRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CompanyRepository companyRepository,
                           EmployeeIdGenerator idGenerator,
                           PasswordGenerator passwordGenerator,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           FileStorageService fileStorageService,
                           AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.idGenerator = idGenerator;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public EmployeeResponse addEmployee(Long adminCompanyId, EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        Company company = companyRepository.findById(adminCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));

        Employee latest = employeeRepository.findTopByCompanyIdOrderByJoiningDateDescIdDesc(adminCompanyId);
        int currentYear = LocalDate.now().getYear();

        String loginId = idGenerator.generate(company, request.getFirstName(), request.getLastName(), currentYear, latest);
        String tempPassword = passwordGenerator.generateTemporaryPassword();

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setLoginId(loginId);
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setJoiningDate(LocalDate.now());
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employee.setTemporaryPassword(true);
        employee.setRole(Role.valueOf(request.getRole() != null ? request.getRole().toUpperCase() : "EMPLOYEE"));

        employee = employeeRepository.save(employee);

        try {
            // Note: In a real app, URL would be loaded from properties
            emailService.sendEmployeeWelcomeEmail(employee.getEmail(), employee.getFirstName(), loginId, tempPassword, "http://localhost:5173/login");
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return new EmployeeResponse(employee);
    }

    public List<EmployeeResponse> getEmployees(Long companyId) {
        return employeeRepository.findByCompanyId(companyId).stream()
                .map(EmployeeResponse::new)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(Long id, Long companyId) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Not authorized to view this employee.");
        }
        return new EmployeeResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployeeByAdmin(Long id, Long companyId, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Not authorized to update this employee.");
        }

        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        if (request.getRole() != null) {
            employee.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }

        return new EmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse updateProfileByEmployee(Long id, ProfileUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress().trim());
        }
        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            String path = fileStorageService.storeFile(request.getProfilePicture());
            employee.setProfilePicture(path);
        }

        return new EmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(Long id, Long companyId) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Not authorized to delete this employee.");
        }
        employeeRepository.delete(employee);
    }

    public Page<EmployeeCardResponse> searchEmployees(Long companyId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeRepository.searchEmployees(companyId, keyword, pageable);

        return employees.map(emp -> {
            String status = "Absent"; // Default
            
            // Check if present today
            Attendance attendance = attendanceRepository.findByEmployeeAndDate(emp, LocalDate.now()).orElse(null);
            if (attendance != null) {
                status = "Present";
            } else {
                // In a real app we would check if they are on leave today
                // But for simplicity, we will just use Absent if no check-in
            }

            return new EmployeeCardResponse(
                    emp.getId(),
                    emp.getFirstName() + " " + emp.getLastName(),
                    emp.getLoginId(),
                    emp.getDepartment(),
                    emp.getProfilePicture(),
                    status
            );
        });
    }
}
