package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Doctor> getDoctorById(String id) {
        return doctorRepository.findById(id);
    }

    @SuppressWarnings("null")
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @SuppressWarnings("null")
    public Doctor updateDoctor(String id, Doctor doctorDetails) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setName(doctorDetails.getName());
                    doctor.setSpecialty(doctorDetails.getSpecialty());
                    doctor.setEmail(doctorDetails.getEmail());
                    doctor.setPhoneNumber(doctorDetails.getPhoneNumber());
                    return doctorRepository.save(doctor);
                })
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    @SuppressWarnings("null")
    public void deleteDoctor(String id) {
        doctorRepository.deleteById(id);
    }
}
