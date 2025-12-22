package com.clinalert.utils;

import com.clinalert.config.TestConfig;
import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing screenshots
 */
public class ScreenshotUtil {

    private WebDriver driver;
    private String screenshotDir;

    public ScreenshotUtil(WebDriver driver) {
        this.driver = driver;
        this.screenshotDir = TestConfig.SCREENSHOT_DIR;

        // Create screenshot directory if it doesn't exist
        File dir = new File(screenshotDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Capture screenshot with given name
     * 
     * @param name Screenshot name (without extension)
     * @return Path to saved screenshot
     */
    public String captureScreenshot(String name) {
        try {
            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = name + "_" + timestamp + ".png";
            String filePath = screenshotDir + fileName;

            // Take screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File(filePath);

            // Copy to destination
            FileUtils.copyFile(screenshot, destination);

            // Attach to Allure report
            try (FileInputStream fis = new FileInputStream(destination)) {
                Allure.addAttachment(name, "image/png", fis, "png");
            }

            System.out.println("✓ Screenshot captured: " + fileName);
            return filePath;

        } catch (Exception e) {
            System.err.println("✗ Error capturing screenshot: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Capture screenshot on test failure
     * 
     * @param testName Name of failed test
     */
    public void captureFailureScreenshot(String testName) {
        captureScreenshot("FAILURE_" + testName);
    }
}
