package com.clinalert.doctortracker.service;

import jakarta.persistence.EntityNotFoundException;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

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

    public List<Patient> getPatientsByClinicId(String clinicId) {
        return patientRepository.findByClinicId(clinicId);
    }

    @SuppressWarnings("null")
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @SuppressWarnings("null")
    public void deletePatient(String id) {
        patientRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    public Patient updatePatientStatus(String id, String status) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            patient.setStatus(status);
            return patientRepository.save(patient);
        }
        throw new EntityNotFoundException("Patient not found with id: " + id);
    }
}
