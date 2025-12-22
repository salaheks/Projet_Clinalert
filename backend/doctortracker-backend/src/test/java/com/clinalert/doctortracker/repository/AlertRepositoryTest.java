package com.clinalert.doctortracker.repository;

/**
 * Tests pour AlertRepository
 */

import com.clinalert.doctortracker.model.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Tests Repository Alert")
class AlertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlertRepository alertRepository;

    private Alert alert1;
    private Alert alert2;
    private Alert alert3;

    @BeforeEach
    void setUp() {
        alert1 = new Alert();
        alert1.setPatientId("patient-001");
        alert1.setMeasurementId("measure-001");
        alert1.setMessage("Rythme cardiaque élevé");
        alert1.setSeverity("CRITICAL");
        alert1.setTimestamp(LocalDateTime.now());
        alert1.setRead(false);

        alert2 = new Alert();
        alert2.setPatientId("patient-001");
        alert2.setMeasurementId("measure-002");
        alert2.setMessage("Température élevée");
        alert2.setSeverity("MEDIUM");
        alert2.setTimestamp(LocalDateTime.now().minusHours(1));
        alert2.setRead(true);

        alert3 = new Alert();
        alert3.setPatientId("patient-002");
        alert3.setMeasurementId("measure-003");
        alert3.setMessage("Rappel médicament");
        alert3.setSeverity("LOW");
        alert3.setTimestamp(LocalDateTime.now().minusMinutes(30));
        alert3.setRead(false);
    }

    @Test
    @DisplayName("findByPatientId - Doit retourner les alertes du patient")
    void findByPatientId_ShouldReturnPatientAlerts() {
        // Arrange
        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.persist(alert3);
        entityManager.flush();

        // Act
        List<Alert> alerts = alertRepository.findByPatientId("patient-001");

        // Assert
        assertThat(alerts).hasSize(2);
        assertThat(alerts).extracting(Alert::getPatientId)
                .containsOnly("patient-001");
    }

    @Test
    @DisplayName("findByIsReadFalse - Doit retourner les alertes non lues")
    void findByIsReadFalse_ShouldReturnUnreadAlerts() {
        // Arrange
        entityManager.persist(alert1); // isRead = false by default
        entityManager.persist(alert3); // isRead = false by default

        // Persist alert2 and mark it as read
        Alert readAlert = entityManager.persistAndFlush(alert2);
        readAlert.setRead(true);
        entityManager.persistAndFlush(readAlert);

        entityManager.flush();

        // Act
        List<Alert> unreadAlerts = alertRepository.findByIsReadFalse();

        // Assert
        assertThat(unreadAlerts).hasSize(2);
        assertThat(unreadAlerts).allMatch(a -> !a.isRead());
    }

    @Test
    @DisplayName("findTop10ByPatientIdOrderByTimestampDesc - Doit retourner les 10 dernières alertes")
    void findTop10ByPatientId_ShouldReturnLatest10() {
        // Arrange
        for (int i = 0; i < 12; i++) {
            Alert alert = new Alert();
            alert.setPatientId("patient-001");
            alert.setMessage("Alert " + i);
            alert.setSeverity("LOW");
            alert.setTimestamp(LocalDateTime.now().minusHours(i));
            entityManager.persist(alert);
        }
        entityManager.flush();

        // Act
        List<Alert> latest = alertRepository.findTop10ByPatientIdOrderByTimestampDesc("patient-001");

        // Assert
        assertThat(latest).hasSize(10);
    }

    @Test
    @DisplayName("findBySeverityAndTimestampBefore - Doit filtrer par sévérité et date")
    void findBySeverityAndTimestampBefore_ShouldFilterCorrectly() {
        // Arrange
        entityManager.persist(alert1); // CRITICAL, now
        entityManager.persist(alert2); // MEDIUM, -1h
        entityManager.persist(alert3); // LOW, -30min
        entityManager.flush();

        // Act
        List<Alert> criticalBefore = alertRepository.findBySeverityAndTimestampBefore(
                "CRITICAL", LocalDateTime.now().plusMinutes(5));

        // Assert
        assertThat(criticalBefore).hasSize(1);
        assertThat(criticalBefore.get(0).getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("save - Doit sauvegarder une alerte")
    void save_ShouldPersistAlert() {
        // Act
        Alert saved = alertRepository.save(alert1);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("findAll - Doit retourner toutes les alertes")
    void findAll_ShouldReturnAllAlerts() {
        // Arrange
        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.persist(alert3);
        entityManager.flush();

        // Act
        List<Alert> all = alertRepository.findAll();

        // Assert
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("deleteById - Doit supprimer une alerte")
    void deleteById_ShouldRemoveAlert() {
        // Arrange
        Alert saved = entityManager.persistAndFlush(alert1);

        // Act
        alertRepository.deleteById(saved.getId());
        entityManager.flush();

        // Assert
        assertThat(alertRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("count - Doit compter les alertes")
    void count_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.flush();

        // Act
        long count = alertRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByPatientId - Doit retourner liste vide si aucune alerte")
    void findByPatientId_WhenNoAlerts_ShouldReturnEmpty() {
        // Act
        List<Alert> alerts = alertRepository.findByPatientId("patient-999");

        // Assert
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("findByIsReadFalse - Doit retourner liste vide si toutes lues")
    void findByIsReadFalse_WhenAllRead_ShouldReturnEmpty() {
        // Arrange
        Alert readAlert1 = entityManager.persistAndFlush(alert1);
        Alert readAlert2 = entityManager.persistAndFlush(alert2);

        // Mark as read after persist
        readAlert1.setRead(true);
        readAlert2.setRead(true);
        entityManager.persistAndFlush(readAlert1);
        entityManager.persistAndFlush(readAlert2);

        // Act
        List<Alert> unread = alertRepository.findByIsReadFalse();

        // Assert
        assertThat(unread).isEmpty();
    }
}
