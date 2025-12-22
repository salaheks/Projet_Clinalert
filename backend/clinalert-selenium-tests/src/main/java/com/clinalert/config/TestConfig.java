package com.clinalert.config;

/**
 * Application configuration constants
 */
public class TestConfig {

    // Application URLs
    public static final String BASE_URL = "http://localhost:57056";
    public static final String LOGIN_URL = BASE_URL + "/#/login";
    public static final String DOCTOR_DASHBOARD_URL = BASE_URL + "/#/doctor-dashboard";
    public static final String PATIENT_DASHBOARD_URL = BASE_URL + "/#/patient-dashboard";
    public static final String PATIENTS_URL = BASE_URL + "/#/patients";
    public static final String CLINICS_URL = BASE_URL + "/#/clinics";
    public static final String ALERTS_URL = BASE_URL + "/#/alerts";

    // Test Users
    public static final String DOCTOR_EMAIL = "house@clinalert.com";
    public static final String DOCTOR_PASSWORD = "doctor123";
    public static final String DOCTOR_NAME = "Gregory House";

    public static final String PATIENT_EMAIL = "john.doe@clinalert.com";
    public static final String PATIENT_PASSWORD = "patient123";
    public static final String PATIENT_NAME = "John Doe";

    public static final String ADMIN_EMAIL = "admin@clinalert.com";
    public static final String ADMIN_PASSWORD = "admin123";

    // Timeouts (in seconds)
    public static final int DEFAULT_TIMEOUT = 10;
    public static final int LONG_TIMEOUT = 30;
    public static final int SHORT_TIMEOUT = 5;

    // Screenshot directory
    public static final String SCREENSHOT_DIR = "target/screenshots/";

    // Test data
    public static final String TEST_PATIENT_NAME = "Test Patient Selenium";
    public static final String TEST_PATIENT_AGE = "34";

    public static final String TEST_CLINIC_NAME = "Clinique Selenium";
    public static final String TEST_CLINIC_ADDRESS = "123 Rue de Test";
    public static final String TEST_CLINIC_PHONE = "0102030405";
}
