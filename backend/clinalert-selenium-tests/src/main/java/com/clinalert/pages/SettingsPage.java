package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Settings/Profile Page
 */
public class SettingsPage {

    private WebDriver driver;

    // XPath Locators
    private By pageTitle = By
            .xpath("//*[contains(text(), 'Paramètres') or contains(text(), 'Settings') or contains(text(), 'Profil')]");
    private By logoutButton = By.xpath(
            "//button[contains(text(), 'déconnecter') or contains(text(), 'Logout') or contains(text(), 'Log out')]");
    private By confirmLogoutButton = By
            .xpath("//button[contains(text(), 'Confirmer') or contains(text(), 'Confirm') or contains(text(), 'Oui')]");
    private By profileSection = By
            .xpath("//div[contains(@class, 'profile-section') or contains(@class, 'user-profile')]");

    public SettingsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if settings page is displayed
     */
    public boolean isSettingsPageDisplayed() {
        return driver.getCurrentUrl().contains("settings") ||
                driver.getCurrentUrl().contains("profil") ||
                driver.findElements(pageTitle).size() > 0;
    }

    /**
     * Scroll to logout button (usually at bottom of page)
     */
    public void scrollToLogout() {
        try {
            List<WebElement> logoutButtons = driver.findElements(logoutButton);
            if (!logoutButtons.isEmpty()) {
                WebElement btn = logoutButtons.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
                Thread.sleep(500); // Wait for scroll animation
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Click logout button
     */
    public void clickLogout() {
        List<WebElement> logoutButtons = driver.findElements(logoutButton);
        if (!logoutButtons.isEmpty()) {
            WebElement btn = logoutButtons.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(logoutButtons.get(0));
            btn.click();

            // Wait for potential confirmation dialog
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Confirm logout if confirmation dialog appears
     */
    public void confirmLogout() {
        List<WebElement> confirmButtons = driver.findElements(confirmLogoutButton);
        if (!confirmButtons.isEmpty()) {
            confirmButtons.get(0).click();
        }
    }

    /**
     * Complete logout flow (scroll, click, confirm if needed)
     */
    public void performLogout() {
        scrollToLogout();
        clickLogout();
        confirmLogout(); // Will do nothing if no confirmation dialog

        // Wait for redirect to login
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if profile section is visible
     */
    public boolean isProfileSectionVisible() {
        return driver.findElements(profileSection).size() > 0;
    }
}
