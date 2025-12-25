package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<byte[]> downloadPatientReport(@PathVariable String patientId) {
        try {
            byte[] pdfBytes = reportService.generatePatientReport(patientId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=patient_report_" + patientId + ".pdf")
                    .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_PDF))
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
