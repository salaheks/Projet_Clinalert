package com.clinalert.doctortracker.repository;

/**
 * ============================================
 * Tests pour DoctorRepository
 * ============================================
 * 
 * Tests des requêtes JPA pour les docteurs
 * 
 * @author ClinAlert Team
 */

import com.clinalert.doctortracker.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Tests Repository Doctor")
class DoctorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DoctorRepository doctorRepository;

    private Doctor doctor1;
    private Doctor doctor2;

    @BeforeEach
    void setUp() {
        doctor1 = new Doctor();
        doctor1.setName("Dr. Jean Dupont");
        doctor1.setSpecialty("Cardiologie");
        doctor1.setEmail("jean.dupont@clinalert.com");
        doctor1.setPhoneNumber("+33 6 12 34 56 78");

        doctor2 = new Doctor();
        doctor2.setName("Dr. Marie Martin");
        doctor2.setSpecialty("Neurologie");
        doctor2.setEmail("marie.martin@clinalert.com");
        doctor2.setPhoneNumber("+33 6 98 76 54 32");
    }

    @Test
    @DisplayName("save - Doit sauvegarder un nouveau docteur avec ID généré")
    void save_NewDoctor_ShouldGenerateId() {
        // Act
        Doctor saved = doctorRepository.save(doctor1);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dr. Jean Dupont");
        assertThat(saved.getSpecialty()).isEqualTo("Cardiologie");
    }

    @Test
    @DisplayName("findById - Doit trouver un docteur par son ID")
    void findById_WhenExists_ShouldReturnDoctor() {
        // Arrange
        Doctor saved = entityManager.persistAndFlush(doctor1);

        // Act
        Optional<Doctor> found = doctorRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Dr. Jean Dupont");
    }

    @Test
    @DisplayName("findById - Doit retourner Optional.empty() si non trouvé")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // Act
        Optional<Doctor> found = doctorRepository.findById("nonexistent-id");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - Doit retourner tous les docteurs")
    void findAll_ShouldReturnAllDoctors() {
        // Arrange
        entityManager.persist(doctor1);
        entityManager.persist(doctor2);
        entityManager.flush();

        // Act
        List<Doctor> doctors = doctorRepository.findAll();

        // Assert
        assertThat(doctors).hasSize(2);
        assertThat(doctors).extracting(Doctor::getName)
                .containsExactlyInAnyOrder("Dr. Jean Dupont", "Dr. Marie Martin");
    }

    @Test
    @DisplayName("findAll - Doit retourner liste vide si aucun docteur")
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        // Act
        List<Doctor> doctors = doctorRepository.findAll();

        // Assert
        assertThat(doctors).isEmpty();
    }

    @Test
    @DisplayName("save - Doit mettre à jour un docteur existant")
    void save_ExistingDoctor_ShouldUpdate() {
        // Arrange
        Doctor saved = entityManager.persistAndFlush(doctor1);
        saved.setName("Dr. Jean Dupont (Modifié)");
        saved.setSpecialty("Cardiologie Interventionnelle");

        // Act
        Doctor updated = doctorRepository.save(saved);
        entityManager.flush();

        // Assert
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("Dr. Jean Dupont (Modifié)");
        assertThat(updated.getSpecialty()).isEqualTo("Cardiologie Interventionnelle");
    }

    @Test
    @DisplayName("deleteById - Doit supprimer un docteur")
    void deleteById_ShouldRemoveDoctor() {
        // Arrange
        Doctor saved = entityManager.persistAndFlush(doctor1);
        String doctorId = saved.getId();

        // Act
        doctorRepository.deleteById(doctorId);
        entityManager.flush();

        // Assert
        Optional<Doctor> found = doctorRepository.findById(doctorId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("count - Doit compter le nombre de docteurs")
    void count_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persist(doctor1);
        entityManager.persist(doctor2);
        entityManager.flush();

        // Act
        long count = doctorRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("existsById - Doit vérifier l'existence d'un docteur")
    void existsById_WhenExists_ShouldReturnTrue() {
        // Arrange
        Doctor saved = entityManager.persistAndFlush(doctor1);

        // Act
        boolean exists = doctorRepository.existsById(saved.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - Doit retourner false si docteur inexistant")
    void existsById_WhenNotExists_ShouldReturnFalse() {
        // Act
        boolean exists = doctorRepository.existsById("nonexistent-id");

        // Assert
        assertThat(exists).isFalse();
    }
}
