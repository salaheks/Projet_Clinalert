package com.clinalert.doctortracker.repository;

/**
 * Tests pour PatientRepository
 */

import com.clinalert.doctortracker.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Tests Repository Patient")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient1;
    private Patient patient2;
    private Patient patient3;

    @BeforeEach
    void setUp() {
        patient1 = new Patient();
        patient1.setName("Jean Dupont");
        patient1.setAge(45);
        patient1.setGender("M");
        patient1.setDoctorId("doctor-001");
        patient1.setClinicId("clinic-001");
        patient1.setStatus("active");

        patient2 = new Patient();
        patient2.setName("Marie Martin");
        patient2.setAge(32);
        patient2.setGender("F");
        patient2.setDoctorId("doctor-001");
        patient2.setClinicId("clinic-001");
        patient2.setStatus("active");

        patient3 = new Patient();
        patient3.setName("Pierre Bernard");
        patient3.setAge(67);
        patient3.setGender("M");
        patient3.setDoctorId("doctor-002");
        patient3.setClinicId("clinic-002");
        patient3.setStatus("inactive");
    }

    @Test
    @DisplayName("findByDoctorId - Doit retourner les patients du docteur")
    void findByDoctorId_ShouldReturnDoctorPatients() {
        // Arrange
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.persist(patient3);
        entityManager.flush();

        // Act
        List<Patient> patients = patientRepository.findByDoctorId("doctor-001");

        // Assert
        assertThat(patients).hasSize(2);
        assertThat(patients).extracting(Patient::getDoctorId)
                .containsOnly("doctor-001");
    }

    @Test
    @DisplayName("findByClinicId - Doit retourner les patients de la clinique")
    void findByClinicId_ShouldReturnClinicPatients() {
        // Arrange
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.persist(patient3);
        entityManager.flush();

        // Act
        List<Patient> patients = patientRepository.findByClinicId("clinic-001");

        // Assert
        assertThat(patients).hasSize(2);
        assertThat(patients).extracting(Patient::getClinicId)
                .containsOnly("clinic-001");
    }

    @Test
    @DisplayName("save - Doit sauvegarder un nouveau patient")
    void save_NewPatient_ShouldGenerateId() {
        // Act
        Patient saved = patientRepository.save(patient1);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Jean Dupont");
    }

    @Test
    @DisplayName("findAll - Doit retourner tous les patients")
    void findAll_ShouldReturnAllPatients() {
        // Arrange
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.persist(patient3);
        entityManager.flush();

        // Act
        List<Patient> patients = patientRepository.findAll();

        // Assert
        assertThat(patients).hasSize(3);
    }

    @Test
    @DisplayName("findById - Doit trouver un patient par ID")
    void findById_WhenExists_ShouldReturnPatient() {
        // Arrange
        Patient saved = entityManager.persistAndFlush(patient1);

        // Act
        var found = patientRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jean Dupont");
    }

    @Test
    @DisplayName("deleteById - Doit supprimer un patient")
    void deleteById_ShouldRemovePatient() {
        // Arrange
        Patient saved = entityManager.persistAndFlush(patient1);

        // Act
        patientRepository.deleteById(saved.getId());
        entityManager.flush();

        // Assert
        assertThat(patientRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("save - Doit mettre à jour un patient existant")
    void save_ExistingPatient_ShouldUpdate() {
        // Arrange
        Patient saved = entityManager.persistAndFlush(patient1);
        saved.setName("Jean Dupont (Modifié)");
        saved.setAge(46);

        // Act
        Patient updated = patientRepository.save(saved);
        entityManager.flush();

        // Assert
        assertThat(updated.getName()).isEqualTo("Jean Dupont (Modifié)");
        assertThat(updated.getAge()).isEqualTo(46);
    }

    @Test
    @DisplayName("count - Doit compter les patients")
    void count_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.flush();

        // Act
        long count = patientRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByDoctorId - Doit retourner liste vide si aucun patient")
    void findByDoctorId_WhenNoPatients_ShouldReturnEmpty() {
        // Act
        List<Patient> patients = patientRepository.findByDoctorId("doctor-999");

        // Assert
        assertThat(patients).isEmpty();
    }

    @Test
    @DisplayName("findByClinicId - Doit retourner liste vide si aucun patient")
    void findByClinicId_WhenNoPatients_ShouldReturnEmpty() {
        // Act
        List<Patient> patients = patientRepository.findByClinicId("clinic-999");

        // Assert
        assertThat(patients).isEmpty();
    }
}
