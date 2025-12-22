package com.clinalert.doctortracker.service;

/**
 * ============================================
 * Tests Unitaires pour PatientService
 * ============================================
 * 
 * Ce fichier teste la logique métier du service Patient.
 * 
 * STRATÉGIE DE TEST :
 * -------------------
 * - On utilise des mocks pour isoler le service de la base de données
 * - On teste chaque méthode du service individuellement
 * - On vérifie les cas normaux ET les cas d'erreur
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.repository.PatientRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du Service Patient")
class PatientServiceTest {

    // ==========================================
    // SECTION 1 : MOCKS ET INJECTION
    // ==========================================

    /**
     * Mock du repository - simule les appels à la base de données
     */
    @Mock
    private PatientRepository patientRepository;

    /**
     * Le service à tester - Mockito injectera le mock ci-dessus
     */
    @InjectMocks
    private PatientService patientService;

    // Données de test
    private Patient patient1;
    private Patient patient2;
    private List<Patient> patientList;

    // ==========================================
    // SECTION 2 : PRÉPARATION
    // ==========================================

    @BeforeEach
    void setUp() {
        // Patient 1
        patient1 = new Patient();
        patient1.setId("p1");
        patient1.setName("Jean Dupont");
        patient1.setAge(45);
        patient1.setGender("M");
        patient1.setDoctorId("d1");
        patient1.setClinicId("c1");
        patient1.setStatus("active");

        // Patient 2
        patient2 = new Patient();
        patient2.setId("p2");
        patient2.setName("Marie Martin");
        patient2.setAge(32);
        patient2.setGender("F");
        patient2.setDoctorId("d1");
        patient2.setClinicId("c2");
        patient2.setStatus("active");

        patientList = Arrays.asList(patient1, patient2);
    }

    // ==========================================
    // SECTION 3 : TESTS DE RÉCUPÉRATION
    // ==========================================

    /**
     * TEST 1 : Récupérer tous les patients
     */
    @Test
    @DisplayName("getAllPatients() : Doit retourner tous les patients")
    void getAllPatients_ShouldReturnAllPatients() {
        // ===== ARRANGE =====
        when(patientRepository.findAll()).thenReturn(patientList);

        // ===== ACT =====
        List<Patient> result = patientService.getAllPatients();

        // ===== ASSERT =====
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Jean Dupont", result.get(0).getName());
        assertEquals("Marie Martin", result.get(1).getName());

        verify(patientRepository, times(1)).findAll();
    }

    /**
     * TEST 2 : Récupérer un patient par ID (trouvé)
     */
    @Test
    @DisplayName("getPatientById() : Doit retourner le patient si trouvé")
    void getPatientById_WhenExists_ShouldReturnPatient() {
        // ===== ARRANGE =====
        when(patientRepository.findById("p1")).thenReturn(Optional.of(patient1));

        // ===== ACT =====
        Optional<Patient> result = patientService.getPatientById("p1");

        // ===== ASSERT =====
        assertTrue(result.isPresent());
        assertEquals("Jean Dupont", result.get().getName());
        verify(patientRepository, times(1)).findById("p1");
    }

    /**
     * TEST 3 : Récupérer un patient par ID (non trouvé)
     */
    @Test
    @DisplayName("getPatientById() : Doit retourner Optional.empty() si non trouvé")
    void getPatientById_WhenNotExists_ShouldReturnEmpty() {
        // ===== ARRANGE =====
        when(patientRepository.findById(anyString())).thenReturn(Optional.empty());

        // ===== ACT =====
        Optional<Patient> result = patientService.getPatientById("p999");

        // ===== ASSERT =====
        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findById("p999");
    }

    /**
     * TEST 4 : Récupérer les patients d'un docteur
     */
    @Test
    @DisplayName("getPatientsByDoctorId() : Doit retourner les patients du docteur")
    void getPatientsByDoctorId_ShouldReturnDoctorPatients() {
        // ===== ARRANGE =====
        when(patientRepository.findByDoctorId("d1")).thenReturn(patientList);

        // ===== ACT =====
        List<Patient> result = patientService.getPatientsByDoctorId("d1");

        // ===== ASSERT =====
        assertNotNull(result);
        assertEquals(2, result.size());
        // Vérifier que tous les patients ont le même doctorId
        assertTrue(result.stream().allMatch(p -> p.getDoctorId().equals("d1")));

        verify(patientRepository, times(1)).findByDoctorId("d1");
    }

    /**
     * TEST 5 : Récupérer les patients d'une clinique
     */
    @Test
    @DisplayName("getPatientsByClinicId() : Doit retourner les patients de la clinique")
    void getPatientsByClinicId_ShouldReturnClinicPatients() {
        // ===== ARRANGE =====
        List<Patient> clinicPatients = Arrays.asList(patient1);
        when(patientRepository.findByClinicId("c1")).thenReturn(clinicPatients);

        // ===== ACT =====
        List<Patient> result = patientService.getPatientsByClinicId("c1");

        // ===== ASSERT =====
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("c1", result.get(0).getClinicId());

        verify(patientRepository, times(1)).findByClinicId("c1");
    }

    // ==========================================
    // SECTION 4 : TESTS DE CRÉATION
    // ==========================================

    /**
     * TEST 6 : Créer un nouveau patient
     */
    @Test
    @DisplayName("createPatient() : Doit sauvegarder et retourner le patient")
    void createPatient_ShouldSaveAndReturnPatient() {
        // ===== ARRANGE =====
        Patient newPatient = new Patient();
        newPatient.setName("Nouveau Patient");
        newPatient.setAge(25);

        Patient savedPatient = new Patient();
        savedPatient.setId("p3");
        savedPatient.setName("Nouveau Patient");
        savedPatient.setAge(25);

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // ===== ACT =====
        Patient result = patientService.createPatient(newPatient);

        // ===== ASSERT =====
        assertNotNull(result);
        assertEquals("p3", result.getId());
        assertEquals("Nouveau Patient", result.getName());

        verify(patientRepository, times(1)).save(newPatient);
    }

    // ==========================================
    // SECTION 5 : TESTS DE SUPPRESSION
    // ==========================================

    /**
     * TEST 7 : Supprimer un patient
     */
    @Test
    @DisplayName("deletePatient() : Doit appeler le repository pour supprimer")
    void deletePatient_ShouldCallRepositoryDelete() {
        // ===== ARRANGE =====
        String patientId = "p1";
        doNothing().when(patientRepository).deleteById(patientId);

        // ===== ACT =====
        patientService.deletePatient(patientId);

        // ===== ASSERT =====
        verify(patientRepository, times(1)).deleteById(patientId);
    }

    // ==========================================
    // SECTION 6 : TESTS DE MISE À JOUR DU STATUT
    // ==========================================

    /**
     * TEST 8 : Mettre à jour le statut d'un patient (succès)
     */
    @Test
    @DisplayName("updatePatientStatus() : Doit mettre à jour et sauvegarder le statut")
    void updatePatientStatus_WhenPatientExists_ShouldUpdateStatus() {
        // ===== ARRANGE =====
        String patientId = "p1";
        String newStatus = "inactive";

        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);
        existingPatient.setName("Jean Dupont");
        existingPatient.setStatus("active");

        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);
        updatedPatient.setName("Jean Dupont");
        updatedPatient.setStatus(newStatus);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        // ===== ACT =====
        Patient result = patientService.updatePatientStatus(patientId, newStatus);

        // ===== ASSERT =====
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());

        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    /**
     * TEST 9 : Mettre à jour le statut d'un patient inexistant (erreur)
     */
    @Test
    @DisplayName("updatePatientStatus() : Doit lever une exception si patient non trouvé")
    void updatePatientStatus_WhenPatientNotExists_ShouldThrowException() {
        // ===== ARRANGE =====
        String patientId = "p999";
        String newStatus = "inactive";

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientStatus(patientId, newStatus);
        });

        assertEquals("Patient not found with id: p999", exception.getMessage());

        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    // ==========================================
    // SECTION 7 : TESTS DE CAS LIMITES
    // ==========================================

    /**
     * TEST 10 : Récupérer une liste vide de patients
     */
    @Test
    @DisplayName("getAllPatients() : Doit retourner une liste vide si aucun patient")
    void getAllPatients_WhenNoPatients_ShouldReturnEmptyList() {
        // ===== ARRANGE =====
        when(patientRepository.findAll()).thenReturn(Arrays.asList());

        // ===== ACT =====
        List<Patient> result = patientService.getAllPatients();

        // ===== ASSERT =====
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }
}
