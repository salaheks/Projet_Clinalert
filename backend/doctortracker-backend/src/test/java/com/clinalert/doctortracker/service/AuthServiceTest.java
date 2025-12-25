package com.clinalert.doctortracker.service;

/**
 * ============================================
 * Tests Unitaires pour AuthService
 * ============================================
 * 
 * Ce fichier contient tous les tests pour le service d'authentification.
 * 
 * TYPES DE TESTS :
 * ----------------
 * 1. Tests de login (succès et échecs)
 * 2. Tests d'inscription (succès et échecs)
 * 3. Tests de récupération de l'utilisateur courant
 * 
 * ANNOTATIONS UTILISÉES :
 * ----------------------
 * @ExtendWith(MockitoExtension.class) : Active Mockito pour créer des mocks
 * @Mock : Crée un objet "simulé" (mock) d'une dépendance
 * @InjectMocks : Injecte automatiquement les mocks dans la classe à tester
 * @Test : Marque une méthode comme test
 * @DisplayName : Donne un nom descriptif au test
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.dto.LoginRequest;
import com.clinalert.doctortracker.dto.LoginResponse;
import com.clinalert.doctortracker.dto.RegisterRequest;
import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.DoctorRepository;
import com.clinalert.doctortracker.repository.PatientRepository;
import com.clinalert.doctortracker.repository.UserRepository;
import com.clinalert.doctortracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Configuration de Mockito pour les tests unitaires isolés
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du Service d'Authentification")
class AuthServiceTest {

    // ==========================================
    // SECTION 1 : DÉCLARATION DES MOCKS
    // ==========================================

    /**
     * Mock du repository des utilisateurs
     * Simule les appels à la base de données pour les utilisateurs
     */
    @Mock
    private UserRepository userRepository;

    /**
     * Mock du repository des docteurs
     * Simule les appels à la base de données pour les docteurs
     */
    @Mock
    private DoctorRepository doctorRepository;

    /**
     * Mock du repository des patients
     * Simule les appels à la base de données pour les patients
     */
    @Mock
    private PatientRepository patientRepository;

    /**
     * Mock de l'encodeur de mot de passe
     * Simule le chiffrement des mots de passe
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Mock du générateur de token JWT
     * Simule la création de tokens JWT
     */
    @Mock
    private JwtTokenProvider tokenProvider;

    /**
     * Mock du gestionnaire d'authentification Spring Security
     * Simule le processus d'authentification
     */
    @Mock
    private AuthenticationManager authenticationManager;

    /**
     * L'instance du service à tester
     * Mockito injectera automatiquement tous les mocks ci-dessus
     */
    @InjectMocks
    private AuthService authService;

    // Variables communes pour tous les tests
    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    // ==========================================
    // SECTION 2 : PRÉPARATION DES TESTS
    // ==========================================

    /**
     * Méthode exécutée AVANT chaque test
     * Prépare les données nécessaires pour les tests
     */
    @BeforeEach
    void setUp() {
        // Créer un utilisateur de test
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("doctor@clinalert.com");
        testUser.setPassword("$2a$10$encodedPassword"); // Mot de passe hashé simulé
        testUser.setRole(User.UserRole.DOCTOR);
        testUser.setEnabled(true);

        // Créer une requête de login de test
        loginRequest = new LoginRequest();
        loginRequest.setEmail("doctor@clinalert.com");
        loginRequest.setPassword("password123");

        // Créer une requête d'inscription de test
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newdoctor@clinalert.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(User.UserRole.DOCTOR);
        registerRequest.setName("Dr. Test");
        registerRequest.setSpecialty("Cardiology");
    }

    // ==========================================
    // SECTION 3 : TESTS DE LOGIN
    // ==========================================

    /**
     * TEST 1 : Login réussi avec des identifiants valides
     * 
     * SCÉNARIO :
     * - Un utilisateur existant se connecte avec un email et mot de passe corrects
     * 
     * RÉSULTAT ATTENDU :
     * - Une réponse LoginResponse contenant le token JWT et les infos utilisateur
     * - Le token ne doit pas être null
     * - L'email et le rôle doivent correspondre
     */
    @Test
    @DisplayName("Login réussi : doit retourner un token JWT et les informations utilisateur")
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // ===== ARRANGE (Préparer) =====
        // Créer un objet Authentication simulé
        Authentication mockAuthentication = mock(Authentication.class);

        // Configurer le comportement des mocks :
        // 1. L'authentification doit réussir
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // 2. La recherche de l'utilisateur par email doit retourner notre utilisateur
        // de test
        when(userRepository.findByEmail("doctor@clinalert.com"))
                .thenReturn(Optional.of(testUser));

        // 3. La génération du token doit retourner un token fictif
        when(tokenProvider.generateToken(testUser, "user-123", "DOCTOR"))
                .thenReturn("jwt-token-123456");

        // ===== ACT (Agir) =====
        // Exécuter la méthode à tester
        LoginResponse response = authService.login(loginRequest);

        // ===== ASSERT (Vérifier) =====
        // Vérifier que la réponse n'est pas nulle
        assertNotNull(response, "La réponse ne doit pas être null");

        // Vérifier le contenu de la réponse
        assertEquals("jwt-token-123456", response.getToken(), "Le token JWT doit correspondre");
        assertEquals("user-123", response.getUserId(), "L'ID utilisateur doit correspondre");
        assertEquals("doctor@clinalert.com", response.getEmail(), "L'email doit correspondre");
        assertEquals("DOCTOR", response.getRole(), "Le rôle doit correspondre");

        // Vérifier que les méthodes des mocks ont bien été appelées
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByEmail("doctor@clinalert.com"); // Appelé 2 fois : ligne 49 et 65
        verify(tokenProvider, times(1)).generateToken(testUser, "user-123", "DOCTOR");
    }

    /**
     * TEST 2 : Login échoué avec un mot de passe incorrect
     * 
     * SCÉNARIO :
     * - Un utilisateur essaie de se connecter avec un mauvais mot de passe
     * 
     * RÉSULTAT ATTENDU :
     * - Une exception BadCredentialsException doit être levée
     */
    @Test
    @DisplayName("Login échoué : doit lever une exception avec un mot de passe incorrect")
    void login_WithInvalidPassword_ShouldThrowException() {
        // ===== ARRANGE =====
        // Simuler une authentification échouée
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Mot de passe incorrect"));

        // ===== ACT & ASSERT =====
        // Vérifier qu'une exception est bien levée
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        }, "Une BadCredentialsException doit être levée pour un mot de passe incorrect");

        // Vérifier que l'authentification a bien été tentée
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Vérifier que le token n'a PAS été généré (car l'auth a échoué)
        verify(tokenProvider, never()).generateToken(any(), anyString(), anyString());
    }

    /**
     * TEST 3 : Login échoué avec un email inexistant
     * 
     * SCÉNARIO :
     * - Un utilisateur essaie de se connecter avec un email qui n'existe pas
     * 
     * RÉSULTAT ATTENDU :
     * - Une exception RuntimeException doit être levée
     */
    @Test
    @DisplayName("Login échoué : doit lever une exception si l'utilisateur n'existe pas")
    void login_WithNonExistentEmail_ShouldThrowException() {
        // ===== ARRANGE =====
        // L'authentification réussit (car on ne vérifie pas l'existence dans
        // authManager)
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // Mais la recherche de l'utilisateur retourne vide
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        }, "Une RuntimeException doit être levée si l'utilisateur n'existe pas");

        // Vérifier que la recherche a bien été tentée (2 fois car debug + méthode
        // principale)
        verify(userRepository, times(2)).findByEmail(anyString());
    }

    /**
     * TEST 3b : Login avec email contenant des caractères spéciaux (CRLF)
     *
     * SCÉNARIO :
     * - Un utilisateur essaie de se connecter avec un email contenant des sauts de
     * ligne
     *
     * RÉSULTAT ATTENDU :
     * - Le login doit procéder normalement (après sanitization dans les logs)
     * - Si l'utilisateur n'existe pas avec cet email, une exception est levée
     */
    @Test
    @DisplayName("Login avec sanitization : doit gérer les caractères CRLF dans l'email")
    void login_WithCRLFInEmail_ShouldSanitizeAndProceed() {
        // ===== ARRANGE =====
        LoginRequest maliciousRequest = new LoginRequest();
        maliciousRequest.setEmail("hacker@clinalert.com\r\nADMIN");
        maliciousRequest.setPassword("password123");

        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // On simule que l'utilisateur n'est pas trouvé (pour aller jusqu'au log WARN
        // qui utilise la sanitization)
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        assertThrows(RuntimeException.class, () -> {
            authService.login(maliciousRequest);
        });

        // La vérification principale ici est que le code s'exécute sans erreur de
        // formatage de log
        // et que la méthode sanitizeForLog a été traversée par l'exécution
        verify(userRepository, atLeastOnce()).findByEmail(anyString());
    }

    // ==========================================
    // SECTION 4 : TESTS D'INSCRIPTION
    // ==========================================

    /**
     * TEST 4 : Inscription réussie d'un nouveau docteur
     * 
     * SCÉNARIO :
     * - Un nouveau docteur s'inscrit avec des informations valides
     * 
     * RÉSULTAT ATTENDU :
     * - Un utilisateur et un profil docteur doivent être créés
     * - Un token JWT doit être généré
     */
    @Test
    @DisplayName("Inscription réussie : doit créer un utilisateur DOCTOR et son profil")
    void register_WithValidDoctorData_ShouldCreateUserAndDoctorProfile() {
        // ===== ARRANGE =====
        // L'email n'existe pas encore
        when(userRepository.existsByEmail("newdoctor@clinalert.com")).thenReturn(false);

        // Le mot de passe est encodé
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");

        // L'utilisateur est sauvegardé et retourné avec un ID
        User savedUser = new User();
        savedUser.setId("new-user-456");
        savedUser.setEmail("newdoctor@clinalert.com");
        savedUser.setPassword("$2a$10$encodedPassword");
        savedUser.setRole(User.UserRole.DOCTOR);
        savedUser.setEnabled(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Le profil docteur est sauvegardé
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor());

        // Le token est généré
        when(tokenProvider.generateToken(any(User.class), eq("new-user-456"), eq("DOCTOR")))
                .thenReturn("new-jwt-token");

        // ===== ACT =====
        LoginResponse response = authService.register(registerRequest);

        // ===== ASSERT =====
        // Vérifier la réponse
        assertNotNull(response, "La réponse ne doit pas être null");
        assertEquals("new-jwt-token", response.getToken(), "Le token doit être généré");
        assertEquals("new-user-456", response.getUserId(), "L'ID doit correspondre");
        assertEquals("newdoctor@clinalert.com", response.getEmail(), "L'email doit correspondre");
        assertEquals("DOCTOR", response.getRole(), "Le rôle doit être DOCTOR");

        // Vérifier que toutes les opérations ont été effectuées
        verify(userRepository, times(1)).existsByEmail("newdoctor@clinalert.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verify(tokenProvider, times(1)).generateToken(any(User.class), eq("new-user-456"), eq("DOCTOR"));

        // Vérifier que le repository patient n'a PAS été appelé (car c'est un DOCTOR)
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /**
     * TEST 5 : Inscription réussie d'un nouveau patient
     * 
     * SCÉNARIO :
     * - Un nouveau patient s'inscrit
     * 
     * RÉSULTAT ATTENDU :
     * - Un utilisateur et un profil patient doivent être créés
     */
    @Test
    @DisplayName("Inscription réussie : doit créer un utilisateur PATIENT et son profil")
    void register_WithValidPatientData_ShouldCreateUserAndPatientProfile() {
        // ===== ARRANGE =====
        // Modifier la requête pour un patient
        registerRequest.setRole(User.UserRole.PATIENT);
        registerRequest.setName("Patient Test");
        registerRequest.setAge(35);
        registerRequest.setGender("M");
        registerRequest.setDoctorId("doctor-123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");

        User savedUser = new User();
        savedUser.setId("patient-789");
        savedUser.setEmail("newdoctor@clinalert.com");
        savedUser.setRole(User.UserRole.PATIENT);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(patientRepository.save(any(Patient.class))).thenReturn(new Patient());
        when(tokenProvider.generateToken(any(), anyString(), anyString())).thenReturn("patient-token");

        // ===== ACT =====
        LoginResponse response = authService.register(registerRequest);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals("PATIENT", response.getRole());

        // Vérifier que le profil patient a été créé
        verify(patientRepository, times(1)).save(any(Patient.class));

        // Vérifier que le profil docteur n'a PAS été créé
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * TEST 6 : Inscription échouée - email déjà utilisé
     * 
     * SCÉNARIO :
     * - Un utilisateur essaie de s'inscrire avec un email déjà existant
     * 
     * RÉSULTAT ATTENDU :
     * - Une exception RuntimeException avec le message "Email already in use"
     */
    @Test
    @DisplayName("Inscription échouée : doit lever IllegalArgumentException si l'email existe déjà")
    void register_WithExistingEmail_ShouldThrowException() {
        // ===== ARRANGE =====
        // L'email existe déjà dans la base
        when(userRepository.existsByEmail("newdoctor@clinalert.com")).thenReturn(true);

        // ===== ACT & ASSERT =====
        // Correction: AuthService lance maintenant IllegalArgumentException, pas
        // RuntimeException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        // Vérifier le message de l'exception
        assertEquals("Email already in use", exception.getMessage());

        // Vérifier qu'aucune sauvegarde n'a été effectuée
        verify(userRepository, never()).save(any(User.class));
    }

    // ==========================================
    // SECTION 5 : TESTS DE RÉCUPÉRATION DE L'UTILISATEUR COURANT
    // ==========================================

    @Test
    @DisplayName("getCurrentUser - Utilisateur connecté")
    void getCurrentUser_WhenAuthenticated_ShouldReturnUser() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@test.com");

        org.springframework.security.core.context.SecurityContext securityContext = mock(
                org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
    }

    @Test
    @DisplayName("getCurrentUser - Non connecté")
    void getCurrentUser_WhenNotAuthenticated_ShouldReturnNull() {
        // Arrange
        org.springframework.security.core.context.SecurityContext securityContext = mock(
                org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // Act
        User result = authService.getCurrentUser();

        // Assert
        assertNull(result);
    }
}
