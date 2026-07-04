package com.hackathon.backend.service;

import com.hackathon.backend.dto.PayrollRequestDTO;
import com.hackathon.backend.dto.PayrollResponse;
import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.PayrollRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hackathon.backend.exception.PayrollNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollService(PayrollRepository payrollRepository, EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    public PayrollResponse getMyPayroll(String email) {
        log.info("Fetching payroll for user: {}", email);
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Payroll payroll = payrollRepository.findByEmployee(employee).orElseThrow(() -> new PayrollNotFoundException("Payroll not generated yet"));
        return mapToResponse(payroll);
    }

    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PayrollResponse updatePayroll(Long employeeId, PayrollRequestDTO dto) {
        log.info("Updating payroll for employee ID: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Payroll payroll = payrollRepository.findByEmployee(employee).orElse(new Payroll());
        
        payroll.setEmployee(employee);
        payroll.setBasicSalary(dto.getBasicSalary());
        payroll.setAllowances(dto.getAllowances());
        payroll.setDeductions(dto.getDeductions());
        
        // Calculate net salary
        double net = (dto.getBasicSalary() != null ? dto.getBasicSalary() : 0.0) +
                     (dto.getAllowances() != null ? dto.getAllowances() : 0.0) -
                     (dto.getDeductions() != null ? dto.getDeductions() : 0.0);
        payroll.setNetSalary(net);

        Payroll saved = payrollRepository.save(payroll);
        return mapToResponse(saved);
    }

    private PayrollResponse mapToResponse(Payroll p) {
        return new PayrollResponse(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName(),
                p.getBasicSalary(),
                p.getAllowances(),
                p.getDeductions(),
                p.getNetSalary()
        );
    }
}
