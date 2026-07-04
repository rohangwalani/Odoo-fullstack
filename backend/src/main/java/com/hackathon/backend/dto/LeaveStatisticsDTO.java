package com.hackathon.backend.dto;

import lombok.Data;

@Data
public class LeaveStatisticsDTO {
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
}
