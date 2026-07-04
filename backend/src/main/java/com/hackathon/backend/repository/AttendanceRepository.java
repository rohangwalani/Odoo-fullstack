package com.hackathon.backend.repository;

import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    List<Attendance> findByEmployee(Employee employee);
    List<Attendance> findByDate(LocalDate date);
}
