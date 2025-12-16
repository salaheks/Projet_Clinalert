package com.clinalert.doctortracker.repository;

import com.clinalert.doctortracker.model.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, String> {
    List<Clinic> findByDoctorId(String doctorId);
}
