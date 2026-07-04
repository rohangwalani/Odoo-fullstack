package com.hackathon.backend.service;

import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Notification;
import com.hackathon.backend.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(Employee employee, String message) {
        log.info("Sending notification to {}: {}", employee.getEmail(), message);
        
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setMessage(message);
        notificationRepository.save(notification);

        // Here we would also integrate Email Service sending the email in a real app
    }

    public List<Notification> getMyNotifications(Employee employee) {
        return notificationRepository.findByEmployeeOrderByTimestampDesc(employee);
    }
}
