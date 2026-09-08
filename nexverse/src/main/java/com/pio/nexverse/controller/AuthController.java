package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.AuthService;
import com.pio.nexverse.utils.CookieUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.pio.nexverse.constants.AppConstants.REFRESH_TOKEN_COOKIE;
import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @Value("${cookie.refreshToken.maxAgeInDays}")
    private Integer refreshTokenMaxAgeInDays;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<Void>> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login request received for email = {}", request.getEmail());
        TokenPair tokenPair = authService.login(request);
        log.info("Login successful for email = {}", request.getEmail());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(LOGIN_SUCCESS)
                .build();
        return createAuthenticationResponse(response, tokenPair, refreshTokenMaxAgeInDays);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO<Void>> refreshToken(@CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken) {
        log.info("Refresh token request received.");
        TokenPair tokenPair = authService.refreshToken(refreshToken);
        log.info("Access token refreshed successfully.");
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(TOKEN_REFRESH_SUCCESS)
                .build();
        return createAuthenticationResponse(response, tokenPair, refreshTokenMaxAgeInDays);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken) {
        log.info("Logout request received.");
        authService.logout(refreshToken);
        log.info("User logged out successfully.");
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(LOGOUT_SUCCESS)
                .build();
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("")
                .refreshToken("")
                .build();
        return createAuthenticationResponse(response, tokenPair, 0);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequest) {
        log.info("Password reset requested for email = {}", forgotPasswordRequest.getEmail());
        authService.forgotPassword(forgotPasswordRequest.getEmail());
        log.info("Password reset email processed for email = {}", forgotPasswordRequest.getEmail());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(PASSWORD_RESET_LINK_SENT)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(@Valid @RequestBody SetPasswordRequestDTO request) {
        log.info("Password reset request received.");
        authService.setPassword(request.getNewPassword(), request.getToken());
        log.info("Password reset successfully.");
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(PASSWORD_RESET_SUCCESS)
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate-account")
    public ResponseEntity<ApiResponseDTO<Void>> activateAccount(@Valid @RequestBody SetPasswordRequestDTO request) {
        log.info("Account activation request received.");
        authService.setPassword(request.getNewPassword(), request.getToken());
        log.info("Account activated successfully.");
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(ACCOUNT_ACTIVATE_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<ApiResponseDTO<Void>> createAuthenticationResponse(ApiResponseDTO<Void> response, TokenPair tokenPair, Integer refreshTokenMaxAgeInDays) {
        ResponseCookie accessTokenCookie = CookieUtils.addAccessTokenCookie(tokenPair.getAccessToken());
        ResponseCookie refreshTokenCookie = CookieUtils.addRefreshTokenCookie(tokenPair.getRefreshToken(), refreshTokenMaxAgeInDays);
        log.debug("Authentication cookies created.");
        return ResponseEntity
                .status(200)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }
}