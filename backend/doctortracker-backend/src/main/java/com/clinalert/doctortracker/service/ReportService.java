package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.repository.AlertRepository;
import com.clinalert.doctortracker.repository.PatientRepository;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PatientRepository patientRepository;

    private final MeasurementRepository measurementRepository;

    private final AlertRepository alertRepository;

    public byte[] generatePatientReport(String patientId) {
        Objects.requireNonNull(patientId, "Patient ID cannot be null");
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("ClinAlert - Patient Medical Report")
                    .setFontSize(18).setBold());
            document.add(new Paragraph("Generated on: " + LocalDateTime.now()));
            document.add(new Paragraph("Patient: " + patient.getName()));
            document.add(new Paragraph("Age: " + patient.getAge()));

            // Vital Signs Section
            document.add(new Paragraph("\nRecent Vital Signs").setFontSize(14).setBold());
            Table vitalTable = new Table(UnitValue.createPercentArray(new float[] { 3, 3, 3, 4 }));
            vitalTable.setWidth(UnitValue.createPercentValue(100));
            vitalTable.addHeaderCell("Type");
            vitalTable.addHeaderCell("Value");
            vitalTable.addHeaderCell("Unit"); // Unit is not in Measurement model, removing or hardcoding
            vitalTable.addHeaderCell("Time");

            List<Measurement> measurements = measurementRepository.findTop20ByPatientIdOrderByTimestampDesc(patientId);
            for (Measurement m : measurements) {
                vitalTable.addCell(m.getType());
                vitalTable.addCell(String.valueOf(m.getValue()));
                vitalTable.addCell("-"); // Unit not available in Measurement model
                vitalTable.addCell(m.getTimestamp().toString());
            }
            document.add(vitalTable);

            // Alerts Section
            document.add(new Paragraph("\nRecent Alerts").setFontSize(14).setBold());
            Table alertTable = new Table(UnitValue.createPercentArray(new float[] { 3, 5, 4 }));
            alertTable.setWidth(UnitValue.createPercentValue(100));
            alertTable.addHeaderCell("Severity");
            alertTable.addHeaderCell("Message");
            alertTable.addHeaderCell("Time");

            List<Alert> alerts = alertRepository.findTop10ByPatientIdOrderByTimestampDesc(patientId);
            for (Alert a : alerts) {
                alertTable.addCell(a.getSeverity());
                alertTable.addCell(a.getMessage());
                alertTable.addCell(a.getTimestamp().toString());
            }
            document.add(alertTable);

            // Signature Placeholder
            document.add(new Paragraph("\n\n__________________________"));
            document.add(new Paragraph("Doctor Signature"));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
        return out.toByteArray();
    }
}
