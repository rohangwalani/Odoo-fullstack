package com.hackathon.backend.dto;

import lombok.Data;

@Data
public class SalaryComponentDTO {
    private Double basicSalary;
    private Double houseRentAllowance;
    private Double standardAllowance;
    private Double performanceBonus;
    private Double leaveTravelAllowance;
    private Double fixedAllowance;
    private Double pfEmployee;
    private Double pfEmployer;
    private Double professionalTax;
    private Double netSalary;
}
