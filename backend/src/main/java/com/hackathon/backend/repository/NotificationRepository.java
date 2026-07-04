package com.hackathon.backend.repository;

import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeOrderByTimestampDesc(Employee employee);
    List<Notification> findByEmployeeAndIsReadFalseOrderByTimestampDesc(Employee employee);
}
