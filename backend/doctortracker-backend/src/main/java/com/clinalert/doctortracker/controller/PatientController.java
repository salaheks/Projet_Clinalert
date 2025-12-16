package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable String id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Patient> getPatientsByDoctor(@PathVariable String doctorId) {
        return patientService.getPatientsByDoctorId(doctorId);
    }

    @GetMapping("/clinic/{clinicId}")
    public List<Patient> getPatientsByClinic(@PathVariable String clinicId) {
        return patientService.getPatientsByClinicId(clinicId);
    }

    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable String id, @RequestBody Patient patient) {
        try {
            patient.setId(id);
            Patient updatedPatient = patientService.createPatient(patient); // save() works for update too
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Patient> updatePatientStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Patient updatedPatient = patientService.updatePatientStatus(id, status);
            return ResponseEntity.ok(updatedPatient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
