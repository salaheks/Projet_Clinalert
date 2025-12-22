package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.pages.DoctorDashboardPage;
import com.clinalert.pages.LoginPage;
import com.clinalert.pages.PatientDashboardPage;
import com.clinalert.pages.SettingsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Authentication Test Suite
 * Tests: 6 scenarios (3 positive + 3 negative)
 */
public class AuthenticationTest extends BaseTest {

        @Test(priority = 1)
        @Description("AUTH_001: Login Doctor avec credentials valides")
        @Severity(SeverityLevel.CRITICAL)
        public void testLoginDoctorSuccess() {
                logStep("GIVEN - Navigateur ouvert sur page login");
                LoginPage loginPage = new LoginPage(driver);

                logStep("WHEN - Saisie email et password doctor valides");
                loginPage.login(TestConfig.DOCTOR_EMAIL, TestConfig.DOCTOR_PASSWORD);

                logStep("THEN - Redirection vers doctor dashboard");
                waitHelper.waitForUrlContains("doctor-dashboard", TestConfig.DEFAULT_TIMEOUT);

                DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);
                Assert.assertTrue(dashboard.isDoctorDashboardDisplayed(),
                                "Le dashboard doctor devrait être affiché");

                logStep("AND - Nom du doctor affiché correctement");
                String username = dashboard.getUsername();
                Assert.assertTrue(username.contains("Gregory") || username.contains("House"),
                                "Le nom 'Gregory House' devrait être affiché, trouvé: " + username);

                Assert.assertTrue(dashboard.isSidebarVisible(),
                                "La sidebar de navigation devrait être visible");

                screenshotUtil.captureScreenshot("AUTH_001_doctor_dashboard_logged_in");
        }

        @Test(priority = 2)
        @Description("AUTH_002: Login Patient avec credentials valides")
        @Severity(SeverityLevel.CRITICAL)
        public void testLoginPatientSuccess() {
                logStep("GIVEN - Page login affichée");
                LoginPage loginPage = new LoginPage(driver);

                logStep("WHEN - Saisie email et password patient valides");
                loginPage.login(TestConfig.PATIENT_EMAIL, TestConfig.PATIENT_PASSWORD);

                logStep("THEN - Redirection vers patient dashboard");
                waitHelper.waitForUrlContains("patient", TestConfig.DEFAULT_TIMEOUT);

                PatientDashboardPage dashboard = new PatientDashboardPage(driver);
                Assert.assertTrue(dashboard.isPatientDashboardDisplayed(),
                                "Le dashboard patient devrait être affiché");

                logStep("AND - Menu patient-specific visible");
                Assert.assertTrue(dashboard.isMyHealthMenuVisible() || dashboard.isHealthHistoryMenuVisible(),
                                "Les menus patient (My Health/Health History) devraient être visibles");

                screenshotUtil.captureScreenshot("AUTH_002_patient_dashboard_logged_in");
        }

        @Test(priority = 3)
        @Description("AUTH_004_NEG: Login avec credentials invalides")
        @Severity(SeverityLevel.NORMAL)
        public void testLoginInvalidCredentials() {
                logStep("GIVEN - Page login affichée");
                LoginPage loginPage = new LoginPage(driver);

                logStep("WHEN - Tentative login avec password invalide");
                loginPage.login(TestConfig.DOCTOR_EMAIL, "MAUVAIS_PASSWORD_123");

                // Wait for potential error message
                waitHelper.hardWait(2000);

                logStep("THEN - Message erreur affiché ou pas de redirection");
                String currentUrl = loginPage.getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("login"),
                                "L'utilisateur devrait rester sur la page login, URL actuelle: " + currentUrl);

                // Check if error message is displayed (may vary by implementation)
                boolean hasError = loginPage.isErrorMessageDisplayed();
                if (hasError) {
                        String errorMsg = loginPage.getErrorMessage();
                        logStep("Message d'erreur détecté: " + errorMsg);
                        Assert.assertTrue(errorMsg.toLowerCase().contains("invalide") ||
                                        errorMsg.toLowerCase().contains("incorrect"),
                                        "Le message devrait indiquer des identifiants invalides");
                } else {
                        logStep("Pas de message d'erreur visible - validation côté backend attendue");
                }

                screenshotUtil.captureScreenshot("AUTH_004_login_invalid_credentials_error");
        }

        @Test(priority = 4)
        @Description("AUTH_005_NEG: Password inférieur à 8 caractères")
        @Severity(SeverityLevel.NORMAL)
        public void testLoginPasswordTooShort() {
                logStep("GIVEN - Page login affichée");
                LoginPage loginPage = new LoginPage(driver);

                logStep("WHEN - Saisie password trop court (< 8 caractères)");
                loginPage.enterEmail(TestConfig.DOCTOR_EMAIL);
                loginPage.enterPassword("1234"); // Only 4 characters

                waitHelper.hardWait(1000); // Wait for validation

                logStep("THEN - Validation côté client active");
                // Check if button is disabled or field shows error
                boolean isButtonDisabled = loginPage.isLoginButtonDisabled();
                boolean isFieldInvalid = loginPage.isPasswordFieldInvalid();

                Assert.assertTrue(isButtonDisabled || isFieldInvalid,
                                "Le bouton Login devrait être désactivé OU le champ password marqué invalide. " +
                                                "Bouton disabled: " + isButtonDisabled + ", Champ invalide: "
                                                + isFieldInvalid);

                screenshotUtil.captureScreenshot("AUTH_005_login_password_too_short");
        }

        @Test(priority = 5)
        @Description("AUTH_006_NEG: Champs requis vides")
        @Severity(SeverityLevel.NORMAL)
        public void testLoginEmptyFields() {
                logStep("GIVEN - Page login avec champs vides");
                LoginPage loginPage = new LoginPage(driver);

                logStep("WHEN - Clic sur Login sans saisir de données");
                loginPage.clickLogin();

                waitHelper.hardWait(1000);

                logStep("THEN - Validation HTML5 empêche soumission");
                String currentUrl = loginPage.getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("login"),
                                "L'utilisateur devrait rester sur la page login (validation HTML5)");

                // Check for required field errors
                boolean hasRequiredErrors = loginPage.areRequiredFieldErrorsShown();
                logStep("Erreurs champs requis affichées: " + hasRequiredErrors);

                screenshotUtil.captureScreenshot("AUTH_006_login_empty_fields");
        }

        @Test(priority = 6)
        @Description("AUTH_003: Logout avec succès")
        @Severity(SeverityLevel.CRITICAL)
        public void testLogoutSuccess() {
                logStep("GIVEN - Doctor connecté sur dashboard");
                LoginPage loginPage = new LoginPage(driver);
                loginPage.login(TestConfig.DOCTOR_EMAIL, TestConfig.DOCTOR_PASSWORD);
                waitHelper.waitForUrlContains("doctor-dashboard", TestConfig.DEFAULT_TIMEOUT);

                DoctorDashboardPage dashboard = new DoctorDashboardPage(driver);

                logStep("WHEN - Navigation vers Settings et logout");
                dashboard.navigateToSettings();
                waitHelper.hardWait(1500); // Wait for settings page load

                SettingsPage settings = new SettingsPage(driver);
                settings.performLogout();

                logStep("THEN - Redirection vers page login");
                waitHelper.waitForUrlContains("login", TestConfig.DEFAULT_TIMEOUT);

                Assert.assertTrue(driver.getCurrentUrl().contains("login"),
                                "L'utilisateur devrait être redirigé vers la page login après logout");

                screenshotUtil.captureScreenshot("AUTH_003_logout_complete");
        }
}
