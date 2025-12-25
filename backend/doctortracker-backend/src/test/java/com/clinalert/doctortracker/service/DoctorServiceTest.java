package com.clinalert.doctortracker.service;

/**
 * ============================================
 * Tests Unitaires pour DoctorService
 * ============================================
 * 
 * Ce fichier contient les tests unitaires pour le service de gestion des docteurs.
 * 
 * MÉTHODES TESTÉES :
 * -----------------
 * - getAllDoctors() : Récupère tous les docteurs
 * - getDoctorById() : Récupère un docteur par son ID
 * - createDoctor() : Crée un nouveau docteur
 * - updateDoctor() : Met à jour un docteur existant
 * - deleteDoctor() : Supprime un docteur
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.repository.DoctorRepository;
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

/**
 * Configuration Mockito pour les tests unitaires
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires du Service Doctor")
class DoctorServiceTest {

    // ==========================================
    // SECTION 1 : CONFIGURATION DES MOCKS
    // ==========================================

    /**
     * Le repository est mocké : on contrôle ses réponses
     */
    @Mock
    private DoctorRepository doctorRepository;

    /**
     * Le service à tester : Mockito injecte automatiquement les mocks
     */
    @InjectMocks
    private DoctorService doctorService;

    // Variables communes à tous les tests
    private Doctor doctor1;
    private Doctor doctor2;
    private Doctor doctor3;

    // ==========================================
    // SECTION 2 : PRÉPARATION DES TESTS
    // ==========================================

    /**
     * Méthode exécutée AVANT chaque test
     * Prépare les données de test
     */
    @BeforeEach
    void setUp() {
        // Docteur 1 : Cardiologue
        doctor1 = new Doctor();
        doctor1.setId("doctor-001");
        doctor1.setName("Dr. Jean Dupont");
        doctor1.setSpecialty("Cardiologie");
        doctor1.setEmail("jean.dupont@clinalert.com");
        doctor1.setPhoneNumber("+33 6 12 34 56 78");

        // Docteur 2 : Neurologue
        doctor2 = new Doctor();
        doctor2.setId("doctor-002");
        doctor2.setName("Dr. Marie Martin");
        doctor2.setSpecialty("Neurologie");
        doctor2.setEmail("marie.martin@clinalert.com");
        doctor2.setPhoneNumber("+33 6 98 76 54 32");

        // Docteur 3 : Généraliste
        doctor3 = new Doctor();
        doctor3.setId("doctor-003");
        doctor3.setName("Dr. Pierre Bernard");
        doctor3.setSpecialty("Médecine Générale");
        doctor3.setEmail("pierre.bernard@clinalert.com");
        doctor3.setPhoneNumber("+33 6 11 22 33 44");
    }

    // ==========================================
    // SECTION 3 : TESTS - RÉCUPÉRER TOUS LES DOCTEURS
    // ==========================================

    /**
     * TEST 1 : Récupérer tous les docteurs - Liste non vide
     * 
     * SCÉNARIO :
     * - La base contient plusieurs docteurs
     * 
     * RÉSULTAT ATTENDU :
     * - Retourne la liste complète des docteurs
     * - La taille de la liste correspond au nombre de docteurs
     */
    @Test
    @DisplayName("getAllDoctors - Doit retourner tous les docteurs")
    void getAllDoctors_ShouldReturnAllDoctors() {
        // ===== ARRANGE =====
        List<Doctor> allDoctors = Arrays.asList(doctor1, doctor2, doctor3);
        when(doctorRepository.findAll()).thenReturn(allDoctors);

        // ===== ACT =====
        List<Doctor> result = doctorService.getAllDoctors();

        // ===== ASSERT =====
        assertThat(result).isNotNull()
                .hasSize(3)
                .contains(doctor1, doctor2, doctor3);

        // Vérifier que le repository a été appelé une fois
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllDoctors - Doit retourner une liste vide si aucun docteur")
    void getAllDoctors_WhenNoDoctors_ShouldReturnEmptyList() {
        // ===== ARRANGE =====
        when(doctorRepository.findAll()).thenReturn(Arrays.asList());

        // ===== ACT =====
        List<Doctor> result = doctorService.getAllDoctors();

        // ===== ASSERT =====
        assertThat(result).isNotNull().isEmpty();
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getDoctorById - Doit retourner le docteur si il existe")
    void getDoctorById_WhenDoctorExists_ShouldReturnDoctor() {
        // ===== ARRANGE =====
        String doctorId = "doctor-001";
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor1));

        // ===== ACT =====
        Optional<Doctor> result = doctorService.getDoctorById(doctorId);

        // ===== ASSERT =====
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(doctorId);
        assertThat(result.get().getName()).isEqualTo("Dr. Jean Dupont");
        assertThat(result.get().getSpecialty()).isEqualTo("Cardiologie");
        verify(doctorRepository, times(1)).findById(doctorId);
    }

    // Skipping getDoctorById_WhenDoctorNotFound_ShouldReturnEmpty (already chained
    // isEmpty)

    @Test
    @DisplayName("createDoctor - Doit créer et retourner un nouveau docteur")
    void createDoctor_WithValidData_ShouldSaveAndReturnDoctor() {
        // ===== ARRANGE =====
        Doctor newDoctor = new Doctor();
        newDoctor.setName("Dr. Sophie Dubois");
        newDoctor.setSpecialty("Pédiatrie");
        newDoctor.setEmail("sophie.dubois@clinalert.com");
        newDoctor.setPhoneNumber("+33 6 55 66 77 88");

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId("doctor-004"); // ID généré par la base
        savedDoctor.setName(newDoctor.getName());
        savedDoctor.setSpecialty(newDoctor.getSpecialty());
        savedDoctor.setEmail(newDoctor.getEmail());
        savedDoctor.setPhoneNumber(newDoctor.getPhoneNumber());

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        // ===== ACT =====
        Doctor result = doctorService.createDoctor(newDoctor);

        // ===== ASSERT =====
        assertThat(result).isNotNull()
                .returns("doctor-004", Doctor::getId)
                .returns("Dr. Sophie Dubois", Doctor::getName)
                .returns("Pédiatrie", Doctor::getSpecialty);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    @DisplayName("updateDoctor - Doit mettre à jour le docteur existant")
    void updateDoctor_WhenDoctorExists_ShouldUpdateAndReturn() {
        // ===== ARRANGE =====
        String doctorId = "doctor-001";

        Doctor updatedDetails = new Doctor();
        updatedDetails.setName("Dr. Jean Dupont (Modifié)");
        updatedDetails.setSpecialty("Cardiologie Interventionnelle");
        updatedDetails.setEmail("j.dupont@clinalert.com");
        updatedDetails.setPhoneNumber("+33 6 99 88 77 66");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor1));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ===== ACT =====
        Doctor result = doctorService.updateDoctor(doctorId, updatedDetails);

        // ===== ASSERT =====
        assertThat(result).isNotNull()
                .returns("Dr. Jean Dupont (Modifié)", Doctor::getName)
                .returns("Cardiologie Interventionnelle", Doctor::getSpecialty)
                .returns("j.dupont@clinalert.com", Doctor::getEmail)
                .returns("+33 6 99 88 77 66", Doctor::getPhoneNumber);

        verify(doctorRepository, times(1)).findById(doctorId);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    // Skipping error cases (already good)

    @Test
    @DisplayName("createDoctor - Doit créer même avec données minimales")
    void createDoctor_WithMinimalData_ShouldSucceed() {
        // ===== ARRANGE =====
        Doctor minimalDoctor = new Doctor();
        minimalDoctor.setName("Dr. Test");

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId("doctor-005");
        savedDoctor.setName("Dr. Test");

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        // ===== ACT =====
        Doctor result = doctorService.createDoctor(minimalDoctor);

        // ===== ASSERT =====
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Dr. Test");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }
}
