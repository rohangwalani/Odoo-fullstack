package com.hackathon.backend.dto;

public class SalaryUpdateRequest {
    private Double monthlySalary;
    private Double professionalTax;
    private Integer workingDays;
    private Integer workingHours;

    public SalaryUpdateRequest() {}

    public Double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(Double monthlySalary) { this.monthlySalary = monthlySalary; }

    public Double getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(Double professionalTax) { this.professionalTax = professionalTax; }
    
    public Integer getWorkingDays() { return workingDays; }
    public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }
    
    public Integer getWorkingHours() { return workingHours; }
    public void setWorkingHours(Integer workingHours) { this.workingHours = workingHours; }
}
