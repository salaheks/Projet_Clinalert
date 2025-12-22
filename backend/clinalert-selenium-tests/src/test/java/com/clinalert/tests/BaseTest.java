package com.clinalert.tests;

import com.clinalert.config.TestConfig;
import com.clinalert.config.WebDriverConfig;
import com.clinalert.utils.ScreenshotUtil;
import com.clinalert.utils.WaitHelper;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Base Test class for all Selenium tests
 * Handles driver setup, teardown, and screenshot capture
 */
public class BaseTest {

    protected WebDriver driver;
    protected WaitHelper waitHelper;
    protected ScreenshotUtil screenshotUtil;

    @BeforeClass
    public void setUpClass() {
        System.out.println("========================================");
        System.out.println("Starting Test Suite: " + this.getClass().getSimpleName());
        System.out.println("========================================");
    }

    @BeforeMethod
    public void setUp(Method method) {
        System.out.println("\n▶ Starting test: " + method.getName());

        // Get WebDriver instance
        driver = WebDriverConfig.getDriver();

        // Initialize helpers
        waitHelper = new WaitHelper(driver, TestConfig.DEFAULT_TIMEOUT);
        screenshotUtil = new ScreenshotUtil(driver);

        // Navigate to login page
        driver.get(TestConfig.LOGIN_URL);

        // Wait for page load
        waitHelper.hardWait(1000);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        // Capture screenshot on failure
        if (result.getStatus() == ITestResult.FAILURE) {
            System.err.println("✗ Test FAILED: " + result.getName());
            screenshotUtil.captureFailureScreenshot(result.getName());

            // Add failure details to Allure
            Allure.addAttachment("Failure Reason", result.getThrowable().toString());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            System.out.println("✓ Test PASSED: " + result.getName());
        } else if (result.getStatus() == ITestResult.SKIP) {
            System.out.println("⊘ Test SKIPPED: " + result.getName());
        }

        // Don't quit driver yet - will quit in @AfterClass
    }

    @AfterClass
    public void tearDownClass() {
        // Quit driver after all tests in class
        WebDriverConfig.quitDriver();
        System.out.println("\n========================================");
        System.out.println("Finished Test Suite: " + this.getClass().getSimpleName());
        System.out.println("========================================\n");
    }

    /**
     * Helper method to add Gherkin-style log to Allure
     */
    protected void logStep(String step) {
        System.out.println("  " + step);
        Allure.step(step);
    }
}
