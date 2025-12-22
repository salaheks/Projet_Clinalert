package com.clinalert.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Singleton WebDriver configuration class
 * Manages the lifecycle of ChromeDriver instance
 */
public class WebDriverConfig {

    private static WebDriver driver;

    private WebDriverConfig() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get or create WebDriver instance
     * 
     * @return WebDriver instance
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            // Setup ChromeDriver automatically
            WebDriverManager.chromedriver().setup();

            // Configure Chrome options
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");

            // Uncomment for headless mode
            // options.addArguments("--headless=new");

            // Create ChromeDriver instance
            driver = new ChromeDriver(options);

            // Set implicit wait
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Set page load timeout
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        }
        return driver;
    }

    /**
     * Quit and cleanup WebDriver instance
     */
    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error closing driver: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }

    /**
     * Refresh driver (quit and recreate)
     */
    public static void refreshDriver() {
        quitDriver();
        getDriver();
    }
}
