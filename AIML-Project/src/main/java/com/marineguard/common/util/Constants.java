package com.marineguard.common.util;

public class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // API Base Paths
    public static final String API_BASE = "/api";
    public static final String API_AUTH = API_BASE + "/auth";
    public static final String API_USERS = API_BASE + "/users";
    public static final String API_OBSERVATIONS = API_BASE + "/observations";
    public static final String API_ALERTS = API_BASE + "/alerts";
    public static final String API_LIBRARY = API_BASE + "/library";
    public static final String API_ADMIN_LIBRARY = API_BASE + "/admin/library";
    public static final String API_REPORTS = API_BASE + "/reports";
    public static final String API_TASKS = API_BASE + "/tasks";

    // User Roles
    public static final String ROLE_PUBLIC = "PUBLIC";
    public static final String ROLE_NGO = "NGO";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DIVE_OPERATOR = "DIVE_OPERATOR";
    public static final String ROLE_MARINE_BIOLOGIST = "MARINE_BIOLOGIST";

    // Observation Status
    public static final String OBSERVATION_STATUS_PENDING = "PENDING";
    public static final String OBSERVATION_STATUS_APPROVED = "APPROVED";
    public static final String OBSERVATION_STATUS_REJECTED = "REJECTED";
    public static final String OBSERVATION_STATUS_DRAFT = "DRAFT";
    public static final String OBSERVATION_STATUS_WITHDRAWN = "WITHDRAWN";

    // Alert Priorities
    public static final String ALERT_PRIORITY_LOW = "LOW";
    public static final String ALERT_PRIORITY_MEDIUM = "MEDIUM";
    public static final String ALERT_PRIORITY_HIGH = "HIGH";
    public static final String ALERT_PRIORITY_URGENT = "URGENT";

    // Alert Types
    public static final String ALERT_TYPE_BLEACHING = "BLEACHING";
    public static final String ALERT_TYPE_DISEASE = "DISEASE";
    public static final String ALERT_TYPE_SYSTEM = "SYSTEM";

    // Alert Status
    public static final String ALERT_STATUS_ACTIVE = "ACTIVE";
    public static final String ALERT_STATUS_ARCHIVED = "ARCHIVED";

    // Task Status
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String TASK_STATUS_COMPLETED = "COMPLETED";
    public static final String TASK_STATUS_CANCELLED = "CANCELLED";

    // Task Priority
    public static final String TASK_PRIORITY_LOW = "LOW";
    public static final String TASK_PRIORITY_MEDIUM = "MEDIUM";
    public static final String TASK_PRIORITY_HIGH = "HIGH";
    public static final String TASK_PRIORITY_CRITICAL = "CRITICAL";

    // Library Item Types
    public static final String LIBRARY_TYPE_PDF = "PDF";
    public static final String LIBRARY_TYPE_ARTICLE = "ARTICLE";
    public static final String LIBRARY_TYPE_VIDEO = "VIDEO";
    public static final String LIBRARY_TYPE_IMAGE = "IMAGE";
    public static final String LIBRARY_TYPE_GUIDE = "GUIDE";
    public static final String LIBRARY_TYPE_FAQ = "FAQ";

    // Library Categories
    public static final String LIBRARY_CAT_BLEACHING = "BLEACHING";
    public static final String LIBRARY_CAT_DISEASE = "DISEASE";
    public static final String LIBRARY_CAT_CONSERVATION = "CONSERVATION";
    public static final String LIBRARY_CAT_IDENTIFICATION = "IDENTIFICATION";
    public static final String LIBRARY_CAT_RESTORATION = "RESTORATION";

    // AI Prediction Labels
    public static final String AI_LABEL_HEALTHY = "HEALTHY";
    public static final String AI_LABEL_EARLY_BLEACHING = "EARLY_BLEACHING";
    public static final String AI_LABEL_MODERATE_BLEACHING = "MODERATE_BLEACHING";
    public static final String AI_LABEL_SEVERE_BLEACHING = "SEVERE_BLEACHING";
    public static final String AI_LABEL_DISEASED = "DISEASED";
    public static final String AI_LABEL_UNKNOWN = "UNKNOWN";

    // Severity Levels (0-4)
    public static final int SEVERITY_NONE = 0;
    public static final int SEVERITY_MILD = 1;
    public static final int SEVERITY_MODERATE = 2;
    public static final int SEVERITY_SEVERE = 3;
    public static final int SEVERITY_CRITICAL = 4;

    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String UPLOAD_DIR = "uploads/observations/";
    public static final String LIBRARY_UPLOAD_DIR = "uploads/library/";
    public static final String ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/jpg";
    public static final String ALLOWED_DOC_TYPES = "application/pdf";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // JWT
    public static final String JWT_SECRET = "${jwt.secret}"; // From properties
    public static final long JWT_EXPIRATION = 86400000; // 24 hours in milliseconds

    // MongoDB Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_OBSERVATIONS = "observations";
    public static final String COLLECTION_ALERTS = "alerts";
    public static final String COLLECTION_LIBRARY = "library";
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_TASKS = "tasks";

    // Regex Patterns
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    public static final String PHONE_PATTERN = "^\\+?[0-9]{10,13}$";

    // Success Messages
    public static final String MSG_USER_REGISTERED = "User registered successfully";
    public static final String MSG_LOGIN_SUCCESS = "Login successful";
    public static final String MSG_PASSWORD_RESET = "Password reset email sent";
    public static final String MSG_OBSERVATION_UPLOADED = "Observation uploaded successfully";
    public static final String MSG_ALERT_CREATED = "Alert created successfully";
    public static final String MSG_TASK_CREATED = "Task created successfully";
    public static final String MSG_REPORT_GENERATED = "Report generated successfully";

    // Error Messages
    public static final String ERR_USER_NOT_FOUND = "User not found";
    public static final String ERR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERR_EMAIL_EXISTS = "Email already exists";
    public static final String ERR_OBSERVATION_NOT_FOUND = "Observation not found";
    public static final String ERR_UNAUTHORIZED = "You are not authorized to perform this action";
    public static final String ERR_INVALID_FILE_TYPE = "Invalid file type";
    public static final String ERR_FILE_TOO_LARGE = "File size exceeds maximum limit";
}