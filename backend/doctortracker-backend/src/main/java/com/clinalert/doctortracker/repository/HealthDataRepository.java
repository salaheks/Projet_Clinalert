package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthDataRepository extends JpaRepository<HealthData, String> {

    List<HealthData> findByPatientIdOrderByTimestampDesc(String patientId);

    List<HealthData> findTop50ByPatientIdOrderByTimestampDesc(String patientId);

    List<HealthData> findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
            String patientId,
            LocalDateTime start,
            LocalDateTime end);

    List<HealthData> findByDeviceIdOrderByTimestampDesc(String deviceId);

    @Query("SELECT h FROM HealthData h WHERE h.patientId = :patientId " +
            "AND h.heartRate IS NOT NULL ORDER BY h.timestamp DESC")
    List<HealthData> findHeartRateDataByPatientId(@Param("patientId") String patientId);

    @Query("SELECT h FROM HealthData h WHERE h.patientId = :patientId " +
            "AND h.steps IS NOT NULL ORDER BY h.timestamp DESC")
    List<HealthData> findStepsDataByPatientId(@Param("patientId") String patientId);

    @Query("SELECT h FROM HealthData h WHERE h.patientId = :patientId " +
            "AND h.spO2 IS NOT NULL ORDER BY h.timestamp DESC")
    List<HealthData> findSpO2DataByPatientId(@Param("patientId") String patientId);

    @Query("SELECT h FROM HealthData h WHERE h.patientId = :patientId " +
            "AND h.sleepMinutes IS NOT NULL ORDER BY h.timestamp DESC")
    List<HealthData> findSleepDataByPatientId(@Param("patientId") String patientId);

    long countByPatientId(String patientId);
}
