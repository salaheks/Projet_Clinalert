package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Patient Dashboard
 */
public class PatientDashboardPage {

    private WebDriver driver;

    // XPath Locators
    private By patientNameDisplay = By
            .xpath("//*[contains(text(), 'John') or contains(@class, 'username') or contains(@class, 'patient-name')]");
    private By myHealthMenuItem = By.xpath("//a[contains(text(), 'My Health') or contains(text(), 'Ma Santé')]");
    private By healthHistoryMenuItem = By.xpath(
            "//a[contains(text(), 'Health History') or contains(text(), 'Historique') or @href='#/health-history']");
    private By profileMenuItem = By
            .xpath("//a[contains(text(), 'Profile') or contains(text(), 'Profil') or contains(text(), 'Settings')]");

    // These should NOT be visible for patients
    private By patientsMenuAdmin = By.xpath("//a[contains(text(), 'Patients') and contains(@href, '/patients')]");
    private By clinicsMenuAdmin = By.xpath("//a[contains(text(), 'Cliniques') or contains(text(), 'Clinics')]");

    public PatientDashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if patient dashboard is displayed
     */
    public boolean isPatientDashboardDisplayed() {
        return driver.getCurrentUrl().contains("patient-dashboard") ||
                driver.getCurrentUrl().contains("patient");
    }

    /**
     * Get patient name displayed
     */
    public String getPatientName() {
        List<WebElement> names = driver.findElements(patientNameDisplay);
        return names.isEmpty() ? "" : names.get(0).getText();
    }

    /**
     * Navigate to Health History
     */
    public void viewHealthHistory() {
        List<WebElement> healthHistory = driver.findElements(healthHistoryMenuItem);
        if (!healthHistory.isEmpty()) {
            healthHistory.get(0).click();
        }
    }

    /**
     * Navigate to Profile/Settings
     */
    public void navigateToProfile() {
        List<WebElement> profile = driver.findElements(profileMenuItem);
        if (!profile.isEmpty()) {
            profile.get(0).click();
        }
    }

    /**
     * Check if My Health menu is visible
     */
    public boolean isMyHealthMenuVisible() {
        return driver.findElements(myHealthMenuItem).size() > 0;
    }

    /**
     * Check if Health History menu is visible
     */
    public boolean isHealthHistoryMenuVisible() {
        return driver.findElements(healthHistoryMenuItem).size() > 0;
    }

    /**
     * Verify patient does NOT have access to Patients CRUD menu
     * (Security test - should return false for patient role)
     */
    public boolean hasPatientsMenuAccess() {
        return driver.findElements(patientsMenuAdmin).size() > 0;
    }

    /**
     * Verify patient does NOT have access to Clinics menu
     * (Security test - should return false for patient role)
     */
    public boolean hasClinicsMenuAccess() {
        return driver.findElements(clinicsMenuAdmin).size() > 0;
    }
}
