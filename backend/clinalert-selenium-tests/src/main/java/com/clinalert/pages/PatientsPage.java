package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Patients Management Page
 */
public class PatientsPage {

    private WebDriver driver;

    // XPath Locators
    private By pageTitle = By.xpath("//*[contains(text(), 'Patients') or contains(text(), 'Tous les patients')]");
    private By addPatientButton = By
            .xpath("//button[contains(text(), 'Ajouter') or contains(., '+') or contains(@aria-label, 'Add')]");
    private By fullNameField = By.xpath(
            "//input[contains(@placeholder, 'nom') or contains(@name, 'fullName') or contains(@placeholder, 'Name')]");
    private By ageField = By
            .xpath("//input[@type='number' or contains(@placeholder, 'age') or contains(@name, 'age')]");
    private By saveButton = By.xpath(
            "//button[contains(text(), 'Ajouter') or contains(text(), 'Enregistrer') or contains(text(), 'Save')]");
    private By cancelButton = By.xpath("//button[contains(text(), 'Annuler') or contains(text(), 'Cancel')]");
    private By patientsList = By.xpath(
            "//div[contains(@class, 'patient-card') or contains(@class, 'list-item') or contains(@class, 'patient-item')]");
    private By searchField = By.xpath(
            "//input[@type='search' or contains(@placeholder, 'Rechercher') or contains(@placeholder, 'Search')]");
    private By activeStatusBadge = By.xpath("//*[contains(text(), 'ACTIVE') or contains(@class, 'status-active')]");

    public PatientsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if patients page is displayed
     */
    public boolean isPatientsPageDisplayed() {
        return driver.getCurrentUrl().contains("patients") &&
                driver.findElements(pageTitle).size() > 0;
    }

    /**
     * Click Add Patient button
     */
    public void clickAddPatient() {
        WebElement btn = driver.findElement(addPatientButton);
        // Scroll into view for Flutter web
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
    }

    /**
     * Enter patient full name
     */
    public void enterFullName(String name) {
        try {
            // Wait for form to appear (animation)
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> nameFields = driver.findElements(fullNameField);
        if (!nameFields.isEmpty()) {
            WebElement field = nameFields.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(nameFields.get(0));
            field.clear();
            field.sendKeys(name);
        }
    }

    /**
     * Enter patient age
     */
    public void enterAge(String age) {
        List<WebElement> ageFields = driver.findElements(ageField);
        if (!ageFields.isEmpty()) {
            WebElement field = ageFields.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(ageFields.get(0));
            field.clear();
            field.sendKeys(age);
        }
    }

    /**
     * Click Save button
     */
    public void clickSave() {
        List<WebElement> saveButtons = driver.findElements(saveButton);
        if (!saveButtons.isEmpty()) {
            WebElement btn = saveButtons.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(saveButtons.get(0));
            btn.click();
        }
    }

    /**
     * Complete patient creation flow
     */
    public void createPatient(String name, String age) {
        clickAddPatient();
        enterFullName(name);
        enterAge(age);
        clickSave();

        // Wait for patient to be added (API call + UI update)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if patient is in the list by name
     */
    public boolean isPatientInList(String name) {
        // XPath to find any element containing the patient name
        By patientByName = By.xpath("//*[contains(text(), '" + name + "')]");
        return driver.findElements(patientByName).size() > 0;
    }

    /**
     * Get count of patients in list
     */
    public int getPatientsCount() {
        return driver.findElements(patientsList).size();
    }

    /**
     * Search for patient
     */
    public void searchPatient(String searchTerm) {
        List<WebElement> searchFields = driver.findElements(searchField);
        if (!searchFields.isEmpty()) {
            WebElement field = searchFields.get(0);
            field.clear();
            field.sendKeys(searchTerm);
        }
    }

    /**
     * Check if ACTIVE status badge is visible for a patient
     */
    public boolean isActiveStatusVisible() {
        return driver.findElements(activeStatusBadge).size() > 0;
    }
}
