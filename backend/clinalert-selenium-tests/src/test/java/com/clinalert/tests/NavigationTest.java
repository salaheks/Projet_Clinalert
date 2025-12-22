package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.pages.AlertsPage;
import com.clinalert.pages.DoctorDashboardPage;
import com.clinalert.pages.LoginPage;
import com.clinalert.pages.PatientsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Navigation Test Suite
 * Tests: 2 scenarios
 */
public class NavigationTest extends BaseTest {

    @BeforeMethod
    @Override
    public void setUp(Method method) {
        super.setUp(method);

        // Login as doctor
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.DOCTOR_EMAIL, TestConfig.DOCTOR_PASSWORD);
        waitHelper.waitForUrlContains("doctor-dashboard", TestConfig.DEFAULT_TIMEOUT);
    }

    @Test(priority = 1)
    @Description("NAV_001: Navigation Dashboard → Patients")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateDashboardToPatients() {
        logStep("GIVEN - Doctor sur dashboard");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);
        Assert.assertTrue(dashboard.isDoctorDashboardDisplayed());

        logStep("WHEN - Clic menu Patients");
        dashboard.navigateToPatients();

        logStep("THEN - Page Patients chargée en < 3s");
        boolean loaded = waitHelper.waitForUrlContains("patients", 3);
        Assert.assertTrue(loaded, "La navigation vers Patients devrait réussir en < 3s");

        PatientsPage patientsPage = new PatientsPage(driver);
        Assert.assertTrue(patientsPage.isPatientsPageDisplayed(),
                "La page Patients devrait être affichée");

        screenshotUtil.captureScreenshot("NAV_001_patients_page_loaded");
    }

    @Test(priority = 2)
    @Description("NAV_002: Navigation Dashboard → Alertes avec badges sévérité")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateDashboardToAlerts() {
        logStep("GIVEN - Doctor sur dashboard");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

        logStep("WHEN - Clic menu Alertes");
        dashboard.navigateToAlerts();

        logStep("THEN - Page Alertes chargée");
        waitHelper.waitForUrlContains("alerts", TestConfig.DEFAULT_TIMEOUT);

        AlertsPage alertsPage = new AlertsPage(driver);
        Assert.assertTrue(alertsPage.isAlertsPageDisplayed(),
                "La page Alertes devrait être affichée");

        logStep("AND - Badges sévérité visibles avec color-coding");
        boolean hasCritical = alertsPage.hasCriticalAlert();
        boolean hasHigh = alertsPage.hasHighAlert();
        boolean hasMedium = alertsPage.hasMediumAlert();

        logStep("Badges détectés - CRITICAL: " + hasCritical +
                ", HIGH: " + hasHigh + ", MEDIUM: " + hasMedium);

        Assert.assertTrue(hasCritical || hasHigh || hasMedium,
                "Au moins un badge de sévérité devrait être visible");

        Assert.assertTrue(alertsPage.areTimestampsDisplayed(),
                "Les timestamps relatifs devraient être affichés");

        screenshotUtil.captureScreenshot("NAV_002_alerts_page_with_badges");
    }
}
