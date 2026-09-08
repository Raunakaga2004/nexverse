package com.pio.nexverse.utils;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static com.pio.nexverse.constants.AppConstants.*;

public final class CookieUtils {
    private CookieUtils() {}

    public static ResponseCookie addAccessTokenCookie(String token) {
        return createTokenCookie(ACCESS_TOKEN_COOKIE, token, null, "/");
    }

    public static ResponseCookie addRefreshTokenCookie(String token, Integer maxAgeInDays) {
        return createTokenCookie(REFRESH_TOKEN_COOKIE, token, maxAgeInDays, "/api/v1/auth");
    }

    private static ResponseCookie createTokenCookie(String tokenName, String token, Integer maxAgeInDays, String path) {
        ResponseCookie.ResponseCookieBuilder tokenCookieBuilder = ResponseCookie
                .from(tokenName, token)
                .httpOnly(true)
                .sameSite(STRICT_COOKIE_SAME_SITE)
                .path(path);
        if (maxAgeInDays != null) {
            tokenCookieBuilder.maxAge(Duration.ofDays(maxAgeInDays));
        }
        return tokenCookieBuilder.build();
    }
}