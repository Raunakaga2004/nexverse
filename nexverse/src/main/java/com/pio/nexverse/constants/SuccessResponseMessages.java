package com.pio.nexverse.constants;

public final class SuccessResponseMessages {
    private SuccessResponseMessages() {}

    // Authentication Controller
    public static final String LOGIN_SUCCESS = "Logged in successfully.";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully.";
    public static final String LOGOUT_SUCCESS = "Logged out successfully.";
    public static final String PASSWORD_RESET_LINK_SENT = "If the email exists, a password reset link has ben sent.";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully.";
    public static final String ACCOUNT_ACTIVATE_SUCCESS = "Account activated successfully.";

    //User Controller
    public static final String USER_PASSWORD_CHANGED = "Password changed successfully.";
    public static final String USER_DETAILS = "User details fetched successfully.";

    // Organization Controller
    public static final String ORGANIZATION_CREATED_SUCCESS = "Organization created successfully.";
    public static final String ORGANIZATION_UPDATED_SUCCESS = "Organization updated successfully.";
    public static final String ORGANIZATION_SUSPENDED_SUCCESS = "Organization suspended successfully.";
    public static final String ORGANIZATION_REACTIVATE_SUCCESS = "Organization reactivated successfully.";
    public static final String ORGANIZATIONS_RETRIEVED_SUCCESS = "Organizations retrieved successfully.";
    public static final String ORGANIZATION_RETRIEVED_SUCCESS = "Organization retrieved successfully.";
    public static final String ORGANIZATION_DISABLED_SUCCESS = "Organization disabled successfully.";
    public static final String ORGANIZATION_ENABLE_SUCCESS = "Organization enabled successfully.";

    // Department Controller
    public static final String DEPARTMENT_CREATED_SUCCESS = "Department created successfully.";
    public static final String DEPARTMENT_UPDATED_SUCCESS = "Department updated successfully.";
    public static final String DEPARTMENTS_RETRIEVED_SUCCESS = "Departments retrieved successfully.";
    public static final String DEPARTMENT_RETRIEVED_SUCCESS = "Department retrieved successfully.";
    public static final String DEPARTMENT_DISABLED_SUCCESS = "Department disabled successfully.";
    public static final String DEPARTMENT_ENABLED_SUCCESS = "Department enabled successfully.";

    // Employee Controller
    public static final String EMPLOYEE_CREATED_SUCCESS = "Employee created successfully.";
    public static final String EMPLOYEE_UPDATED_SUCCESS = "Employee updated successfully.";
    public static final String EMPLOYEE_SUSPENDED_SUCCESS = "Employee suspended successfully.";
    public static final String EMPLOYEE_REACTIVATED_SUCCESS = "Employee reactivated successfully.";
    public static final String EMPLOYEES_RETRIEVED_SUCCESS = "Employees retrieved successfully.";
    public static final String EMPLOYEE_RETRIEVED_SUCCESS = "Employee retrieved successfully.";
    public static final String EMPLOYEE_DISABLED_SUCCESS = "Employee disabled successfully.";
    public static final String EMPLOYEE_ENABLED_SUCCESS = "Employee enabled successfully.";
    public static final String EMPLOYEES_IMPORTED_SUCCESS = "Employees imported successfully.";
    public static final String EMPLOYEE_LEARNING_PROGRESS_FETCHED = "Employees learning progress fetched successfully.";

    // Course Controller
    public static final String COURSE_CREATED_SUCCESS = "Course created successfully.";
    public static final String COURSE_UPDATED_SUCCESS = "Course updated successfully.";
    public static final String COURSES_RETRIEVED_SUCCESS = "Courses retrieved successfully.";
    public static final String COURSE_RETRIEVED_SUCCESS = "Course retrieved successfully.";
    public static final String DRAFT_COURSE_DELETED_SUCCESS = "Course deleted successfully.";
    public static final String COURSE_PUBLISHED_SUCCESS = "Course published successfully.";
    public static final String COURSE_ARCHIVED_SUCCESS = "Course archived successfully.";
    public static final String COURSE_RESTORED_SUCCESS = "Course restored successfully.";
    public static final String COURSE_ASSIGNED_SUCCESS = "Course assigned to employees successfully.";

    // Course Module Controller
    public static final String COURSE_MODULE_CREATED_SUCCESS = "Course module created successfully.";
    public static final String COURSE_MODULE_UPDATED_SUCCESS = "Course module updated successfully.";
    public static final String COURSE_MODULES_RETRIEVED_SUCCESS = "Course modules retrieved successfully.";
    public static final String COURSE_MODULE_RETRIEVED_SUCCESS = "Course module retrieved successfully.";
    public static final String COURSE_MODULE_DELETED_SUCCESS = "Course module deleted successfully.";
    public static final String COURSE_REORDERED_SUCCESS = "Course module reordered successfully.";

    // Module Content Controller
    public static final String MODULE_CONTENT_CREATED_SUCCESS = "Module Content created successfully.";
    public static final String MODULE_CONTENT_UPDATED_SUCCESS = "Module Content updated successfully.";
    public static final String MODULE_CONTENT_RETRIEVED_SUCCESS = "Module Content retrieved successfully.";
    public static final String MODULE_CONTENT_DELETED_SUCCESS = "Module Content deleted successfully.";
    public static final String MODULE_CONTENTS_REORDERED_SUCCESS = "Module Content reordered successfully.";

    // Skill Controller
    public static final String SKILL_CREATED_SUCCESS = "Skill created successfully.";
    public static final String SKILL_UPDATED_SUCCESS = "Skill updated successfully.";
    public static final String SKILLS_RETRIEVED_SUCCESS = "Skills retrieved successfully.";
    public static final String SKILL_RETRIEVED_SUCCESS = "Skill retrieved successfully.";
    public static final String SKILL_DISABLED_SUCCESS = "Skill disabled successfully.";
    public static final String SKILL_ENABLED_SUCCESS = "Skill enabled successfully.";

    // Dashboard Controller
    public static final String SUPER_ADMIN_DASHBOARD_RETRIEVED = "Super admin dashboard retrieved successfully.";
    public static final String ORG_ADMIN_DASHBOARD_RETRIEVED = "Organization admin dashboard retrieved successfully.";
    public static final String DEP_MANAGER_DASHBOARD_RETRIEVED = "Department manager dashboard retrieved successfully.";
    public static final String EMPLOYEE_DASHBOARD_RETRIEVED = "Employee dashboard retrieved successfully.";

    // Employee Course Controller
    public static final String COURSE_ACCESS_PROCESSED_SUCCESSFULLY = "Course access request processed successfully.";
    public static final String CONTENT_RETRIEVED_SUCCESS = "Content retrieved successfully.";
    public static final String CONTENT_STARTED_SUCCESS = "Content started successfully.";
    public static final String CONTENT_COMPLETED_SUCCESS = "Content completed successfully.";
}