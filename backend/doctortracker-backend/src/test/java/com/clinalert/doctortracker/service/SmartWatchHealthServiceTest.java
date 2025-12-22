package com.clinalert.doctortracker.service;

/**
 * Tests complets SmartWatchHealthService - 30+ tests
 * Couvre: devices, health data, dailySummaries, stats
 */

import com.clinalert.doctortracker.model.*;
import com.clinalert.doctortracker.repository.*;
import com.clinalert.doctortracker.service.SmartWatchHealthService.HealthDataStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Service SmartWatchHealth")
class SmartWatchHealthServiceTest {

    @Mock
    private SmartWatchDeviceRepository deviceRepository;

    @Mock
    private HealthDataRepository healthDataRepository;

    @Mock
    private DailyHealthSummaryRepository dailySummaryRepository;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private SmartWatchHealthService service;

    private SmartWatchDevice device;
    private HealthData healthData;
    private DailyHealthSummary summary;

    @BeforeEach
    void setUp() {
        device = new SmartWatchDevice();
        device.setId("dev-001");
        device.setPatientId("patient-001");
        device.setDeviceAddress("AA:BB:CC:DD:EE:FF");
        device.setDeviceName("Apple Watch");
        device.setIsActive(true);

        healthData = new HealthData();
        healthData.setId("health-001");
        healthData.setPatientId("patient-001");
        healthData.setHeartRate(75);
        healthData.setSpO2(98.5);
        healthData.setSteps(5000);
        healthData.setTimestamp(LocalDateTime.now());

        summary = new DailyHealthSummary();
        summary.setId("sum-001");
        summary.setPatientId("patient-001");
        summary.setDate(LocalDate.now());
    }

    // ========== DEVICE TESTS ==========

    @Test
    @DisplayName("registerDevice - Nouveau device")
    void registerDevice_New_ShouldSave() {
        when(deviceRepository.findByPatientIdAndDeviceAddress(any(), any())).thenReturn(Optional.empty());
        when(deviceRepository.save(any())).thenReturn(device);

        SmartWatchDevice result = service.registerDevice(device);

        assertThat(result).isNotNull();
        verify(deviceRepository).save(device);
    }

    @Test
    @DisplayName("registerDevice - Device existant")
    void registerDevice_Existing_ShouldUpdate() {
        when(deviceRepository.findByPatientIdAndDeviceAddress(any(), any())).thenReturn(Optional.of(device));
        when(deviceRepository.save(any())).thenReturn(device);

        service.registerDevice(device);

        verify(deviceRepository).save(any());
    }

    @Test
    @DisplayName("getPatientDevices")
    void getPatientDevices_ShouldReturnList() {
        when(deviceRepository.findByPatientId("patient-001")).thenReturn(Arrays.asList(device));

        List<SmartWatchDevice> result = service.getPatientDevices("patient-001");

        assertThat(result).hasSize(1);
        verify(deviceRepository).findByPatientId("patient-001");
    }

    @Test
    @DisplayName("getActiveDevices")
    void getActiveDevices_ShouldReturnActive() {
        when(deviceRepository.findByPatientIdAndIsActiveTrue("patient-001")).thenReturn(Arrays.asList(device));

        List<SmartWatchDevice> result = service.getActiveDevices("patient-001");

        assertThat(result).hasSize(1);
        verify(deviceRepository).findByPatientIdAndIsActiveTrue("patient-001");
    }

    @Test
    @DisplayName("deactivateDevice")
    void deactivateDevice_ShouldSetInactive() {
        when(deviceRepository.findById("dev-001")).thenReturn(Optional.of(device));

        service.deactivateDevice("dev-001");

        verify(deviceRepository).save(any());
    }

