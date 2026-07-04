package com.hackathon.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.LeaveRequest;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendLeaveAppliedNotification(Employee employee, LeaveRequest request) {
        log.info("Sending notification: Leave Applied by {}", employee.getEmail());
        log.info("[EMAIL SENT] To: Admin, Subject: New Leave Request, Body: {} has applied for leave from {} to {}", 
                 employee.getFirstName(), request.getFromDate(), request.getToDate());
    }

    public void sendLeaveApprovedNotification(Employee employee, LeaveRequest request) {
        log.info("Sending notification: Leave Approved for {}", employee.getEmail());
        log.info("[EMAIL SENT] To: {}, Subject: Leave Approved, Body: Your leave from {} to {} has been approved.", 
                 employee.getEmail(), request.getFromDate(), request.getToDate());
    }

    public void sendLeaveRejectedNotification(Employee employee, LeaveRequest request) {
        log.info("Sending notification: Leave Rejected for {}", employee.getEmail());
        log.info("[EMAIL SENT] To: {}, Subject: Leave Rejected, Body: Your leave from {} to {} has been rejected. Reason: {}", 
                 employee.getEmail(), request.getFromDate(), request.getToDate(), request.getAdminComments());
    }
}
