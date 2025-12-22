package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.pages.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * End-to-End Test Suite
 * Tests: 1 complete user journey (doctor + patient)
 */
public class E2ETest extends BaseTest {

    @Test(priority = 1)
    @Description("E2E_001: Workflow complet Doctor → Patient → Logout")
    @Severity(SeverityLevel.BLOCKER)
    public void testCompleteWorkflow() {
        // ========== DOCTOR FLOW ==========
        logStep("=== PHASE 1: DOCTOR LOGIN ===");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.DOCTOR_EMAIL, TestConfig.DOCTOR_PASSWORD);
        waitHelper.waitForUrlContains("doctor-dashboard", TestConfig.DEFAULT_TIMEOUT);

        DoctorDashboardPage doctorDashboard = new DoctorDashboardPage(driver);
        Assert.assertTrue(doctorDashboard.isDoctorDashboardDisplayed());
        screenshotUtil.captureScreenshot("E2E_001_step1_doctor_logged_in");

        logStep("=== PHASE 2: CREATE PATIENT ===");
        doctorDashboard.navigateToPatients();
        waitHelper.waitForUrlContains("patients", TestConfig.DEFAULT_TIMEOUT);

        PatientsPage patientsPage = new PatientsPage(driver);
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        String patientName = "E2E Patient " + timestamp;

        patientsPage.createPatient(patientName, "45");
        waitHelper.hardWait(2000);

        boolean patientCreated = patientsPage.isPatientInList(patientName);
        Assert.assertTrue(patientCreated, "Patient E2E devrait être créé");
        screenshotUtil.captureScreenshot("E2E_001_step2_patient_created");

        logStep("=== PHASE 3: CHECK ALERTS ===");
        doctorDashboard.navigateToAlerts();
        waitHelper.waitForUrlContains("alerts", TestConfig.DEFAULT_TIMEOUT);

        AlertsPage alertsPage = new AlertsPage(driver);
        Assert.assertTrue(alertsPage.isAlertsPageDisplayed());
        screenshotUtil.captureScreenshot("E2E_001_step3_alerts_viewed");

        logStep("=== PHASE 4: DOCTOR LOGOUT ===");
        doctorDashboard.navigateToSettings();
        waitHelper.hardWait(1500);

        SettingsPage settings = new SettingsPage(driver);
        settings.performLogout();
        waitHelper.waitForUrlContains("login", TestConfig.DEFAULT_TIMEOUT);
        Assert.assertTrue(driver.getCurrentUrl().contains("login"));
        screenshotUtil.captureScreenshot("E2E_001_step4_doctor_logged_out");

        // ========== PATIENT FLOW ==========
        logStep("=== PHASE 5: PATIENT LOGIN ===");
        loginPage.login(TestConfig.PATIENT_EMAIL, TestConfig.PATIENT_PASSWORD);
        waitHelper.waitForUrlContains("patient", TestConfig.DEFAULT_TIMEOUT);

        PatientDashboardPage patientDashboard = new PatientDashboardPage(driver);
        Assert.assertTrue(patientDashboard.isPatientDashboardDisplayed());
        screenshotUtil.captureScreenshot("E2E_001_step5_patient_logged_in");

        logStep("=== PHASE 6: PATIENT VIEW HEALTH ===");
        patientDashboard.viewHealthHistory();
        waitHelper.hardWait(2000);
        screenshotUtil.captureScreenshot("E2E_001_step6_patient_health_history");

        logStep("=== PHASE 7: PATIENT LOGOUT ===");
        patientDashboard.navigateToProfile();
        waitHelper.hardWait(1500);

        settings.performLogout();
        waitHelper.waitForUrlContains("login", TestConfig.DEFAULT_TIMEOUT);

        Assert.assertTrue(driver.getCurrentUrl().contains("login"),
                "Workflow E2E devrait se terminer sur la page login");
        screenshotUtil.captureScreenshot("E2E_001_step7_workflow_complete");

        logStep("=== WORKFLOW COMPLET: SUCCESS ===");
    }
}
