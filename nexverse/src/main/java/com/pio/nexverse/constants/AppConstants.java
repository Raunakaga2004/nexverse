package com.pio.nexverse.constants;

import java.util.regex.Pattern;

public final class AppConstants {
    private AppConstants() {}

    public static final String STRICT_COOKIE_SAME_SITE = "strict";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";

    // regular expression
    public static final String PHONE_NUMBER_REGEX = "^[6-9]\\d{9}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]).{8,}$";
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // file type
    public static final String PROFILE_IMAGE_CONTENT_TYPE = "image/jpeg";
    public static final String XML_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String ACTIVATE_ACCOUNT_PATH = "/activate-account/";
    public static final String RESET_PASSWORD_PATH = "/reset-password/";
}