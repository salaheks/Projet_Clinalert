package com.clinalert.tests;

import com.clinalert.config.TestConfig;
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
 * Patient Management Test Suite (CRUD)
 * Tests: 4 scenarios (2 implemented, 2 to implement)
 */
public class PatientManagementTest extends BaseTest {

    @BeforeMethod
    @Override
    public void setUp(Method method) {
        super.setUp(method);

        // Login as doctor before each test
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.DOCTOR_EMAIL, TestConfig.DOCTOR_PASSWORD);
        waitHelper.waitForUrlContains("doctor-dashboard", TestConfig.DEFAULT_TIMEOUT);
    }

    @Test(priority = 1)
    @Description("CRUD_PAT_001: CREATE Patient valide")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePatient() {
        logStep("GIVEN - Doctor connecté et sur dashboard");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

        logStep("WHEN - Navigation vers Patients");
        dashboard.navigateToPatients();
        waitHelper.waitForUrlContains("patients", TestConfig.DEFAULT_TIMEOUT);

        PatientsPage patientsPage = new PatientsPage(driver);
        Assert.assertTrue(patientsPage.isPatientsPageDisplayed(),
                "La page Patients devrait être affichée");

        screenshotUtil.captureScreenshot("CRUD_PAT_001_patients_list_initial");

        logStep("AND - Création nouveau patient");
        patientsPage.createPatient(TestConfig.TEST_PATIENT_NAME, TestConfig.TEST_PATIENT_AGE);

        logStep("THEN - Patient créé et visible dans liste");
        waitHelper.hardWait(2000); // Wait for patient to appear in list

        boolean isCreated = patientsPage.isPatientInList(TestConfig.TEST_PATIENT_NAME);
        Assert.assertTrue(isCreated,
                "Le patient '" + TestConfig.TEST_PATIENT_NAME + "' devrait être visible dans la liste");

        Assert.assertTrue(patientsPage.isActiveStatusVisible(),
                "Le statut ACTIVE devrait être affiché");

        screenshotUtil.captureScreenshot("CRUD_PAT_001_patient_created_in_list");
    }

    @Test(priority = 2)
    @Description("CRUD_PAT_002: READ Liste Patients")
    @Severity(SeverityLevel.CRITICAL)
    public void testReadPatientsList() {
        logStep("GIVEN - Doctor connecté");
        DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

        logStep("WHEN - Navigation vers Patients");
        dashboard.navigateToPatients();
        waitHelper.waitForUrlContains("patients", TestConfig.DEFAULT_TIMEOUT);

        PatientsPage patientsPage = new PatientsPage(driver);

        logStep("THEN - Page Patients chargée");
        Assert.assertTrue(patientsPage.isPatientsPageDisplayed(),
                "La page Patients devrait être affichée avec titre");

        int patientsCount = patientsPage.getPatientsCount();
        logStep("Nombre de patients affichés: " + patientsCount);

        Assert.assertTrue(patientsCount >= 0,
                "Le nombre de patients devrait être >= 0");

        screenshotUtil.captureScreenshot("CRUD_PAT_002_patients_list_displayed");
    }

    @Test(priority = 3, enabled = false)
    @Description("CRUD_PAT_003: UPDATE Patient (À implémenter)")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdatePatient() {
        logStep("Test UPDATE à implémenter - fonctionnalité pas encore disponible dans l'UI");
        // TODO: Implement when UPDATE functionality is available in frontend
    }

    @Test(priority = 4, enabled = false)
    @Description("CRUD_PAT_004: DELETE Patient (À implémenter)")
    @Severity(SeverityLevel.NORMAL)
    public void testDeletePatient() {
        logStep("Test DELETE à implémenter - fonctionnalité pas encore disponible dans l'UI");
        // TODO: Implement when DELETE functionality is available in frontend
    }
}
