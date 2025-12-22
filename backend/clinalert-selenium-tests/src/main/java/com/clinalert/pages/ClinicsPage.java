package com.clinalert.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Clinics Management Page
 */
public class ClinicsPage {

    private WebDriver driver;

    // XPath Locators
    private By pageTitle = By.xpath("//*[contains(text(), 'Cliniques') or contains(text(), 'Clinics')]");
    private By createClinicButton = By
            .xpath("//button[contains(text(), 'Créer') or contains(text(), 'Create') or contains(text(), 'Ajouter')]");
    private By clinicNameField = By.xpath(
            "//input[contains(@placeholder, 'nom') or contains(@name, 'name') or contains(@placeholder, 'Name')]");
    private By addressField = By.xpath(
            "//input[contains(@placeholder, 'adresse') or contains(@name, 'address') or contains(@placeholder, 'Address')]");
    private By phoneField = By
            .xpath("//input[@type='tel' or contains(@placeholder, 'téléphone') or contains(@placeholder, 'phone')]");
    private By saveButton = By.xpath(
            "//button[contains(text(), 'Créer la clinique') or contains(text(), 'Enregistrer') or contains(text(), 'Save')]");
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
     * Complete clinic creation flow
     */
    public void createClinic(String name, String address, String phone) {
        clickCreateClinic();
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
