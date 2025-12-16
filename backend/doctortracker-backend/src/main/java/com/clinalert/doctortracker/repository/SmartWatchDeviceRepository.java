package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.SmartWatchDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmartWatchDeviceRepository extends JpaRepository<SmartWatchDevice, String> {

    List<SmartWatchDevice> findByPatientId(String patientId);

    List<SmartWatchDevice> findByPatientIdAndIsActiveTrue(String patientId);

    Optional<SmartWatchDevice> findByDeviceAddress(String deviceAddress);

    Optional<SmartWatchDevice> findByPatientIdAndDeviceAddress(String patientId, String deviceAddress);

    boolean existsByDeviceAddress(String deviceAddress);
}
