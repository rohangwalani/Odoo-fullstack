package com.hackathon.backend.service;

import com.hackathon.backend.dto.SalaryResponse;
import com.hackathon.backend.dto.SalaryUpdateRequest;
import com.hackathon.backend.exception.InvalidSalaryException;
import com.hackathon.backend.exception.SalaryNotFoundException;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Salary;
import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.repository.SalaryRepository;
import com.hackathon.backend.repository.PayrollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SalaryService {
    private static final Logger log = LoggerFactory.getLogger(SalaryService.class);

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;

    public SalaryService(SalaryRepository salaryRepository, EmployeeRepository employeeRepository, PayrollRepository payrollRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
    }

    public SalaryResponse getSalary(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Salary salary = salaryRepository.findByEmployee(employee).orElse(null);
        
        if (salary == null) {
            SalaryResponse emptyResponse = new SalaryResponse();
            emptyResponse.setMonthlySalary(0.0);
            emptyResponse.setYearlySalary(0.0);
            emptyResponse.setWorkingDays(5);
            emptyResponse.setWorkingHours(8);
            emptyResponse.setPfPercentage(12.0);
            emptyResponse.setProfessionalTax(200.0);
            return emptyResponse;
        }
        
        return mapToResponse(salary);
    }

    public SalaryResponse updateSalary(Long employeeId, SalaryUpdateRequest request) {
        if (request.getMonthlySalary() == null || request.getMonthlySalary() <= 0) {
            throw new InvalidSalaryException("Monthly salary must be greater than zero");
        }
        if (request.getProfessionalTax() != null && request.getProfessionalTax() < 0) {
            throw new InvalidSalaryException("Tax cannot be negative");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Salary salary = salaryRepository.findByEmployee(employee).orElse(new Salary());

        salary.setEmployee(employee);
        salary.setMonthlySalary(request.getMonthlySalary());

        if (request.getProfessionalTax() != null) {
            salary.setProfessionalTax(request.getProfessionalTax());
        } else if (salary.getProfessionalTax() == null) {
            salary.setProfessionalTax(200.0); // Default professional tax in India
        }

        calculateSalaryComponents(salary);

        Salary savedSalary = salaryRepository.save(salary);
        
        // Sync with Payroll table so /api/payroll/me works
        Payroll payroll = payrollRepository.findByEmployee(employee).orElse(new Payroll());
        payroll.setEmployee(employee);
        payroll.setBasicSalary(savedSalary.getBasicSalary());
        payroll.setAllowances(savedSalary.getHouseRentAllowance() + savedSalary.getPerformanceBonus() + 
                              savedSalary.getLeaveTravelAllowance() + savedSalary.getStandardAllowance() + 
                              savedSalary.getFixedAllowance());
        payroll.setDeductions(savedSalary.getPfEmployee() + savedSalary.getProfessionalTax());
        payroll.setNetSalary(savedSalary.getNetSalary());
        payrollRepository.save(payroll);

        return mapToResponse(savedSalary);
    }

    private void calculateSalaryComponents(Salary salary) {
        Double monthly = salary.getMonthlySalary();
        salary.setYearlySalary(monthly * 12);

        // Basic is 50% of monthly
        Double basic = Math.round(monthly * 0.50 * 100.0) / 100.0;
        salary.setBasicSalary(basic);

        // HRA is 50% of basic
        Double hra = Math.round(basic * 0.50 * 100.0) / 100.0;
        salary.setHouseRentAllowance(hra);

        // Performance Bonus 8.33% of monthly
        Double bonus = Math.round(monthly * 0.0833 * 100.0) / 100.0;
        salary.setPerformanceBonus(bonus);

        // LTA 8.33% of monthly
        Double lta = Math.round(monthly * 0.0833 * 100.0) / 100.0;
        salary.setLeaveTravelAllowance(lta);

        // Standard Allowance is fixed (let's say 0 for now)
        salary.setStandardAllowance(0.0);

        // Fixed Allowance is the rest
        Double fixed = Math.round((monthly - (basic + hra + bonus + lta + salary.getStandardAllowance())) * 100.0) / 100.0;
        if (fixed < 0) fixed = 0.0;
        salary.setFixedAllowance(fixed);

        // PF (12% of Basic)
        Double pfPercentage = salary.getPfPercentage() / 100.0;
        Double pf = Math.round(basic * pfPercentage * 100.0) / 100.0;
        salary.setPfEmployee(pf);
        salary.setPfEmployer(pf);

        // Net Salary
        Double net = Math.round((monthly - salary.getPfEmployee() - salary.getProfessionalTax()) * 100.0) / 100.0;
        salary.setNetSalary(net);
    }

    private SalaryResponse mapToResponse(Salary salary) {
        SalaryResponse dto = new SalaryResponse();
        dto.setId(salary.getId());
        dto.setMonthlySalary(salary.getMonthlySalary());
        dto.setYearlySalary(salary.getYearlySalary());
        dto.setWorkingDays(salary.getWorkingDays());
        dto.setWorkingHours(salary.getWorkingHours());
        dto.setPfPercentage(salary.getPfPercentage());

        dto.setBasicSalary(salary.getBasicSalary());
        dto.setHouseRentAllowance(salary.getHouseRentAllowance());
        dto.setStandardAllowance(salary.getStandardAllowance());
        dto.setPerformanceBonus(salary.getPerformanceBonus());
        dto.setLeaveTravelAllowance(salary.getLeaveTravelAllowance());
        dto.setFixedAllowance(salary.getFixedAllowance());

        dto.setPfEmployee(salary.getPfEmployee());
        dto.setPfEmployer(salary.getPfEmployer());
        dto.setProfessionalTax(salary.getProfessionalTax());
        dto.setNetSalary(salary.getNetSalary());

        return dto;
    }
}
