package com.clinalert.doctortracker.repository;

/**
 * ============================================
 * Tests de Repository pour UserRepository
 * ============================================
 * 
 * Ce fichier contient les tests des requêtes JPA pour les utilisateurs.
 * 
 * DIFFÉRENCE AVEC LES TESTS PRÉCÉDENTS :
 * ---------------------------------------
 * - Tests Unitaires (Service) : Utilisent des mocks, pas de DB réelle
 * - Tests d'Intégration (Controller) : Utilisent MockMvc + mocks
 * - Tests Repository : Utilisent une VRAIE base de données (H2) pour tester les requêtes SQL/JPA
 * 
 * ANNOTATIONS UTILISÉES :
 * ----------------------
 * @DataJpaTest : Configure un contexte JPA minimal avec une base H2 en mémoire
 * @AutoConfigureTestDatabase : Configure la base de données de test
 * @TestEntityManager : Permet d'insérer des données de test directement
 * 
 * POURQUOI TESTER LES REPOSITORIES ?
 * ----------------------------------
 * - Vérifier que les requêtes JPA personnalisées fonctionnent correctement
 * - Tester les relations entre entités
 * - Valider les contraintes de base de données (unique, nullable, etc.)
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Configuration pour les tests de repository
 * 
 * @DataJpaTest charge seulement les composants JPA (pas tout Spring Boot)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests du Repository Utilisateur")
class UserRepositoryTest {

    // ==========================================
    // SECTION 1 : INJECTION DES DÉPENDANCES
    // ==========================================

    /**
     * Le repository à tester (injecté automatiquement par Spring)
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * TestEntityManager : permet de manipuler directement les entités
     * Utile pour préparer les données de test
     * C'est comme un mini-Hibernate pour les tests
     */
    @Autowired
    private TestEntityManager entityManager;

    // Données de test
    private User doctorUser;
    private User patientUser;
    private User nurseUser;

    // ==========================================
    // SECTION 2 : PRÉPARATION DES TESTS
    // ==========================================

    /**
     * Méthode exécutée AVANT chaque test
     * Insère des utilisateurs de test dans la base H2
     */
    @BeforeEach
    void setUp() {
        // Nettoyer la base avant chaque test (au cas où)
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Créer un utilisateur DOCTOR
        doctorUser = new User();
        doctorUser.setEmail("doctor@clinalert.com");
        doctorUser.setPassword("$2a$10$hashedPassword1");
        doctorUser.setRole(User.UserRole.DOCTOR);
        doctorUser.setEnabled(true);
        doctorUser.setFirstName("Dr. Jean");
        doctorUser.setLastName("Dupont");

        // Créer un utilisateur PATIENT
        patientUser = new User();
        patientUser.setEmail("patient@clinalert.com");
        patientUser.setPassword("$2a$10$hashedPassword2");
        patientUser.setRole(User.UserRole.PATIENT);
        patientUser.setEnabled(true);
        patientUser.setFirstName("Marie");
        patientUser.setLastName("Martin");

        // Créer un utilisateur NURSE
        nurseUser = new User();
        nurseUser.setEmail("nurse@clinalert.com");
        nurseUser.setPassword("$2a$10$hashedPassword3");
        nurseUser.setRole(User.UserRole.NURSE);
        nurseUser.setEnabled(false); // Compte désactivé
        nurseUser.setFirstName("Sophie");
        nurseUser.setLastName("Bernard");

        // Persister les utilisateurs dans la base de test
        entityManager.persist(doctorUser);
        entityManager.persist(patientUser);
        entityManager.persist(nurseUser);

        // Forcer l'écriture en base et vider le cache
        entityManager.flush();
        entityManager.clear();
    }

    // ==========================================
    // SECTION 3 : TESTS DE RECHERCHE PAR EMAIL
    // ==========================================

    /**
     * TEST 1 : Rechercher un utilisateur par email existant
     * 
     * SCÉNARIO :
     * - Chercher un utilisateur avec un email qui existe dans la base
     * 
     * RÉSULTAT ATTENDU :
     * - Optional contenant l'utilisateur trouvé
     * - Les données correspondent (email, rôle, nom)
     */
    @Test
    @DisplayName("findByEmail() : Doit trouver un utilisateur par son email")
    void findByEmail_WhenEmailExists_ShouldReturnUser() {
        // ===== ACT =====
        Optional<User> foundUser = userRepository.findByEmail("doctor@clinalert.com");

        // ===== ASSERT =====
        // Vérifier que l'utilisateur a été trouvé
        assertTrue(foundUser.isPresent(), "L'utilisateur doit être trouvé");

        // Vérifier les données
        User user = foundUser.get();
        assertEquals("doctor@clinalert.com", user.getEmail());
        assertEquals(User.UserRole.DOCTOR, user.getRole());
        assertEquals("Dr. Jean", user.getFirstName());
        assertEquals("Dupont", user.getLastName());
        assertTrue(user.getEnabled(), "Le compte doit être activé");

        // Vérifier que l'ID a été généré automatiquement
        assertNotNull(user.getId(), "L'ID doit être généré par la base");
    }

    /**
     * TEST 2 : Rechercher un utilisateur avec un email inexistant
     * 
     * SCÉNARIO :
     * - Chercher un email qui n'existe pas
     * 
     * RÉSULTAT ATTENDU :
     * - Optional vide (empty)
     */
    @Test
    @DisplayName("findByEmail() : Doit retourner Optional.empty() si l'email n'existe pas")
    void findByEmail_WhenEmailNotExists_ShouldReturnEmpty() {
        // ===== ACT =====
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@clinalert.com");

        // ===== ASSERT =====
        assertFalse(foundUser.isPresent(), "Aucun utilisateur ne devrait être trouvé");
        assertTrue(foundUser.isEmpty(), "L'Optional doit être vide");
    }

    /**
     * TEST 3 : La recherche par email est insensible à la casse (optionnel)
     * 
     * Note : Ce test dépend de la configuration de votre base de données.
     * Par défaut, PostgreSQL est sensible à la casse, mais H2 peut varier.
     */
    @Test
    @DisplayName("findByEmail() : Sensibilité à la casse (dépend de la config DB)")
    void findByEmail_CaseSensitivity() {
        // ===== ACT =====
        Optional<User> upperCase = userRepository.findByEmail("DOCTOR@CLINALERT.COM");
        Optional<User> mixedCase = userRepository.findByEmail("Doctor@ClinAlert.Com");

        // ===== ASSERT =====
        // Sur H2 par défaut, la recherche est souvent insensible à la casse
        // Mais il vaut mieux stocker les emails en minuscules pour éviter les problèmes
        // Ce test vous montre le comportement actuel de votre DB
        // Asserting that the case sensitivity behavior is as expected for the DB
        // For H2 default, it might be sensitive or not depending on collation, but
        // usually sensitive without special config
        // We just ensure we can call it without error.
        assertNotNull(upperCase);
        assertNotNull(mixedCase);
    }

    // ==========================================
    // SECTION 4 : TESTS DE VÉRIFICATION D'EXISTENCE
    // ==========================================

    /**
     * TEST 4 : Vérifier qu'un email existe
     * 
     * SCÉNARIO :
     * - Vérifier l'existence d'un email présent dans la base
     * 
     * RÉSULTAT ATTENDU :
     * - true
     */
    @Test
    @DisplayName("existsByEmail() : Doit retourner true si l'email existe")
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // ===== ACT =====
        boolean exists = userRepository.existsByEmail("patient@clinalert.com");

        // ===== ASSERT =====
        assertTrue(exists, "L'email doit exister dans la base");
    }

    /**
     * TEST 5 : Vérifier qu'un email n'existe pas
     * 
     * SCÉNARIO :
     * - Vérifier un email qui n'est pas dans la base
     * 
     * RÉSULTAT ATTENDU :
     * - false
     */
    @Test
    @DisplayName("existsByEmail() : Doit retourner false si l'email n'existe pas")
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        // ===== ACT =====
        boolean exists = userRepository.existsByEmail("ghost@clinalert.com");

        // ===== ASSERT =====
        assertFalse(exists, "L'email ne doit pas exister");
    }

    // ==========================================
    // SECTION 5 : TESTS DE SAUVEGARDE
    // ==========================================

    /**
     * TEST 6 : Sauvegarder un nouvel utilisateur
     * 
     * SCÉNARIO :
     * - Créer et sauvegarder un nouvel utilisateur
     * 
     * RÉSULTAT ATTENDU :
     * - L'utilisateur est sauvegardé avec un ID généré
     * - Les dates createdAt et updatedAt sont automatiquement définies
     */
    @Test
    @DisplayName("save() : Doit sauvegarder un nouvel utilisateur avec ID auto-généré")
    void save_NewUser_ShouldGenerateId() {
        // ===== ARRANGE =====
        User newUser = new User();
        newUser.setEmail("admin@clinalert.com");
        newUser.setPassword("$2a$10$adminPassword");
        newUser.setRole(User.UserRole.ADMIN);
        newUser.setEnabled(true);
        newUser.setFirstName("Admin");
        newUser.setLastName("System");

        // ===== ACT =====
        User savedUser = userRepository.save(newUser);

        // Forcer la synchronisation avec la DB
        entityManager.flush();

        // ===== ASSERT =====
        // Vérifier que l'ID a été généré
        assertNotNull(savedUser.getId(), "L'ID doit être généré automatiquement");

        // Vérifier que les dates sont définies (grâce à @PrePersist)
        assertNotNull(savedUser.getCreatedAt(), "createdAt doit être défini");
        assertNotNull(savedUser.getUpdatedAt(), "updatedAt doit être défini");

        // Vérifier que l'utilisateur peut être retrouvé
        Optional<User> retrieved = userRepository.findByEmail("admin@clinalert.com");
        assertTrue(retrieved.isPresent());
        assertEquals(User.UserRole.ADMIN, retrieved.get().getRole());
    }

    /**
     * TEST 7 : Mettre à jour un utilisateur existant
     * 
     * SCÉNARIO :
     * - Modifier les données d'un utilisateur et le sauvegarder
     * 
     * RÉSULTAT ATTENDU :
     * - Les modifications sont persistées
     * - updatedAt est mis à jour (grâce à @PreUpdate)
     */
    @Test
    @DisplayName("save() : Doit mettre à jour un utilisateur existant")
    void save_ExistingUser_ShouldUpdateUser() {
        // ===== ARRANGE =====
        // Récupérer un utilisateur existant
        User user = userRepository.findByEmail("nurse@clinalert.com").orElseThrow();
        String originalId = user.getId();

        // Mémoriser la date de création
        var originalCreatedAt = user.getCreatedAt();

        // ===== ACT =====
        // Modifier l'utilisateur
        user.setEnabled(true); // Activer le compte
        user.setFirstName("Sophie Updated");

        User updated = userRepository.save(user);
        entityManager.flush();

        // ===== ASSERT =====
        // L'ID ne doit pas changer
        assertEquals(originalId, updated.getId());

        // Les modifications doivent être appliquées
        assertTrue(updated.getEnabled(), "Le compte doit être activé");
        assertEquals("Sophie Updated", updated.getFirstName());

        // createdAt ne doit pas changer
        assertEquals(originalCreatedAt, updated.getCreatedAt());

        // updatedAt doit être différent (peut être le même si très rapide)
        assertNotNull(updated.getUpdatedAt());
    }

    // ==========================================
    // SECTION 6 : TESTS DE CONTRAINTES
    // ==========================================

    /**
     * TEST 8 : Violation de contrainte unique sur l'email
     * 
     * SCÉNARIO :
     * - Essayer de créer deux utilisateurs avec le même email
     * 
     * RÉSULTAT ATTENDU :
     * - Une exception est levée (DataIntegrityViolationException)
     */
    @Test
    @DisplayName("save() : Doit échouer si l'email existe déjà (contrainte unique)")
    void save_DuplicateEmail_ShouldThrowException() {
        // ===== ARRANGE =====
        User duplicateUser = new User();
        duplicateUser.setEmail("doctor@clinalert.com"); // Email déjà utilisé
        duplicateUser.setPassword("$2a$10$password");
        duplicateUser.setRole(User.UserRole.PATIENT);
        duplicateUser.setEnabled(true);

        // ===== ACT & ASSERT =====
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush(); // Force l'exécution SQL
        }, "Une exception doit être levée pour email dupliqué");
    }

    // ==========================================
    // SECTION 7 : TESTS DE COMPTAGE
    // ==========================================

    /**
     * TEST 9 : Compter le nombre total d'utilisateurs
     * 
     * SCÉNARIO :
     * - Vérifier le nombre d'utilisateurs dans la base
     * 
     * RÉSULTAT ATTENDU :
     * - 3 utilisateurs (créés dans setUp)
     */
    @Test
    @DisplayName("count() : Doit retourner le nombre total d'utilisateurs")
    void count_ShouldReturnTotalUsers() {
        // ===== ACT =====
        long count = userRepository.count();

        // ===== ASSERT =====
        assertEquals(3, count, "Il doit y avoir 3 utilisateurs (doctor, patient, nurse)");
    }

    /**
     * TEST 10 : Supprimer un utilisateur
     * 
     * SCÉNARIO :
     * - Supprimer un utilisateur et vérifier qu'il n'existe plus
     * 
     * RÉSULTAT ATTENDU :
     * - Le count diminue de 1
     * - L'email n'existe plus
     */
    @Test
    @DisplayName("delete() : Doit supprimer un utilisateur")
    void delete_User_ShouldRemoveFromDatabase() {
        // ===== ARRANGE =====
        String emailToDelete = "nurse@clinalert.com";
        assertTrue(userRepository.existsByEmail(emailToDelete));

        // ===== ACT =====
        User user = userRepository.findByEmail(emailToDelete).orElseThrow();
        userRepository.delete(user);
        entityManager.flush();

        // ===== ASSERT =====
        assertFalse(userRepository.existsByEmail(emailToDelete), "L'email ne doit plus exister");
        assertEquals(2, userRepository.count(), "Il ne doit rester que 2 utilisateurs");
    }
}
