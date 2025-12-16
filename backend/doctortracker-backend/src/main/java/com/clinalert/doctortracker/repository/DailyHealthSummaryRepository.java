package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.DailyHealthSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyHealthSummaryRepository extends JpaRepository<DailyHealthSummary, String> {

    Optional<DailyHealthSummary> findByPatientIdAndDate(String patientId, LocalDate date);

    List<DailyHealthSummary> findByPatientIdOrderByDateDesc(String patientId);

    List<DailyHealthSummary> findTop30ByPatientIdOrderByDateDesc(String patientId);

    List<DailyHealthSummary> findByPatientIdAndDateBetweenOrderByDateAsc(
            String patientId,
            LocalDate startDate,
            LocalDate endDate);

    boolean existsByPatientIdAndDate(String patientId, LocalDate date);
}
