package com.hackathon.backend.config;

import com.hackathon.backend.model.Company;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Role;
import com.hackathon.backend.repository.CompanyRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.util.EmployeeIdGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeIdGenerator employeeIdGenerator;

    public DataSeeder(CompanyRepository companyRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, EmployeeIdGenerator employeeIdGenerator) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeIdGenerator = employeeIdGenerator;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (companyRepository.count() == 0) {
            System.out.println("Seeding dummy data for testing...");

            // 1. Create a Dummy Company
            Company company = new Company();
            company.setCompanyName("Tech Innovators Inc");
            company.setEmail("admin@techinnovators.com");
            company.setPhone("1234567890");
            company.setPassword(passwordEncoder.encode("Admin@123"));
            company.setCreatedAt(LocalDateTime.now());
            company.setUpdatedAt(LocalDateTime.now());
            company = companyRepository.save(company);

            // 2. Create an Admin Employee
            Employee admin = new Employee();
            admin.setCompany(company);
            admin.setFirstName("Alice");
            admin.setLastName("Admin");
            admin.setEmail("alice@techinnovators.com");
            admin.setPhone("9876543210");
            admin.setDepartment("Management");
            admin.setDesignation("CEO");
            admin.setJoiningDate(LocalDate.of(2023, 1, 15));
            admin.setPassword(passwordEncoder.encode("Password@123"));
            admin.setRole(Role.ADMIN);
            admin.setTemporaryPassword(false);
            
            String adminLoginId = employeeIdGenerator.generate(company, admin.getFirstName(), admin.getLastName(), 2023, null);
            admin.setLoginId(adminLoginId);
            employeeRepository.save(admin);

            // 3. Create regular Employees
            List<String[]> employeeData = Arrays.asList(
                    new String[]{"Bob", "Builder", "bob@techinnovators.com", "Engineering", "Developer"},
                    new String[]{"Charlie", "Chaplin", "charlie@techinnovators.com", "Marketing", "Executive"}
            );

            Employee latest = admin;
            for (String[] data : employeeData) {
                Employee emp = new Employee();
                emp.setCompany(company);
                emp.setFirstName(data[0]);
                emp.setLastName(data[1]);
                emp.setEmail(data[2]);
                emp.setPhone("1112223334");
                emp.setDepartment(data[3]);
                emp.setDesignation(data[4]);
                emp.setJoiningDate(LocalDate.of(2023, 5, 20));
                emp.setPassword(passwordEncoder.encode("Password@123"));
                emp.setRole(Role.EMPLOYEE);
                emp.setTemporaryPassword(false);

                String empLoginId = employeeIdGenerator.generate(company, emp.getFirstName(), emp.getLastName(), 2023, latest);
                emp.setLoginId(empLoginId);
                latest = employeeRepository.save(emp);
            }

            System.out.println("Dummy data seeded successfully.");
        }
    }
}
