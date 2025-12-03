package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Patient> getPatientById(String id) {
        return patientRepository.findById(id);
    }

    public List<Patient> getPatientsByDoctorId(String doctorId) {
        return patientRepository.findByDoctorId(doctorId);
    }

    @SuppressWarnings("null")
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @SuppressWarnings("null")
    public void deletePatient(String id) {
        patientRepository.deleteById(id);
    }
}
