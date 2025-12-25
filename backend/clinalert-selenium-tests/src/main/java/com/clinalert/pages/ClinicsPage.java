package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

/**
 * Page Object for Clinics Management Page
 */
public class ClinicsPage {

    private WebDriver driver;

    // XPath Locators
    private By pageTitle = By.xpath("//*[contains(text(), 'Cliniques') or contains(text(), 'Clinics')]");
    // XPath Locators - Explicit Semantic IDs
    // User-Provided Explicit IDs
    // "apres node-27... on click node-116"
    private By createClinicButton = By.xpath("//*[@id='flt-semantic-node-116']");
    private By returnToDashboardButton = By.xpath("//*[@id='flt-semantic-node-48']");

    // Form Fields
    private By clinicNameField = By.xpath("//*[@id='flt-semantic-node-125']/input");
    private By addressField = By.xpath("//*[@id='flt-semantic-node-127']/textarea");
    private By phoneField = By.xpath("//*[@id='flt-semantic-node-129']/input");

    // Save
    // "et apres on click //*[@id='flt-semantic-node-183'] pour ajoutter un
    // clinique"
    private By saveButton = By.xpath("//*[@id='flt-semantic-node-183']");

    // List & Search (Generic or IDs if provided, keeping generic for flexibility)
    private By successBanner = By.xpath(
            "//*[contains(text(), 'succès') or contains(text(), 'success') or contains(@class, 'success-message')]");
    private By clinicsList = By.xpath("//div[contains(@class, 'clinic-card') or contains(@class, 'clinic-item')]");
    private By noClinicMessage = By.xpath("//*[contains(text(), 'Aucune clinique') or contains(text(), 'No clinics')]");

    public ClinicsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Check if clinics page is displayed
     */
    public boolean isClinicsPageDisplayed() {
        return driver.getCurrentUrl().contains("clinics") ||
                driver.findElements(pageTitle).size() > 0;
    }

    /**
     * Click Create Clinic button
     */
    public void clickCreateClinic() {
        List<WebElement> buttons = driver.findElements(createClinicButton);
        if (!buttons.isEmpty()) {
            WebElement btn = buttons.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            btn.click();
        }
    }

    /**
     * Enter clinic name
     */
    public void enterClinicName(String name) {
        try {
            // Wait for form animation
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> nameFields = driver.findElements(clinicNameField);
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
     * Enter clinic address
     */
    public void enterAddress(String address) {
        List<WebElement> addressFields = driver.findElements(addressField);
        if (!addressFields.isEmpty()) {
            WebElement field = addressFields.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(addressFields.get(0));
            field.clear();
            field.sendKeys(address);
        }
    }

    /**
     * Enter clinic phone
     */
    public void enterPhone(String phone) {
        List<WebElement> phoneFields = driver.findElements(phoneField);
        if (!phoneFields.isEmpty()) {
            WebElement field = phoneFields.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(phoneFields.get(0));
            field.clear();
            field.sendKeys(phone);
        }
    }

    /**
     * Click Save/Create button
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
     * Create a new clinic
     */
    public void createClinic(String name, String address, String phone) {
        try {
            // Robust click for Add Clinic
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(createClinicButton));

            WebElement btn = driver.findElement(createClinicButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            Thread.sleep(500);

            try {
                btn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
        } catch (Exception e) {
            System.out.println("Failed to click create clinic button: " + e.getMessage());
        }

        enterClinicName(name);
        enterAddress(address);
        enterPhone(phone);
        clickSave();

        // Wait for clinic creation (API + UI update)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if success banner is displayed
     */
    public boolean isSuccessBannerDisplayed() {
        return driver.findElements(successBanner).size() > 0;
    }

    /**
     * Check if clinic is in the list by name
     */
    public boolean isClinicInList(String name) {
        By clinicByName = By.xpath("//*[contains(text(), '" + name + "')]");
        return driver.findElements(clinicByName).size() > 0;
    }

    /**
     * Get clinics count
     */
    public int getClinicsCount() {
        return driver.findElements(clinicsList).size();
    }

    /**
     * Check if "No clinics" message is displayed
     */
    public boolean isNoClinicMessageDisplayed() {
        return driver.findElements(noClinicMessage).size() > 0;
    }
}
