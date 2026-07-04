package com.hackathon.backend.dto;

import java.util.List;

public class LeaveReportDTO {
    private String reportType;
    private String generatedAt;
    private List<LeaveResponseDTO> data;

    public LeaveReportDTO(String reportType, String generatedAt, List<LeaveResponseDTO> data) {
        this.reportType = reportType;
        this.generatedAt = generatedAt;
        this.data = data;
    }

    public String getReportType() { return reportType; }
    public String getGeneratedAt() { return generatedAt; }
    public List<LeaveResponseDTO> getData() { return data; }
}
