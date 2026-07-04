package com.hackathon.backend.dto;

public class SalaryResponse {
    private Long id;
    private Double monthlySalary;
    private Double yearlySalary;
    private Integer workingDays;
    private Integer workingHours;
    private Double pfPercentage;

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

    public SalaryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(Double monthlySalary) { this.monthlySalary = monthlySalary; }
    public Double getYearlySalary() { return yearlySalary; }
    public void setYearlySalary(Double yearlySalary) { this.yearlySalary = yearlySalary; }
    public Integer getWorkingDays() { return workingDays; }
    public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }
    public Integer getWorkingHours() { return workingHours; }
    public void setWorkingHours(Integer workingHours) { this.workingHours = workingHours; }
    public Double getPfPercentage() { return pfPercentage; }
    public void setPfPercentage(Double pfPercentage) { this.pfPercentage = pfPercentage; }
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public Double getHouseRentAllowance() { return houseRentAllowance; }
    public void setHouseRentAllowance(Double houseRentAllowance) { this.houseRentAllowance = houseRentAllowance; }
    public Double getStandardAllowance() { return standardAllowance; }
    public void setStandardAllowance(Double standardAllowance) { this.standardAllowance = standardAllowance; }
    public Double getPerformanceBonus() { return performanceBonus; }
    public void setPerformanceBonus(Double performanceBonus) { this.performanceBonus = performanceBonus; }
    public Double getLeaveTravelAllowance() { return leaveTravelAllowance; }
    public void setLeaveTravelAllowance(Double leaveTravelAllowance) { this.leaveTravelAllowance = leaveTravelAllowance; }
    public Double getFixedAllowance() { return fixedAllowance; }
    public void setFixedAllowance(Double fixedAllowance) { this.fixedAllowance = fixedAllowance; }
    public Double getPfEmployee() { return pfEmployee; }
    public void setPfEmployee(Double pfEmployee) { this.pfEmployee = pfEmployee; }
    public Double getPfEmployer() { return pfEmployer; }
    public void setPfEmployer(Double pfEmployer) { this.pfEmployer = pfEmployer; }
    public Double getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(Double professionalTax) { this.professionalTax = professionalTax; }
    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }
}
