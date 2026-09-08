package com.pio.nexverse.constants;

public final class ErrorResponseCode {
    private ErrorResponseCode() {}

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String PASSWORD_ALREADY_USED = "PASSWORD_ALREADY_USED";
    public static final String MISSING_REQUEST_COOKIE = "MISSING_REQUEST_COOKIE";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String EMAIL_DELIVERY_FAILED = "EMAIL_DELIVERY_FAILED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String USER_ACCOUNT_DISABLED = "USER_ACCOUNT_DISABLED";
    public static final String ORGANIZATION_DISABLED = "ORGANIZATION_DISABLED";
    public static final String DEPARTMENT_DISABLED = "DEPARTMENT_DISABLED";
   public static final String SUSPENDED = "SUSPENDED";
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String INVALID_COURSE_STATUS = "INVALID_COURSE_STATUS";
    public static final String COURSE_ALREADY_ACCESSIBLE = "COURSE_ALREADY_ACCESSIBLE";
    public static final String COURSE_NOT_ACCESSIBLE = "COURSE_NOT_ACCESSIBLE";
    public static final String FILE_SIZE_LIMIT_EXCEEDED = "FILE_SIZE_LIMIT_EXCEEDED";
    public static final String FAILED_STORING_FILE = "FAILED_STORING_FILE";
    public static final String INCORRECT_PASSWORD = "INCORRECT_PASSWORD";
    public static final String INVALID_ORDER = "INVALID_ORDER";
    public static final String COURSE_PUBLISH_ERROR = "COURSE_PUBLISH_ERROR";
    public static final String INVALID_EXCEL_FILE = "INVALID_EXCEL_FILE";
    public static final String INVALID_CONTENT = "INVALID_CONTENT";
    public static final String PENDING = "PENDING";
}