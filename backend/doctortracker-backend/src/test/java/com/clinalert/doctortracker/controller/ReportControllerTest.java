package com.clinalert.doctortracker.controller;

/**
 * Tests ReportController - Integration - 5 tests
 * Couvre: PDF generation, error handling
 */

import com.clinalert.doctortracker.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests ReportController - Integration")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/reports/patient/{patientId} - Should generate PDF")
    void downloadPatientReport_ValidPatientId_ShouldGeneratePDF() throws Exception {
        byte[] pdfBytes = "PDF_CONTENT".getBytes();
        when(reportService.generatePatientReport("patient-001")).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/reports/patient/patient-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=patient_report_patient-001.pdf"));

        verify(reportService).generatePatientReport("patient-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/reports/patient/{patientId} - Patient not found")
    void downloadPatientReport_InvalidPatientId_ShouldReturnError() throws Exception {
        when(reportService.generatePatientReport(anyString()))
                .thenThrow(new RuntimeException("Patient not found"));

        mockMvc.perform(get("/api/reports/patient/invalid-id"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/reports/patient/{patientId} - Service exception")
    void downloadPatientReport_ServiceException_ShouldHandleGracefully() throws Exception {
        when(reportService.generatePatientReport(anyString()))
                .thenThrow(new RuntimeException("PDF generation failed"));

        mockMvc.perform(get("/api/reports/patient/patient-001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/reports/patient/{patientId} - Empty PDF")
    void downloadPatientReport_EmptyData_ShouldReturnEmptyPDF() throws Exception {
        byte[] emptyPdf = new byte[0];
        when(reportService.generatePatientReport("patient-002")).thenReturn(emptyPdf);

        mockMvc.perform(get("/api/reports/patient/patient-002"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyPdf));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/reports/patient/{patientId} - Large PDF")
    void downloadPatientReport_LargePDF_ShouldSucceed() throws Exception {
        byte[] largePdf = new byte[1024 * 100]; // 100KB
        when(reportService.generatePatientReport("patient-003")).thenReturn(largePdf);

        mockMvc.perform(get("/api/reports/patient/patient-003"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
