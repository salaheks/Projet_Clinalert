package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, String> {
    List<Measurement> findByPatientId(String patientId);
}
