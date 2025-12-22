package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Alerts Page
 */
public class AlertsPage {

    private WebDriver driver;

    // XPath Locators
    private By pageTitle = By.xpath("//*[contains(text(), 'Alertes') or contains(text(), 'Alerts')]");
    private By alertsList = By.xpath("//div[contains(@class, 'alert-card') or contains(@class, 'alert-item')]");

    // Severity badges
    private By criticalBadge = By.xpath("//*[contains(text(), 'CRITICAL') or contains(@class, 'severity-critical')]");
    private By highBadge = By.xpath("//*[contains(text(), 'HIGH') or contains(@class, 'severity-high')]");
    private By mediumBadge = By.xpath("//*[contains(text(), 'MEDIUM') or contains(@class, 'severity-medium')]");

    // Alert details
    private By alertTimestamp = By.xpath("//*[contains(text(), 'il y a') or contains(text(), 'ago')]");
    private By patientInfo = By.xpath("//div[contains(@class, 'patient-info') or contains(@class, 'patient-name')]");

    public AlertsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if alerts page is displayed
     */
    public boolean isAlertsPageDisplayed() {
        return driver.getCurrentUrl().contains("alerts") &&
                driver.findElements(pageTitle).size() > 0;
    }

    /**
     * Get alerts count
     */
    public int getAlertsCount() {
        return driver.findElements(alertsList).size();
    }

    /**
     * Check if CRITICAL badge is visible
     */
    public boolean hasCriticalAlert() {
        return driver.findElements(criticalBadge).size() > 0;
    }

    /**
     * Check if HIGH badge is visible
     */
    public boolean hasHighAlert() {
        return driver.findElements(highBadge).size() > 0;
    }

    /**
     * Check if MEDIUM badge is visible
     */
    public boolean hasMediumAlert() {
        return driver.findElements(mediumBadge).size() > 0;
    }

    /**
     * Get all severity badges (for color-coding verification)
     */
    public List<WebElement> getAllSeverityBadges() {
        By allBadges = By
                .xpath("//*[contains(text(), 'CRITICAL') or contains(text(), 'HIGH') or contains(text(), 'MEDIUM')]");
        return driver.findElements(allBadges);
    }

    /**
     * Check if timestamps are displayed (relative time)
     */
    public boolean areTimestampsDisplayed() {
        return driver.findElements(alertTimestamp).size() > 0;
    }

    /**
     * Check if patient information is visible in alerts
     */
    public boolean isPatientInfoDisplayed() {
        return driver.findElements(patientInfo).size() > 0;
    }

    /**
     * Verify badge color-coding (requires getting CSS properties)
     */
    public boolean verifyCriticalBadgeColor() {
        List<WebElement> badges = driver.findElements(criticalBadge);
        if (!badges.isEmpty()) {
            String className = badges.get(0).getAttribute("class");
            String style = badges.get(0).getAttribute("style");
            // Should contain red/danger color
            return (className != null && (className.contains("red") || className.contains("danger"))) ||
                    (style != null && style.contains("red"));
        }
        return false;
    }
}
