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
    private By usernameDisplay = By.xpath(
            "//*[contains(text(), 'Dr.') or contains(text(), 'Gregory') or contains(@class, 'username') or contains(@class, 'user-name')]");
    private By sidebar = By.xpath("//nav[contains(@class, 'sidebar') or contains(@class, 'navigation')]");
    private By patientsMenuItem = By
            .xpath("//a[contains(text(), 'Patients') or @href='#/patients' or contains(@title, 'Patients')]");
    private By clinicsMenuItem = By
            .xpath("//a[contains(text(), 'Cliniques') or contains(text(), 'Clinics') or @href='#/clinics']");
    private By alertsMenuItem = By
            .xpath("//a[contains(text(), 'Alertes') or contains(text(), 'Alerts') or @href='#/alerts']");
    private By profileIcon = By.xpath(
            "//button[contains(@class, 'profile') or contains(@aria-label, 'profile') or contains(@class, 'user-menu')]");
    private By settingsLink = By.xpath(
            "//a[contains(text(), 'Paramètres') or contains(text(), 'Settings') or contains(text(), 'Profile')]");

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
    public void navigateToSettings() {
        clickProfileIcon();
        try {
            Thread.sleep(500); // Wait for menu animation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> settings = driver.findElements(settingsLink);
        if (!settings.isEmpty()) {
            settings.get(0).click();
        }
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
