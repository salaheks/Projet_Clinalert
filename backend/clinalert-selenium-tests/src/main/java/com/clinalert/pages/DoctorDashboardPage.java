package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Doctor Dashboard
 */
public class DoctorDashboardPage {

    private WebDriver driver;

    // XPath Locators
    // XPath Locators - Explicit Semantic IDs
    private By usernameDisplay = By.xpath("//*[contains(@aria-label, 'Dr.') or contains(@aria-label, 'Gregory')]"); // Keep
                                                                                                                    // generic
                                                                                                                    // for
                                                                                                                    // now
                                                                                                                    // or
                                                                                                                    // ask
                                                                                                                    // user?

    // Sidebar Navigation - fallback to text/aria-label as specific IDs (26/27)
    // caused timeouts
    // for
    // node-27
    // User-Provided Explicit IDs
    // Hybrid Strategy: User ID (Primary) | Text/Label (Fallback)
    // Patients: 26, Clinics: 27, Alerts: 28, Profile: 45, Logout: 316
    private By patientsMenuItem = By.xpath(
            "//*[@id='flt-semantic-node-26'] | //flt-semantics[@role='button'][contains(., 'Patients') or contains(., 'Patients')]");
    private By clinicsMenuItem = By.xpath(
            "//*[@id='flt-semantic-node-27'] | //flt-semantics[@role='button'][contains(., 'Cliniques') or contains(., 'Clinics')]");
    private By alertsMenuItem = By.xpath(
            "//*[@id='flt-semantic-node-28'] | //flt-semantics[@role='button'][contains(., 'Alertes') or contains(., 'Alerts')]");

    private By profileIcon = By.xpath("//*[@id='flt-semantic-node-45']"); // Renamed to profileIcon to match methods
    private By logoutButton = By.xpath("//*[@id='flt-semantic-node-316']");

    // Quick Add Patient
    private By quickAddPatientButton = By.xpath("//*[@id='flt-semantic-node-46']");

    // Fallback/Generic for checks
    private By sidebar = By
            .xpath("//flt-semantics[contains(@aria-label, 'Navigation') or contains(@role, 'navigation')]");

    // Settings logic (Implicit via Profile)
    private By settingsLink = By.xpath("//*[@id='flt-semantic-node-315']");

    public DoctorDashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if doctor dashboard is displayed
     */
    public boolean isDoctorDashboardDisplayed() {
        return driver.getCurrentUrl().contains("doctor-dashboard");
    }

    /**
     * Get displayed username
     */
    public String getUsername() {
        List<WebElement> usernames = driver.findElements(usernameDisplay);
        return usernames.isEmpty() ? "" : usernames.get(0).getText();
    }

    /**
     * Check if sidebar is visible
     */
    public boolean isSidebarVisible() {
        return driver.findElements(sidebar).size() > 0;
    }

    /**
     * Navigate to Patients page
     */
    public void navigateToPatients() {
        driver.findElement(patientsMenuItem).click();
    }

    /**
     * Navigate to Clinics page
     */
    public void navigateToClinics() {
        driver.findElement(clinicsMenuItem).click();
    }

    /**
     * Navigate to Alerts page
     */
    public void navigateToAlerts() {
        driver.findElement(alertsMenuItem).click();
    }

    /**
     * Click on profile icon
     */
    public void clickProfileIcon() {
        List<WebElement> profileIcons = driver.findElements(profileIcon);
        if (!profileIcons.isEmpty()) {
            profileIcons.get(0).click();
        }
    }

    /**
     * Navigate to settings/profile page
     */
    /**
     * Perform Logout directly from Dashboard (Process: Profile -> Logout)
     */
    public void performLogout() {
        clickProfileIcon();
        try {
            Thread.sleep(500); // Wait for dropdown
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(logoutButton).click();
    }

    /**
     * Navigate to settings/profile page
     */
    public void navigateToSettings() {
        // Placeholder or implement if ID provided
        clickProfileIcon();
    }

    /**
     * Check if Patients menu item is visible (used to verify doctor role)
     */
    public boolean isPatientsMenuVisible() {
        return driver.findElements(patientsMenuItem).size() > 0;
    }

    /**
     * Check if Clinics menu item is visible
     */
    public boolean isClinicsMenuVisible() {
        return driver.findElements(clinicsMenuItem).size() > 0;
    }
}
