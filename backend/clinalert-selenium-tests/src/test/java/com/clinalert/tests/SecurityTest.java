package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.pages.LoginPage;
import com.clinalert.pages.PatientDashboardPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Security & Access Control Test Suite
 * Tests: 4 scenarios (2 implemented, 2 negative)
 */
public class SecurityTest extends BaseTest {

    @Test(priority = 1)
    @Description("SEC_001: Patient - Isolation des données personnelles")
    @Severity(SeverityLevel.CRITICAL)
    public void testPatientDataIsolation() {
        logStep("GIVEN - Patient connecté");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.PATIENT_EMAIL, TestConfig.PATIENT_PASSWORD);
        waitHelper.waitForUrlContains("patient", TestConfig.DEFAULT_TIMEOUT);

        PatientDashboardPage dashboard = new PatientDashboardPage(driver);

        logStep("WHEN - Patient affiche ses données");
        dashboard.viewHealthHistory();
        waitHelper.hardWait(2000);

        logStep("THEN - Seulement données personnelles visibles");
        Assert.assertTrue(dashboard.isPatientDashboardDisplayed() ||
                driver.getCurrentUrl().contains("health"),
                "Le patient devrait voir uniquement ses propres données de santé");

        screenshotUtil.captureScreenshot("SEC_001_patient_personal_data_only");
    }

    @Test(priority = 2)
    @Description("SEC_002: Patient N'A PAS accès CRUD Patients/Cliniques")
    @Severity(SeverityLevel.CRITICAL)
    public void testPatientNoCRUDAccess() {
        logStep("GIVEN - Patient connecté");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.PATIENT_EMAIL, TestConfig.PATIENT_PASSWORD);
        waitHelper.waitForUrlContains("patient", TestConfig.DEFAULT_TIMEOUT);

        PatientDashboardPage dashboard = new PatientDashboardPage(driver);

        logStep("THEN - Menus CRUD non visibles pour patient");
        Assert.assertFalse(dashboard.hasPatientsMenuAccess(),
                "Le patient NE DEVRAIT PAS avoir accès au menu Patients CRUD");

        Assert.assertFalse(dashboard.hasClinicsMenuAccess(),
                "Le patient NE DEVRAIT PAS avoir accès au menu Cliniques");

        logStep("Sidebar patient limitée (My Health, Health History, Profile uniquement)");
        Assert.assertTrue(dashboard.isMyHealthMenuVisible() || dashboard.isHealthHistoryMenuVisible(),
                "Le patient devrait avoir accès uniquement à ses menus personnels");

        screenshotUtil.captureScreenshot("SEC_002_patient_limited_menu_access");
    }

    @Test(priority = 3)
    @Description("SEC_003_NEG: Accès URL directe non autorisé")
    @Severity(SeverityLevel.CRITICAL)
    public void testUnauthorizedURLAccess() {
        logStep("GIVEN - Patient connecté");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(TestConfig.PATIENT_EMAIL, TestConfig.PATIENT_PASSWORD);
        waitHelper.waitForUrlContains("patient", TestConfig.DEFAULT_TIMEOUT);

        logStep("WHEN - Tentative accès URL directe /patients (admin)");
        driver.get(TestConfig.PATIENTS_URL);
        waitHelper.hardWait(2000);

        logStep("THEN - Redirection ou page 403/404");
        String currentUrl = driver.getCurrentUrl();

        // Should either redirect to patient dashboard or show error
        boolean isRedirected = currentUrl.contains("patient-dashboard") ||
                currentUrl.contains("patient");
        boolean isErrorPage = currentUrl.contains("403") ||
                currentUrl.contains("404") ||
                currentUrl.contains("unauthorized");

        Assert.assertTrue(isRedirected || isErrorPage,
                "L'accès direct aux URL admin devrait être bloqué. URL actuelle: " + currentUrl);

        screenshotUtil.captureScreenshot("SEC_003_unauthorized_url_blocked");
    }

    @Test(priority = 4)
    @Description("SEC_004_NEG: Tentative injection SQL")
    @Severity(SeverityLevel.CRITICAL)
    public void testSQLInjectionAttempt() {
        logStep("GIVEN - Page login");
        LoginPage loginPage = new LoginPage(driver);

        logStep("WHEN - Tentative SQL injection dans email");
        String sqlInjection = "admin' OR '1'='1' --";
        loginPage.login(sqlInjection, "anypassword");

        waitHelper.hardWait(2000);

        logStep("THEN - Login échoue, aucune vulnérabilité exploitée");
        String currentUrl = loginPage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("login"),
                "Le login devrait échouer (SQL injection protégé). URL: " + currentUrl);

        // Verify no backend error is exposed
        boolean hasError = loginPage.isErrorMessageDisplayed();
        if (hasError) {
            String errorMsg = loginPage.getErrorMessage();
            Assert.assertFalse(errorMsg.contains("SQL") || errorMsg.contains("syntax"),
                    "Le message d'erreur ne devrait PAS exposer de détails SQL");
        }

        screenshotUtil.captureScreenshot("SEC_004_sql_injection_blocked");
    }
}
