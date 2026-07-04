package com.hackathon.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveSearchDTO {
    private String employeeName;
    private String leaveType;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
}
