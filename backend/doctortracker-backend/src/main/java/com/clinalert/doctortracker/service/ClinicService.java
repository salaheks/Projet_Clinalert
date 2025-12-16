package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Clinic;
import com.clinalert.doctortracker.repository.ClinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Clinic> getClinicById(String id) {
        return clinicRepository.findById(id);
    }

    public List<Clinic> getClinicsByDoctorId(String doctorId) {
        return clinicRepository.findByDoctorId(doctorId);
    }

    @SuppressWarnings("null")
    public Clinic createClinic(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    @SuppressWarnings("null")
    public Clinic updateClinic(String id, Clinic clinicDetails) {
        return clinicRepository.findById(id)
                .map(clinic -> {
                    clinic.setName(clinicDetails.getName());
                    clinic.setAddress(clinicDetails.getAddress());
                    clinic.setPhone(clinicDetails.getPhone());
                    clinic.setDoctorId(clinicDetails.getDoctorId());
                    return clinicRepository.save(clinic);
                })
                .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + id));
    }

    @SuppressWarnings("null")
    public void deleteClinic(String id) {
        clinicRepository.deleteById(id);
    }
}
