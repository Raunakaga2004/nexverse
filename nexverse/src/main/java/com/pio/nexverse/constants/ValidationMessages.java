package com.pio.nexverse.constants;

public final class ValidationMessages {
    private ValidationMessages() {}

    // DTO Validation
    public static final String ACTIVATION_UPDATE_FIELD = "Activation field is required.";
    public static final String EMAIL_REQUIRED = "Email is required.";
    public static final String VALID_EMAIL = "Please provide a valid email address.";
    public static final String VALID_PHONE_NUMBER = "Please provide a valid phone number.";
    public static final String DEPARTMENT_NAME_REQUIRED = "Department name is required.";
    public static final String ORGANIZATION_NAME_REQUIRED = "Organization name is required.";
    public static final String ORG_EMAIL_REQUIRED = "Organization contact email is required.";
    public static final String ORG_ADMIN_EMAIL_REQUIRED = "Organization admin email is required.";
    public static final String VALID_ORG_EMAIL = "Please provide a valid email address of organization.";
    public static final String VALID_ORG_ADMIN_EMAIL = "Please provide a valid email address of organization admin";
    public static final String VALID_ORG_PHONE_NUMBER = "Please provide a valid phone number of organization.";
    public static final String VALID_ORG_ADMIN_PHONE_NUMBER = "Please provide a valid phone number of organization admin";
    public static final String PASSWORD_REQUIRED = "Password is required.";
    public static final String OLD_PASSWORD_REQUIRED = "Old password is required.";
    public static final String NEW_PASSWORD_REQUIRED = "New password is required.";
    public static final String TOKEN_REQUIRED = "Token is required.";
    public static final String INVALID_PASSWORD = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, must be at least 8 characters long.";
    public static final String EMPLOYEE_FIRST_NAME_REQUIRED = "First name of employee is required.";
    public static final String ORG_ADMIN_FIRST_NAME_REQUIRED = "First name of organization admin is required.";
    public static final String SKILL_NAME_REQUIRED = "Name of skill is required.";
}