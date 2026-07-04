package com.hackathon.backend.dto;

public class PayrollResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double netSalary;

    public PayrollResponse(Long id, Long employeeId, String employeeName, Double basicSalary, Double allowances, Double deductions, Double netSalary) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netSalary = netSalary;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public Double getBasicSalary() { return basicSalary; }
    public Double getAllowances() { return allowances; }
    public Double getDeductions() { return deductions; }
    public Double getNetSalary() { return netSalary; }
}
