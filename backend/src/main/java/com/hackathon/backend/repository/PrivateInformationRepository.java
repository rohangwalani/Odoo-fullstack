package com.hackathon.backend.repository;

import com.hackathon.backend.model.PrivateInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivateInformationRepository extends JpaRepository<PrivateInformation, Long> {
    Optional<PrivateInformation> findByEmployeeId(Long employeeId);
}
