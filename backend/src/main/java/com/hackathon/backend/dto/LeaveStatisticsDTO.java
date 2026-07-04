package com.hackathon.backend.dto;

public class LeaveStatisticsDTO {
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long totalLeavesThisMonth;

    public LeaveStatisticsDTO(long pendingRequests, long approvedRequests, long rejectedRequests, long totalLeavesThisMonth) {
        this.pendingRequests = pendingRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
        this.totalLeavesThisMonth = totalLeavesThisMonth;
    }

    // Getters
    public long getPendingRequests() { return pendingRequests; }
    public long getApprovedRequests() { return approvedRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public long getTotalLeavesThisMonth() { return totalLeavesThisMonth; }
}
