package com.hackathon.backend.service;

import com.hackathon.backend.dto.PayrollRequestDTO;
import com.hackathon.backend.dto.PayrollResponse;
import com.hackathon.backend.model.Payroll;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.PayrollRepository;
import com.hackathon.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;

    public PayrollService(PayrollRepository payrollRepository, UserRepository userRepository) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
    }

    public PayrollResponse getMyPayroll(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Payroll payroll = payrollRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Payroll not generated yet"));
        return mapToResponse(payroll);
    }

    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PayrollResponse updatePayroll(Long employeeId, PayrollRequestDTO dto) {
        User user = userRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("User not found"));
        Payroll payroll = payrollRepository.findByUser(user).orElse(new Payroll());
        
        payroll.setUser(user);
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
                p.getUser().getId(),
                p.getUser().getUsername(),
                p.getBasicSalary(),
                p.getAllowances(),
                p.getDeductions(),
                p.getNetSalary()
        );
    }
}
