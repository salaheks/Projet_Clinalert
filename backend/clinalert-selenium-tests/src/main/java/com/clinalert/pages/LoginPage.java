package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Login Page
 * Uses XPath selectors for Flutter web elements
 */
public class LoginPage {

    private WebDriver driver;

    // XPath Locators - Flutter Web compatible
    private By emailField = By
            .xpath("//input[@type='email' or contains(@class, 'flt-text-editing') or contains(@placeholder, 'email')]");
    private By passwordField = By.xpath(
            "//input[@type='password' or contains(@placeholder, 'password') or contains(@placeholder, 'Password')]");
    private By loginButton = By
            .xpath("//button[contains(text(), 'Login') or contains(., 'Connexion') or contains(., 'Se connecter')]");
    private By errorMessage = By.xpath(
            "//*[contains(text(), 'invalide') or contains(text(), 'incorrect') or contains(text(), 'Invalid') or contains(@class, 'error')]");
    private By requiredFieldError = By.xpath("//*[contains(text(), 'requis') or contains(text(), 'required')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Navigate to login page
     */
    public void navigateToLogin(String url) {
        driver.get(url);
    }

    /**
     * Enter email address
     */
    public void enterEmail(String email) {
        List<WebElement> emailFields = driver.findElements(emailField);
        if (!emailFields.isEmpty()) {
            WebElement field = emailFields.get(0);
            field.clear();
            field.sendKeys(email);
        }
    }

    /**
     * Enter password
     */
    public void enterPassword(String password) {
        List<WebElement> passwordFields = driver.findElements(passwordField);
        if (!passwordFields.isEmpty()) {
            WebElement field = passwordFields.get(0);
            field.clear();
            field.sendKeys(password);
        }
    }

    /**
     * Click login button
     */
    public void clickLogin() {
        WebElement btn = driver.findElement(loginButton);
        btn.click();
    }

    /**
     * Complete login flow
     */
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        return driver.findElements(errorMessage).size() > 0;
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        List<WebElement> errors = driver.findElements(errorMessage);
        return errors.isEmpty() ? "" : errors.get(0).getText();
    }

    /**
     * Check if password field has validation error (border color)
     */
    public boolean isPasswordFieldInvalid() {
        List<WebElement> pwdFields = driver.findElements(passwordField);
        if (!pwdFields.isEmpty()) {
            WebElement field = pwdFields.get(0);
            String style = field.getAttribute("style");
            String className = field.getAttribute("class");
            return (style != null && (style.contains("border") && style.contains("red"))) ||
                    (className != null && className.contains("error"));
        }
        return false;
    }

    /**
     * Check if login button is disabled
     */
    public boolean isLoginButtonDisabled() {
        try {
            WebElement btn = driver.findElement(loginButton);
            String disabled = btn.getAttribute("disabled");
            boolean enabled = btn.isEnabled();
            return !enabled || disabled != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if required field error is shown
     */
    public boolean areRequiredFieldErrorsShown() {
        return driver.findElements(requiredFieldError).size() > 0;
    }

    /**
     * Get current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
