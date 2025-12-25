package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.time.Duration; // Added
import org.openqa.selenium.JavascriptExecutor; // Added
import org.openqa.selenium.interactions.Actions; // Added
import org.openqa.selenium.support.ui.ExpectedConditions; // Added
import org.openqa.selenium.support.ui.WebDriverWait; // Added

/**
 * Page Object for Patients Management Page
 */
public class PatientsPage {

    private WebDriver driver;

    // Hybrid Strategy: User ID (Primary) | FAB Strategy (Fallback)
    // "apres node-26 et apres node-70..." -> 70 is Add Button
    // FAB Strategy:
    // (//flt-semantics[@role='button'][not(@aria-label)][not(text())])[last()]
    private By addPatientButton = By.xpath(
            "//*[@id='flt-semantic-node-70'] | (//flt-semantics[@role='button'][not(@aria-label)][not(text())])[last()]");
    private By returnToDashboardButton = By.xpath("//*[@id='flt-semantic-node-48']");

    // Form Fields
    private By fullNameField = By.xpath("//*[@id='flt-semantic-node-77']/input");
    private By ageField = By.xpath("//*[@id='flt-semantic-node-78']/input");

    // Gender (92=Male, 93=Female)
    private By maleGender = By.xpath("//*[@id='flt-semantic-node-92']");
    private By femaleGender = By.xpath("//*[@id='flt-semantic-node-93']");

    // Status (80=Dropdown?, 99=Active, 100=Discharged)
    private By statusDropdown = By.xpath("//*[@id='flt-semantic-node-80']");
    private By activeStatusOption = By.xpath("//*[@id='flt-semantic-node-99']");
    private By dischargedStatusOption = By.xpath("//*[@id='flt-semantic-node-100']");

    // Save (81=Save Button)
    private By saveButton = By.xpath("//*[@id='flt-semantic-node-81']");

    // List & Search
    private By searchField = By.xpath("//input[@aria-label='Search' or @aria-label='Rechercher']");
    private By patientsList = By.xpath("//flt-semantics[contains(@aria-label, 'Patient')]");
    private By activeStatusBadge = By.xpath("//*[contains(@aria-label, 'Actif')]");

    public PatientsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Debug: Dump HTML source to file
     */
    public void dumpHtmlSource(String filename) {
        try {
            String source = driver.getPageSource();
            java.nio.file.Path path = java.nio.file.Paths.get("target", filename);
            java.nio.file.Files.writeString(path, source);
            System.out.println("DEBUG HTML saved to: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to dump HTML: " + e.getMessage());
        }
    }

    /**
     * Check if patients page is displayed
     * NOTE: URL might stay 'doctor-dashboard', so checking for Add Button (FAB) or
     * Search Field.
     */
    public boolean isPatientsPageDisplayed() {
        try {
            // URL check is unreliable. Check for Add Button with Wait.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // Check if Add Patient button is visible (User ID 70 or Fallback)
            wait.until(ExpectedConditions.visibilityOfElementLocated(addPatientButton));
            return true;
        } catch (Exception e) {
            System.err.println("Patients Page Verification Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Click the Add Patient button
     */
    public void clickAddPatient() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        System.out.println("Attempting to click Add Patient (ID: 70)...");

        // "Refresh content" strategy: Force driver to read source
        System.out.println("Refreshing HTML context...");
        dumpHtmlSource("patients_page_source.html"); // Dump to file
        // System.out.println("DEBUG HTML: " + htmlSource.substring(0,
        // Math.min(htmlSource.length(), 500)));

        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(addPatientButton));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            Thread.sleep(1000);

            try {
                button.click();
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            }

            Thread.sleep(1000); // Wait for form

        } catch (Exception e) {
            System.err.println("Failed to click Add Patient (ID: 70): " + e.getMessage());
            // Retry with explicit refresh? No, user warned about refresh losing state.
            throw new RuntimeException("Could not interact with Add Patient button", e);
        }
        System.out.println("Add Patient action completed.");
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
