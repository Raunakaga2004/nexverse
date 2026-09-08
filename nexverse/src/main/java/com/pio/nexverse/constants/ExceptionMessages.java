package com.pio.nexverse.constants;

public final class ExceptionMessages {
    private ExceptionMessages(){}

    public static final String ACCESS_DENIED = "You do not have permission to access this resource.";
    public static final String AUTHENTICATION_FAILED = "Authentication is required to access this resource.";
    public static final String USER_ACCOUNT_DISABLED = "User account is disabled.";
    public static final String ORGANIZATION_DISABLED = "Organization is disabled.";
    public static final String DEPARTMENT_DISABLED = "Department is disabled.";
    public static final String DEPARTMENT_NOT_FOUND = "Department not found.";
    public static final String ORGANIZATION_NOT_FOUND = "Organization not found.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String INCORRECT_PASSWORD = "Password entered is incorrect";
    public static final String INVALID_CREDENTIALS = "The email or password you entered is incorrect.";
    public static final String INVALID_SET_PASS_TOKEN = "Link is invalid or expired.";
    public static final String ORGANIZATION_SUSPENDED = "Your Organization has been suspended. Please contact your administrator.";
    public static final String USER_ACCOUNT_SUSPENDED = "Your account has been suspended. Please contact your administrator.";
    public static final String PASSWORD_REUSE = "New password must be different from the current password";
    public static final String USER_EMAIL_EXIST = "User's email already exists.";
    public static final String USER_PHONE_NUMBER_EXIST = "User's phone number already exists.";
    public static final String USER_EMP_CODE_EXIST = "User's employee code in organization already exists.";
    public static final String ORG_EMAIL_EXIST = "Organization's email already exists.";
    public static final String ORG_PHONE_EXIST = "Organization's phone number already exists.";
    public static final String DEP_NAME_EXIST = "Department's name already exists in your organization.";
    public static final String EMAIL_SENT_ERROR = "Failed to send email to: ";
    public static final String SERVER_ERROR = "Unexpected error occurred.";
    public static final String PROFILE_IMAGE_FILE_TYPE_ERROR = "Profile image must be JPG or JPEG.";
    public static final String FILE_STORAGE_ERROR = "Failed to upload or get file.";
    public static final String EXCEL_FILE_ERROR = "Failed to read excel file.";
    public static final String DRAFT_COURSE = "The course is currently in Draft status.";
    public static final String COURSE_MUST_BE_DRAFT = "Course must be in Draft status to perform this action.";
    public static final String SKILL_NAME_EXIST = "Skill name already exists in your organization.";
    public static final String ARCHIVED_COURSE = "The course is currently in Archived state";
    public static final String CONTENT_NOT_FOUND = "Content not found.";
    public static final String COURSE_ACCESSIBLE = "Course is already accessible.";
    public static final String MODULE_NOT_FOUND = "Module not found.";
    public static final String COURSE_NOT_ACCESSIBLE = "Course is not accessible.";
    public static final String COURSE_NOT_ENROLLED= "Course is not enrolled.";
    public static final String COURSE_NOT_FOUND = "Course not found.";
    public static final String COURSE_REQUEST_NOT_FOUND = "No course request found.";
    public static final String COURSE_THUMBNAIL_NOT_FOUND = "Course thumbnail not found.";
    public static final String DEPARTMENT_ARCHIVED = "Department is archived. Please contact your organization administrator.";
    public static final String ORGANIZATION_PENDING = "Organization id currently in pending status.";
    public static final String ALREADY_PUBLISHED_COURSE = "Course is already published.";
    public static final String SKILL_ICON_NOT_FOUND = "Skill icon not found.";
    public static final String SKILL_NOT_FOUND = "Skill not found.";
    public static final String USER_NOT_SUSPENDED = "To reactivate employee account, employee account status must be suspended.";
    public static final String USER_PENDING = "User account is currently in pending state.";
}