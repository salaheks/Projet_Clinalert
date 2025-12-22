package com.clinalert.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Utility class for explicit waits
 */
public class WaitHelper {

    private WebDriver driver;
    private WebDriverWait wait;

    public WaitHelper(WebDriver driver, int timeoutInSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    /**
     * Wait for element to be visible
     */
    public WebElement waitForElementVisible(By locator, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be clickable
     */
    public WebElement waitForElementClickable(By locator, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for URL to contain specific string
     */
    public boolean waitForUrlContains(String urlPart, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.urlContains(urlPart));
    }

    /**
     * Wait for URL to be specific value
     */
    public boolean waitForUrlToBe(String url, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.urlToBe(url));
    }

    /**
     * Wait for element to disappear
     */
    public boolean waitForElementInvisible(By locator, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for text to be present in element
     */
    public boolean waitForTextInElement(By locator, String text, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return customWait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Hard wait (use sparingly)
     */
    public void hardWait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
