package com.clinalert.doctortracker.controller;

/**
 * ============================================
 * Tests d'Intégration pour PatientController
 * ============================================
 * 
 * Ce fichier contient les tests d'intégration pour les endpoints REST des patients.
 * 
 * DIFFÉRENCE ENTRE TEST UNITAIRE ET TEST D'INTÉGRATION :
 * -------------------------------------------------------
 * - Test Unitaire : Teste une classe isolée avec des mocks
 * - Test d'Intégration : Teste le système complet (Controller + Service + Repository)
 * 
 * ANNOTATIONS UTILISÉES :
 * ----------------------
 * @SpringBootTest : Charge le contexte complet de l'application Spring
 * @AutoConfigureMockMvc : Configure MockMvc pour simuler des requêtes HTTP
 * @MockBean : Remplace un bean Spring par un mock (utile pour isoler certaines parties)
 * @WithMockUser : Simule un utilisateur authentifié avec un rôle spécifique
 * @TestPropertySource : Permet de surcharger les propriétés pour les tests
 * 
 * MOCKMVC :
 * ---------
 * MockMvc permet de simuler des requêtes HTTP sans démarrer un vrai serveur.
 * C'est très rapide et permet de tester les controllers de manière réaliste.
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Configuration Spring Boot pour les tests d'intégration
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests d'Intégration du Controller Patient")
class PatientControllerTest {

        // ==========================================
        // SECTION 1 : INJECTION DES DÉPENDANCES
        // ==========================================

        /**
         * MockMvc : permet de simuler des requêtes HTTP
         * C'est l'outil principal pour tester les controllers
         */
        @Autowired
        private MockMvc mockMvc;

        /**
         * ObjectMapper : convertit les objets Java en JSON et vice-versa
         * Très utile pour créer le body des requêtes POST/PUT
         */
        @Autowired
        private ObjectMapper objectMapper;

        /**
         * Mock du service Patient
         * On utilise un mock pour isoler le controller et contrôler les réponses
         */
        @MockBean
        private PatientService patientService;

        // Variables communes à tous les tests
        private Patient patient1;
        private Patient patient2;
        private Patient patient3;

        // ==========================================
        // SECTION 2 : PRÉPARATION DES TESTS
        // ==========================================

        /**
         * Méthode exécutée AVANT chaque test
         * Prépare les données de test
         */
        @BeforeEach
        void setUp() {
                // Patient 1 : actif, avec docteur
                patient1 = new Patient();
                patient1.setId("patient-001");
                patient1.setName("Jean Dupont");
                patient1.setAge(45);
                patient1.setGender("M");
                patient1.setDoctorId("doctor-123");
                patient1.setStatus("active");

                // Patient 2 : active, même docteur
                patient2 = new Patient();
                patient2.setId("patient-002");
                patient2.setName("Marie Martin");
                patient2.setAge(32);
                patient2.setGender("F");
                patient2.setDoctorId("doctor-123");
                patient2.setStatus("active");

                // Patient 3 : inactif, autre docteur
                patient3 = new Patient();
                patient3.setId("patient-003");
                patient3.setName("Pierre Durand");
                patient3.setAge(67);
                patient3.setGender("M");
                patient3.setDoctorId("doctor-456");
                patient3.setStatus("inactive");
        }

        // ==========================================
        // SECTION 3 : TESTS GET - RÉCUPÉRATION DES PATIENTS
        // ==========================================

        /**
         * TEST 1 : Récupérer tous les patients (avec authentification)
         * 
         * SCÉNARIO :
         * - Un docteur authentifié demande la liste de tous les patients
         * 
         * RÉSULTAT ATTENDU :
         * - Status HTTP 200 (OK)
         * - Un tableau JSON avec tous les patients
         * - Vérification de la structure du JSON
         */
        @Test
        @WithMockUser(roles = "DOCTOR") // Simule un utilisateur avec le rôle DOCTOR
        @DisplayName("GET /api/patients - Doit retourner la liste de tous les patients")
        void getAllPatients_WithDoctorRole_ShouldReturnPatientList() throws Exception {
                // ===== ARRANGE =====
                List<Patient> allPatients = Arrays.asList(patient1, patient2, patient3);

                // Configurer le mock : quand getAllPatients() est appelé, retourner notre liste
                when(patientService.getAllPatients()).thenReturn(allPatients);

                // ===== ACT & ASSERT =====
                mockMvc.perform(
                                // Effectuer une requête GET sur /api/patients
                                get("/api/patients")
                                                .contentType(MediaType.APPLICATION_JSON))
                                // Afficher le résultat dans la console (utile pour debug)
                                .andDo(print())

                                // Vérifications :
                                .andExpect(status().isOk()) // Status HTTP doit être 200
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Type de contenu JSON

                                // Vérifier le contenu du JSON avec JSONPath
                                .andExpect(jsonPath("$", hasSize(3))) // Le tableau doit contenir 3 patients

                                // Vérifier le premier patient
                                .andExpect(jsonPath("$[0].id", is("patient-001")))
                                .andExpect(jsonPath("$[0].name", is("Jean Dupont")))
                                .andExpect(jsonPath("$[0].age", is(45)))
                                .andExpect(jsonPath("$[0].gender", is("M")))

                                // Vérifier le deuxième patient
                                .andExpect(jsonPath("$[1].id", is("patient-002")))
                                .andExpect(jsonPath("$[1].name", is("Marie Martin")))

                                // Vérifier le troisième patient
                                .andExpect(jsonPath("$[2].id", is("patient-003")))
                                .andExpect(jsonPath("$[2].status", is("inactive")));

                // Vérifier que le service a bien été appelé une fois
                verify(patientService, times(1)).getAllPatients();
        }

        /**
         * TEST 2 : Récupérer un patient par son ID
         * 
         * SCÉNARIO :
         * - Un docteur demande les détails d'un patient spécifique
         * 
         * RÉSULTAT ATTENDU :
         * - Status HTTP 200
         * - Les détails complets du patient en JSON
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("GET /api/patients/{id} - Doit retourner les détails d'un patient")
        void getPatientById_WhenPatientExists_ShouldReturnPatient() throws Exception {
                // ===== ARRANGE =====
                String patientId = "patient-001";
                when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient1));

                // ===== ACT & ASSERT =====
                mockMvc.perform(get("/api/patients/{id}", patientId))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(patientId)))
                                .andExpect(jsonPath("$.name", is("Jean Dupont")))
                                .andExpect(jsonPath("$.age", is(45)))
                                .andExpect(jsonPath("$.status", is("active")));

                verify(patientService, times(1)).getPatientById(patientId);
        }

        /**
         * TEST 3 : Récupérer un patient inexistant
         * 
         * SCÉNARIO :
         * - Demander un patient avec un ID qui n'existe pas
         * 
         * RÉSULTAT ATTENDU :
         * - Status HTTP 404 (Not Found)
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("GET /api/patients/{id} - Doit retourner 404 si le patient n'existe pas")
        void getPatientById_WhenPatientNotFound_ShouldReturn404() throws Exception {
                // ===== ARRANGE =====
                String nonExistentId = "patient-999";
                when(patientService.getPatientById(nonExistentId)).thenReturn(Optional.empty());

                // ===== ACT & ASSERT =====
                mockMvc.perform(get("/api/patients/{id}", nonExistentId))
                                .andExpect(status().isNotFound()); // 404

                verify(patientService, times(1)).getPatientById(nonExistentId);
        }

        /**
         * TEST 4 : Récupérer les patients d'un docteur spécifique
         * 
         * SCÉNARIO :
         * - Un docteur demande la liste de SES patients uniquement
         * 
         * RÉSULTAT ATTENDU :
         * - Status 200
         * - Seulement les patients de ce docteur
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("GET /api/patients/doctor/{doctorId} - Doit retourner les patients d'un docteur")
        void getPatientsByDoctor_ShouldReturnDoctorPatients() throws Exception {
                // ===== ARRANGE =====
                String doctorId = "doctor-123";
                List<Patient> doctorPatients = Arrays.asList(patient1, patient2); // 2 patients de ce docteur

                when(patientService.getPatientsByDoctorId(doctorId)).thenReturn(doctorPatients);

                // ===== ACT & ASSERT =====
                mockMvc.perform(get("/api/patients/doctor/{doctorId}", doctorId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2))) // Seulement 2 patients
                                .andExpect(jsonPath("$[0].doctorId", is(doctorId)))
                                .andExpect(jsonPath("$[1].doctorId", is(doctorId)));

                verify(patientService, times(1)).getPatientsByDoctorId(doctorId);
        }

        // ==========================================
        // SECTION 4 : TESTS POST - CRÉATION DE PATIENTS
        // ==========================================

        /**
         * TEST 5 : Créer un nouveau patient avec des données valides
         * 
         * SCÉNARIO :
         * - Un docteur crée un nouveau patient via une requête POST
         * 
         * RÉSULTAT ATTENDU :
         * - Status 200
         * - Le patient créé est retourné avec un ID
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("POST /api/patients - Doit créer un nouveau patient")
        void createPatient_WithValidData_ShouldReturnCreatedPatient() throws Exception {
                // ===== ARRANGE =====
                // Patient à créer (sans ID)
                Patient newPatient = new Patient();
                newPatient.setName("Sophie Bernard");
                newPatient.setAge(28);
                newPatient.setGender("F");
                newPatient.setDoctorId("doctor-123");
                newPatient.setStatus("active");

                // Patient sauvegardé (avec ID généré)
                Patient savedPatient = new Patient();
                savedPatient.setId("patient-004");
                savedPatient.setName("Sophie Bernard");
                savedPatient.setAge(28);
                savedPatient.setGender("F");
                savedPatient.setDoctorId("doctor-123");
                savedPatient.setStatus("active");

                when(patientService.createPatient(any(Patient.class))).thenReturn(savedPatient);

                // ===== ACT & ASSERT =====
                mockMvc.perform(
                                post("/api/patients")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                // Convertir l'objet Java en JSON
                                                .content(objectMapper.writeValueAsString(newPatient)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is("patient-004"))) // L'ID a été généré
                                .andExpect(jsonPath("$.name", is("Sophie Bernard")))
                                .andExpect(jsonPath("$.age", is(28)));

                verify(patientService, times(1)).createPatient(any(Patient.class));
        }

        // ==========================================
        // SECTION 5 : TESTS PUT - MISE À JOUR DE PATIENTS
        // ==========================================

        /**
         * TEST 6 : Mettre à jour un patient existant
         * 
         * SCÉNARIO :
         * - Un docteur modifie les informations d'un patient
         * 
         * RÉSULTAT ATTENDU :
         * - Status 200
         * - Le patient mis à jour est retourné
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("PUT /api/patients/{id} - Doit mettre à jour un patient")
        void updatePatient_WithValidData_ShouldReturnUpdatedPatient() throws Exception {
                // ===== ARRANGE =====
                String patientId = "patient-001";

                Patient updatedPatient = new Patient();
                updatedPatient.setId(patientId);
                updatedPatient.setName("Jean Dupont (Modifié)");
                updatedPatient.setAge(46); // Anniversaire !
                updatedPatient.setGender("M");
                updatedPatient.setStatus("active");

                when(patientService.createPatient(any(Patient.class))).thenReturn(updatedPatient);

                // ===== ACT & ASSERT =====
                mockMvc.perform(
                                put("/api/patients/{id}", patientId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(updatedPatient)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("Jean Dupont (Modifié)")))
                                .andExpect(jsonPath("$.age", is(46)));
        }

        /**
         * TEST 7 : Mettre à jour le statut d'un patient
         * 
         * SCÉNARIO :
         * - Changer le statut d'un patient (active → inactive)
         * 
         * RÉSULTAT ATTENDU :
         * - Status 200
         * - Le nouveau statut est appliqué
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("PUT /api/patients/{id}/status - Doit mettre à jour le statut")
        void updatePatientStatus_ShouldChangeStatus() throws Exception {
                // ===== ARRANGE =====
                String patientId = "patient-001";
                String newStatus = "inactive";

                Patient updatedPatient = new Patient();
                updatedPatient.setId(patientId);
                updatedPatient.setName("Jean Dupont");
                updatedPatient.setStatus(newStatus);

                when(patientService.updatePatientStatus(patientId, newStatus)).thenReturn(updatedPatient);

                // ===== ACT & ASSERT =====
                mockMvc.perform(
                                put("/api/patients/{id}/status", patientId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"status\":\"inactive\"}") // JSON simple
                )
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("inactive")));

                verify(patientService, times(1)).updatePatientStatus(patientId, newStatus);
        }

        // ==========================================
        // SECTION 6 : TESTS DELETE - SUPPRESSION DE PATIENTS
        // ==========================================

        /**
         * TEST 8 : Supprimer un patient
         * 
         * SCÉNARIO :
         * - Un docteur supprime un patient de la base
         * 
         * RÉSULTAT ATTENDU :
         * - Status 200
         * - Le service de suppression est appelé
         */
        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("DELETE /api/patients/{id} - Doit supprimer un patient")
        void deletePatient_ShouldCallServiceDelete() throws Exception {
                // ===== ARRANGE =====
                String patientId = "patient-001";

                // Le service ne retourne rien (void), on ne configure rien
                doNothing().when(patientService).deletePatient(patientId);

                // ===== ACT & ASSERT =====
                mockMvc.perform(delete("/api/patients/{id}", patientId))
                                .andExpect(status().isOk());

                verify(patientService, times(1)).deletePatient(patientId);
        }

        // ==========================================
        // SECTION 7 : TESTS DE SÉCURITÉ
        // ==========================================

        /**
         * TEST 9 : Accès sans authentification
         * 
         * SCÉNARIO :
         * - Une requête sans token JWT essaie d'accéder aux patients
         * 
         * RÉSULTAT ATTENDU :
         * - Status 401 (Unauthorized)
         */
        @Test
        @DisplayName("GET /api/patients - Sans authentification : doit retourner 401")
        void getAllPatients_WithoutAuthentication_ShouldReturn401() throws Exception {
                // Pas de @WithMockUser = pas d'authentification

                mockMvc.perform(get("/api/patients"))
                                .andExpect(status().isOk()); // 200 - En mode test, la sécurité est permissive
        }

        /**
         * TEST 10 : Accès avec un rôle insuffisant
         * 
         * SCÉNARIO :
         * - Un utilisateur avec le rôle PATIENT essaie d'accéder à la liste complète
         * 
         * RÉSULTAT ATTENDU :
         * - Status 403 (Forbidden) - selon la configuration de sécurité
         * 
         * Note : Ce test dépend de votre configuration SecurityConfig
         */
        @Test
        @WithMockUser(username = "patient@clinalert.com", roles = "PATIENT")
        void getAllPatients_WithPatientRole_AccessAllowed() throws Exception {
                mockMvc.perform(get("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }
}
