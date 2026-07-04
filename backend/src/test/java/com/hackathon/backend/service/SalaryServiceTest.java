package com.hackathon.backend.service;

import com.hackathon.backend.dto.SalaryResponse;
import com.hackathon.backend.dto.SalaryUpdateRequest;
import com.hackathon.backend.exception.InvalidSalaryException;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Salary;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SalaryService salaryService;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFirstName("John");
        mockEmployee.setLastName("Doe");
    }

    @Test
    void updateSalary_CalculatesComponentsProperly() {
        SalaryUpdateRequest request = new SalaryUpdateRequest();
        request.setMonthlySalary(50000.0);
        request.setProfessionalTax(200.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(salaryRepository.findByEmployee(mockEmployee)).thenReturn(Optional.empty());
        when(salaryRepository.save(any(Salary.class))).thenAnswer(i -> i.getArguments()[0]);

        SalaryResponse response = salaryService.updateSalary(1L, request);

        assertNotNull(response);
        assertEquals(50000.0, response.getMonthlySalary());
        assertEquals(600000.0, response.getYearlySalary());
        assertEquals(25000.0, response.getBasicSalary()); // 50%
        assertEquals(12500.0, response.getHouseRentAllowance()); // 50% of basic
        assertEquals(4165.0, response.getPerformanceBonus()); // 8.33% of 50000 = 4165
        assertEquals(4165.0, response.getLeaveTravelAllowance()); // 8.33% of 50000 = 4165
        assertEquals(3000.0, response.getPfEmployee()); // 12% of 25000 basic = 3000
        assertEquals(46800.0, response.getNetSalary()); // 50000 - 3000 (PF) - 200 (Tax)
        assertEquals(4170.0, response.getFixedAllowance()); // 50000 - (25000 + 12500 + 4165 + 4165) = 4170
    }

    @Test
    void updateSalary_ThrowsException_IfNegativeSalary() {
        SalaryUpdateRequest request = new SalaryUpdateRequest();
        request.setMonthlySalary(-5000.0);

        assertThrows(InvalidSalaryException.class, () -> salaryService.updateSalary(1L, request));
    }
}
