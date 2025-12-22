package com.clinalert.doctortracker.service;

/**
 * Tests EscalationService - 4 tests
 * Couvre: Alert escalation logic, scheduled tasks
 */

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests EscalationService")
class EscalationServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private EscalationService escalationService;

    private Alert mediumAlert;

    @BeforeEach
    void setUp() {
        mediumAlert = new Alert();
        mediumAlert.setId("alert-001");
        mediumAlert.setPatientId("patient-001");
        mediumAlert.setSeverity("MEDIUM");
        mediumAlert.setMessage("High heart rate detected");
        mediumAlert.setTimestamp(LocalDateTime.now().minusMinutes(20));
    }

    @Test
    @DisplayName("Should escalate old MEDIUM alerts to HIGH")
    void checkAndEscalateAlerts_OldMediumAlerts_ShouldEscalateToHigh() {
        List<Alert> pendingAlerts = Arrays.asList(mediumAlert);
        when(alertRepository.findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class)))
                .thenReturn(pendingAlerts);

        escalationService.checkAndEscalateAlerts();

        verify(alertRepository).findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class));
        verify(alertRepository).save(argThat(alert -> alert.getSeverity().equals("HIGH") &&
                alert.getMessage().startsWith("[ESCALATED]")));
    }

    @Test
    @DisplayName("Should NOT escalate recent MEDIUM alerts")
    void checkAndEscalateAlerts_RecentMediumAlerts_ShouldNotEscalate() {
        when(alertRepository.findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        escalationService.checkAndEscalateAlerts();

        verify(alertRepository).findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class));
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should escalate multiple alerts")
    void checkAndEscalateAlerts_MultipleAlerts_ShouldEscalateAll() {
        Alert alert1 = new Alert();
        alert1.setId("alert-001");
        alert1.setSeverity("MEDIUM");
        alert1.setMessage("Alert 1");
        alert1.setTimestamp(LocalDateTime.now().minusMinutes(20));

        Alert alert2 = new Alert();
        alert2.setId("alert-002");
        alert2.setSeverity("MEDIUM");
        alert2.setMessage("Alert 2");
        alert2.setTimestamp(LocalDateTime.now().minusMinutes(25));

        List<Alert> pendingAlerts = Arrays.asList(alert1, alert2);
        when(alertRepository.findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class)))
                .thenReturn(pendingAlerts);

        escalationService.checkAndEscalateAlerts();

        verify(alertRepository, times(2)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should prepend ESCALATED to message")
    void checkAndEscalateAlerts_ShouldPrependEscalatedToMessage() {
        List<Alert> pendingAlerts = Arrays.asList(mediumAlert);
        when(alertRepository.findBySeverityAndTimestampBefore(eq("MEDIUM"), any(LocalDateTime.class)))
                .thenReturn(pendingAlerts);

        escalationService.checkAndEscalateAlerts();

        verify(alertRepository).save(argThat(alert -> alert.getMessage().contains("[ESCALATED]") &&
                alert.getMessage().contains("High heart rate detected")));
    }
}
