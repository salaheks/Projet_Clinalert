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
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains(doctor1, doctor2, doctor3);

        // Vérifier que le repository a été appelé une fois
        verify(doctorRepository, times(1)).findAll();
    }

    /**
     * TEST 2 : Récupérer tous les docteurs - Liste vide
     * 
     * SCÉNARIO :
     * - La base ne contient aucun docteur
     * 
     * RÉSULTAT ATTENDU :
     * - Retourne une liste vide (pas null)
     */
    @Test
    @DisplayName("getAllDoctors - Doit retourner une liste vide si aucun docteur")
    void getAllDoctors_WhenNoDoctors_ShouldReturnEmptyList() {
        // ===== ARRANGE =====
        when(doctorRepository.findAll()).thenReturn(Arrays.asList());

        // ===== ACT =====
        List<Doctor> result = doctorService.getAllDoctors();

        // ===== ASSERT =====
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(doctorRepository, times(1)).findAll();
    }

    // ==========================================
    // SECTION 4 : TESTS - RÉCUPÉRER PAR ID
    // ==========================================

    /**
     * TEST 3 : Récupérer un docteur par ID - Docteur trouvé
     * 
     * SCÉNARIO :
     * - Recherche d'un docteur existant
     * 
     * RÉSULTAT ATTENDU :
     * - Retourne un Optional contenant le docteur
     */
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

    /**
     * TEST 4 : Récupérer un docteur par ID - Docteur non trouvé
     * 
     * SCÉNARIO :
     * - Recherche d'un docteur inexistant
     * 
     * RÉSULTAT ATTENDU :
     * - Retourne un Optional.empty()
     */
    @Test
    @DisplayName("getDoctorById - Doit retourner Optional.empty() si docteur non trouvé")
    void getDoctorById_WhenDoctorNotFound_ShouldReturnEmpty() {
        // ===== ARRANGE =====
        String doctorId = "doctor-999";
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // ===== ACT =====
        Optional<Doctor> result = doctorService.getDoctorById(doctorId);

        // ===== ASSERT =====
        assertThat(result).isEmpty();
        verify(doctorRepository, times(1)).findById(doctorId);
    }

    // ==========================================
    // SECTION 5 : TESTS - CRÉER UN DOCTEUR
    // ==========================================

    /**
     * TEST 5 : Créer un nouveau docteur - Données valides
     * 
     * SCÉNARIO :
     * - Création d'un nouveau docteur avec toutes les informations
     * 
     * RÉSULTAT ATTENDU :
     * - Le docteur est sauvegardé avec un ID généré
     * - Retourne le docteur créé
     */
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
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("doctor-004");
        assertThat(result.getName()).isEqualTo("Dr. Sophie Dubois");
        assertThat(result.getSpecialty()).isEqualTo("Pédiatrie");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    // ==========================================
    // SECTION 6 : TESTS - METTRE À JOUR UN DOCTEUR
    // ==========================================

    /**
     * TEST 6 : Mettre à jour un docteur existant - Succès
     * 
     * SCÉNARIO :
     * - Modification des informations d'un docteur existant
     * 
     * RÉSULTAT ATTENDU :
     * - Les informations sont mises à jour
     * - Retourne le docteur mis à jour
     */
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
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Dr. Jean Dupont (Modifié)");
        assertThat(result.getSpecialty()).isEqualTo("Cardiologie Interventionnelle");
        assertThat(result.getEmail()).isEqualTo("j.dupont@clinalert.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+33 6 99 88 77 66");

        verify(doctorRepository, times(1)).findById(doctorId);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    /**
     * TEST 7 : Mettre à jour un docteur inexistant - Erreur
     * 
     * SCÉNARIO :
     * - Tentative de modification d'un docteur non existant
     * 
     * RÉSULTAT ATTENDU :
     * - Lance une RuntimeException
     */
    @Test
    @DisplayName("updateDoctor - Doit lancer une exception si docteur non trouvé")
    void updateDoctor_WhenDoctorNotFound_ShouldThrowException() {
        // ===== ARRANGE =====
        String doctorId = "doctor-999";
        Doctor updatedDetails = new Doctor();
        updatedDetails.setName("Test");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        assertThatThrownBy(() -> doctorService.updateDoctor(doctorId, updatedDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Doctor not found with id: " + doctorId);

        verify(doctorRepository, times(1)).findById(doctorId);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    // ==========================================
    // SECTION 7 : TESTS - SUPPRIMER UN DOCTEUR
    // ==========================================

    /**
     * TEST 8 : Supprimer un docteur - Succès
     * 
     * SCÉNARIO :
     * - Suppression d'un docteur existant
     * 
     * RÉSULTAT ATTENDU :
     * - La méthode deleteById est appelée
     * - Aucune exception n'est levée
     */
    @Test
    @DisplayName("deleteDoctor - Doit supprimer le docteur")
    void deleteDoctor_ShouldCallRepositoryDelete() {
        // ===== ARRANGE =====
        String doctorId = "doctor-001";
        doNothing().when(doctorRepository).deleteById(doctorId);

        // ===== ACT =====
        doctorService.deleteDoctor(doctorId);

        // ===== ASSERT =====
        verify(doctorRepository, times(1)).deleteById(doctorId);
    }

    /**
     * TEST 9 : Supprimer un docteur - Vérifie qu'aucune erreur pour ID inexistant
     * 
     * SCÉNARIO :
     * - Tentative de suppression d'un docteur inexistant
     * 
     * RÉSULTAT ATTENDU :
     * - La méthode ne lève pas d'exception (comportement JPA standard)
     */
    @Test
    @DisplayName("deleteDoctor - Ne doit pas lever d'exception si docteur inexistant")
    void deleteDoctor_WithNonExistentId_ShouldNotThrowException() {
        // ===== ARRANGE =====
        String doctorId = "doctor-999";
        doNothing().when(doctorRepository).deleteById(doctorId);

        // ===== ACT & ASSERT =====
        assertThatCode(() -> doctorService.deleteDoctor(doctorId))
                .doesNotThrowAnyException();

        verify(doctorRepository, times(1)).deleteById(doctorId);
    }

    // ==========================================
    // SECTION 8 : TESTS - CAS LIMITES
    // ==========================================

    /**
     * TEST 10 : Créer un docteur avec données minimales
     * 
     * SCÉNARIO :
     * - Création avec seulement le nom (champs optionnels vides)
     * 
     * RÉSULTAT ATTENDU :
     * - Le docteur est créé malgré les champs manquants
     */
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