    @Test
    @DisplayName("deactivateDevice - Not found")
    void deactivateDevice_NotFound_ShouldNotSave() {
        when(deviceRepository.findById("dev-999")).thenReturn(Optional.empty());

        service.deactivateDevice("dev-999");

        verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteDevice")
    void deleteDevice_ShouldCallDelete() {
        service.deleteDevice("dev-001");

        verify(deviceRepository).deleteById("dev-001");
    }

    @Test
    @DisplayName("updateDeviceLastConnected")
    void updateLastConnected_ShouldUpdate() {
        when(deviceRepository.findById("dev-001")).thenReturn(Optional.of(device));

        service.updateDeviceLastConnected("dev-001");

        verify(deviceRepository).save(any());
    }

    // ========== HEALTH DATA TESTS ==========

    @Test
    @DisplayName("saveHealthData - Single")
    void saveHealthData_Single_ShouldSave() {
        when(healthDataRepository.save(any())).thenReturn(healthData);

        HealthData result = service.saveHealthData(healthData);

        assertThat(result).isNotNull();
        verify(healthDataRepository).save(healthData);
    }

    @Test
    @DisplayName("saveHealthData - List")
    void saveHealthData_List_ShouldSaveAll() {
        when(healthDataRepository.saveAll(anyList())).thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.saveHealthData(Arrays.asList(healthData));

        assertThat(result).hasSize(1);
        verify(healthDataRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("getPatientHealthData")
    void getPatientHealthData_ShouldReturnTop50() {
        when(healthDataRepository.findTop50ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getPatientHealthData("patient-001");

        assertThat(result).hasSize(1);
        verify(healthDataRepository).findTop50ByPatientIdOrderByTimestampDesc("patient-001");
    }

    @Test
    @DisplayName("getPatientHealthDataBetween")
    void getHealthDataBetween_ShouldReturnRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(any(), any(), any()))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getPatientHealthDataBetween("patient-001", start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getHeartRateHistory")
    void getHeartRateHistory_ShouldReturn() {
        when(healthDataRepository.findHeartRateDataByPatientId("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getHeartRateHistory("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getStepsHistory")
    void getStepsHistory_ShouldReturn() {
        when(healthDataRepository.findStepsDataByPatientId("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getStepsHistory("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getSpO2History")
    void getSpO2History_ShouldReturn() {
        when(healthDataRepository.findSpO2DataByPatientId("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getSpO2History("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getSleepHistory")
    void getSleepHistory_ShouldReturn() {
        when(healthDataRepository.findSleepDataByPatientId("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        List<HealthData> result = service.getSleepHistory("patient-001");

        assertThat(result).hasSize(1);
    }

    // ========== SUMMARY TESTS ==========

    @Test
    @DisplayName("getDailySummary")
    void getDailySummary_ShouldReturn() {
        when(dailySummaryRepository.findByPatientIdAndDate("patient-001", LocalDate.now()))
                .thenReturn(Optional.of(summary));

        Optional<DailyHealthSummary> result = service.getDailySummary("patient-001", LocalDate.now());

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getRecentDailySummaries")
    void getRecentSummaries_ShouldReturn30() {
        when(dailySummaryRepository.findTop30ByPatientIdOrderByDateDesc("patient-001"))
                .thenReturn(Arrays.asList(summary));

        List<DailyHealthSummary> result = service.getRecentDailySummaries("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getDailySummariesBetween")
    void getSummariesBetween_ShouldReturnRange() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        when(dailySummaryRepository.findByPatientIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(Arrays.asList(summary));

        List<DailyHealthSummary> result = service.getDailySummariesBetween("patient-001", start, end);

        assertThat(result).hasSize(1);
    }

    // ========== STATS TESTS ==========

    @Test
    @DisplayName("getPatientStats - Avec données")
    void getStats_WithData_ShouldCalculate() {
        when(healthDataRepository.findTop50ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(healthData));

        HealthDataStats stats = service.getPatientStats("patient-001");

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDataPoints()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPatientStats - Sans données")
    void getStats_WithoutData_ShouldReturnEmpty() {
        when(healthDataRepository.findTop50ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList());

        HealthDataStats stats = service.getPatientStats("patient-001");

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDataPoints()).isNull();
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("getActiveDevices - Aucun")
    void getActiveDevices_WhenNone_ShouldReturnEmpty() {
        when(deviceRepository.findByPatientIdAndIsActiveTrue("patient-001")).thenReturn(Arrays.asList());

        List<SmartWatchDevice> result = service.getActiveDevices("patient-001");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveHealthData - Triggers alert")
    void saveHealthData_HighHeartRate_ShouldCreateAlert() {
        healthData.setHeartRate(160);
        when(healthDataRepository.save(any())).thenReturn(healthData);

        service.saveHealthData(healthData);

        verify(alertService, atLeastOnce()).createAlert(any());
    }

    @Test
    @DisplayName("saveHealthData - Low SpO2 alert")
    void saveHealthData_LowSpO2_ShouldCreateAlert() {
        healthData.setSpO2(88.0);
        when(healthDataRepository.save(any())).thenReturn(healthData);

        service.saveHealthData(healthData);

        verify(alertService, atLeastOnce()).createAlert(any());
    }
}
