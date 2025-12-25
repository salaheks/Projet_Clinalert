package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.Clinic;
import com.clinalert.doctortracker.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping
    public List<Clinic> getAllClinics() {
        return clinicService.getAllClinics();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable String id) {
        return clinicService.getClinicById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Clinic> getClinicsByDoctorId(@PathVariable String doctorId) {
        return clinicService.getClinicsByDoctorId(doctorId);
    }

    @PostMapping
    public Clinic createClinic(@RequestBody Clinic clinic) {
        return clinicService.createClinic(clinic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clinic> updateClinic(@PathVariable String id, @RequestBody Clinic clinic) {
        try {
            Clinic updatedClinic = clinicService.updateClinic(id, clinic);
            return ResponseEntity.ok(updatedClinic);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable String id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }
}
