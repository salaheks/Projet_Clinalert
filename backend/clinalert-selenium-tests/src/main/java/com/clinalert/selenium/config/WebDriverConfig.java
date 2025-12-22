package com.clinalert.selenium.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * WebDriver Configuration and Factory
 * Manages WebDriver instances with auto-download via WebDriverManager
 */
public class WebDriverConfig {

    private static final String BROWSER = System.getProperty("browser", "chrome");
    private static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "false"));
    private static final int IMPLICIT_WAIT = Integer.parseInt(System.getProperty("implicit.wait", "10"));

    /**
     * Create and configure WebDriver instance
     */
    public static WebDriver createDriver() {
        WebDriver driver;

        switch (BROWSER.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (HEADLESS) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (HEADLESS) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                driver = new ChromeDriver(chromeOptions);
                break;
        }

        // Configure implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT));

        // Maximize window
        driver.manage().window().maximize();

        return driver;
    }
}
