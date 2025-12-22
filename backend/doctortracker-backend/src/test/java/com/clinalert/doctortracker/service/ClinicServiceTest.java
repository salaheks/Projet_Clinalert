package com.clinalert.doctortracker.service;

/**
 * Tests ClinicService - 10 tests
 * Couvre: CRUD clinics, recherche par doctor
 */

import com.clinalert.doctortracker.model.Clinic;
import com.clinalert.doctortracker.repository.ClinicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Service Clinic")
class ClinicServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicService clinicService;

    private Clinic clinic;

    @BeforeEach
    void setUp() {
        clinic = new Clinic();
        clinic.setId("clinic-001");
        clinic.setName("Centre Médical Test");
        clinic.setAddress("123 Rue Test");
        clinic.setPhone("0123456789");
        clinic.setDoctorId("doctor-001");
    }

    @Test
    @DisplayName("getAllClinics")
    void getAllClinics_ShouldReturnAll() {
        when(clinicRepository.findAll()).thenReturn(Arrays.asList(clinic));

        List<Clinic> result = clinicService.getAllClinics();

        assertThat(result).hasSize(1);
        verify(clinicRepository).findAll();
    }

    @Test
    @DisplayName("getClinicById - Existant")
    void getClinicById_WhenExists_ShouldReturn() {
        when(clinicRepository.findById("clinic-001")).thenReturn(Optional.of(clinic));

        Optional<Clinic> result = clinicService.getClinicById("clinic-001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Centre Médical Test");
    }

    @Test
    @DisplayName("getClinicById - Non existant")
    void getClinicById_NotFound_ShouldReturnEmpty() {
        when(clinicRepository.findById("clinic-999")).thenReturn(Optional.empty());

        Optional<Clinic> result = clinicService.getClinicById("clinic-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getClinicsByDoctorId")
    void getClinicsByDoctorId_ShouldReturnDoctorClinics() {
        when(clinicRepository.findByDoctorId("doctor-001")).thenReturn(Arrays.asList(clinic));

        List<Clinic> result = clinicService.getClinicsByDoctorId("doctor-001");

        assertThat(result).hasSize(1);
        verify(clinicRepository).findByDoctorId("doctor-001");
    }

    @Test
    @DisplayName("createClinic")
    void createClinic_ShouldSave() {
        when(clinicRepository.save(any())).thenReturn(clinic);

        Clinic result = clinicService.createClinic(clinic);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("clinic-001");
        verify(clinicRepository).save(clinic);
    }

    @Test
    @DisplayName("updateClinic - Existant")
    void updateClinic_WhenExists_ShouldUpdate() {
        Clinic updated = new Clinic();
        updated.setName("Nouveau Nom");
        updated.setAddress("Nouvelle Adresse");
        updated.setPhone("0987654321");
        updated.setDoctorId("doctor-002");

        when(clinicRepository.findById("clinic-001")).thenReturn(Optional.of(clinic));
        when(clinicRepository.save(any())).thenReturn(clinic);

        Clinic result = clinicService.updateClinic("clinic-001", updated);

        assertThat(result).isNotNull();
        verify(clinicRepository).save(any());
    }

    @Test
    @DisplayName("updateClinic - Non existant")
    void updateClinic_NotFound_ShouldThrow() {
        when(clinicRepository.findById("clinic-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicService.updateClinic("clinic-999", clinic))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Clinic not found");
    }

    @Test
    @DisplayName("deleteClinic")
    void deleteClinic_ShouldCallDelete() {
        clinicService.deleteClinic("clinic-001");

        verify(clinicRepository).deleteById("clinic-001");
    }

    @Test
    @DisplayName("getClinicsByDoctorId - Aucune clinic")
    void getClinicsByDoctorId_WhenNone_ShouldReturnEmpty() {
        when(clinicRepository.findByDoctorId("doctor-999")).thenReturn(Arrays.asList());

        List<Clinic> result = clinicService.getClinicsByDoctorId("doctor-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllClinics - Liste vide")
    void getAllClinics_WhenEmpty_ShouldReturnEmpty() {
        when(clinicRepository.findAll()).thenReturn(Arrays.asList());

        List<Clinic> result = clinicService.getAllClinics();

        assertThat(result).isEmpty();
    }
}
