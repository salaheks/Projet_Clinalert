package com.clinalert.doctortracker.util;

public final class AppConstants {

    // Prevent instantiation
    private AppConstants() {
    }

    // Measurement Types
    public static final String MEASUREMENT_TYPE_HEART_RATE = "heart_rate";
    public static final String MEASUREMENT_TYPE_TEMPERATURE = "temperature";
    public static final String MEASUREMENT_TYPE_BLOOD_PRESSURE = "blood_pressure_systolic";
    public static final String MEASUREMENT_TYPE_OXYGEN_SATURATION = "oxygen_saturation";

    // Security
    public static final String HMAC_ALGORITHM = "HmacSHA256";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_PATIENT = "PATIENT";

    // Alert Severity
    public static final String ALERT_SEVERITY_HIGH = "HIGH";
    public static final String ALERT_SEVERITY_MEDIUM = "MEDIUM";
    public static final String ALERT_SEVERITY_CRITICAL = "CRITICAL";

    // Status
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_TRANSFERRED = "transferred";
    public static final String STATUS_DISCHARGED = "discharged";

    // Keys
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ERROR = "error";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_ID = "id";
    public static final String KEY_ROLE = "role";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_PHONE = "phone";

    // Error Messages
    public static final String ERROR_DEVICE_ID_NULL = "deviceId must not be null";
    public static final String ERROR_HEALTH_DATA_NULL = "healthDataList must not be null";
    public static final String ERROR_USER_NOT_FOUND_PREFIX = "User not found: ";
}
