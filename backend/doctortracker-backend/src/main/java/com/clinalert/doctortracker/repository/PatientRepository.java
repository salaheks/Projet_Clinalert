package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    List<Patient> findByDoctorId(String doctorId);
}
