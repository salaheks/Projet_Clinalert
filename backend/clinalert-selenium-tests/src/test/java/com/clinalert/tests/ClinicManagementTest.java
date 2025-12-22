package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.pages.ClinicsPage;
import com.clinalert.pages.DoctorDashboardPage;
import com.clinalert.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Clinic Management Test Suite (CRUD)
 * Tests: 4 scenarios (2 implemented, 2 to implement)
 */
public class ClinicManagementTest extends BaseTest {

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
    @Description("CRUD_CLI_001: CREATE Clinique valide")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateClinic() {
        logStep("GIVEN - Doctor connecté");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

        logStep("WHEN - Navigation vers Cliniques");
        dashboard.navigateToClinics();
        waitHelper.waitForUrlContains("clinics", TestConfig.DEFAULT_TIMEOUT);

        ClinicsPage clinicsPage = new ClinicsPage(driver);
        Assert.assertTrue(clinicsPage.isClinicsPageDisplayed(),
                "La page Cliniques devrait être affichée");

        screenshotUtil.captureScreenshot("CRUD_CLI_001_clinics_list_initial");

        logStep("AND - Création nouvelle clinique");
        clinicsPage.createClinic(
                TestConfig.TEST_CLINIC_NAME,
                TestConfig.TEST_CLINIC_ADDRESS,
                TestConfig.TEST_CLINIC_PHONE);

        logStep("THEN - Clinique créée avec succès");
        waitHelper.hardWait(2000);

        boolean isCreated = clinicsPage.isClinicInList(TestConfig.TEST_CLINIC_NAME);
        Assert.assertTrue(isCreated,
                "La clinique '" + TestConfig.TEST_CLINIC_NAME + "' devrait être visible");

        screenshotUtil.captureScreenshot("CRUD_CLI_001_clinic_created_in_list");
    }

    @Test(priority = 2)
    @Description("CRUD_CLI_002: READ Liste Cliniques")
    @Severity(SeverityLevel.CRITICAL)
    public void testReadClinicsList() {
        logStep("GIVEN - Doctor connecté");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

        logStep("WHEN - Navigation vers Cliniques");
        dashboard.navigateToClinics();
        waitHelper.waitForUrlContains("clinics", TestConfig.DEFAULT_TIMEOUT);

        ClinicsPage clinicsPage = new ClinicsPage(driver);

        logStep("THEN - Page Cliniques chargée");
        Assert.assertTrue(clinicsPage.isClinicsPageDisplayed(),
                "La page Cliniques devrait être affichée");

        int clinicsCount = clinicsPage.getClinicsCount();
        logStep("Nombre de cliniques: " + clinicsCount);

        screenshotUtil.captureScreenshot("CRUD_CLI_002_clinics_list_displayed");
    }

    @Test(priority = 3, enabled = false)
    @Description("CRUD_CLI_003: UPDATE Clinique (À implémenter)")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateClinic() {
        logStep("Test UPDATE clinique à implémenter");
        // TODO: Implement when UPDATE UI is available
    }

    @Test(priority = 4, enabled = false)
    @Description("CRUD_CLI_004: DELETE Clinique (À implémenter)")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteClinic() {
        logStep("Test DELETE clinique à implémenter");
        // TODO: Implement when DELETE UI is available
    }
}
