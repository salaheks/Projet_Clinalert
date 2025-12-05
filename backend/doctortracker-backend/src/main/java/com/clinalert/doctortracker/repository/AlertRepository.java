package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {
    List<Alert> findByPatientId(String patientId);

    List<Alert> findByIsReadFalse();

    List<Alert> findTop10ByPatientIdOrderByTimestampDesc(String patientId);

    List<Alert> findBySeverityAndTimestampBefore(String severity, java.time.LocalDateTime timestamp);
}
