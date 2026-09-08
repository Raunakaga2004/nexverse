package com.pio.nexverse.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pio.nexverse.constants.ErrorResponseCode;
import com.pio.nexverse.constants.ExceptionMessages;
import com.pio.nexverse.dto.ErrorResponseDTO;
import com.pio.nexverse.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Anonymous";
        Collection<? extends GrantedAuthority> authorities = authentication != null ? authentication.getAuthorities() : List.of();
        log.warn("Authentication failed. user={}, roles={}, method={}, uri={}, reason={}", username, authorities, request.getMethod(), request.getRequestURI(), authException.getMessage());
        if (authException instanceof UserAccountDisabledException) {
            makeErrorResponse(response, ErrorResponseCode.USER_ACCOUNT_DISABLED, authException.getMessage());
            return;
        }
        if (authException instanceof UserSuspendedException) {
            makeErrorResponse(response, ErrorResponseCode.SUSPENDED, authException.getMessage());
            return;
        }
        if (authException instanceof OrganizationDisabledException) {
            makeErrorResponse(response, ErrorResponseCode.ORGANIZATION_DISABLED, authException.getMessage());
            return;
        }
        if (authException instanceof OrganizationSuspendedException) {
            makeErrorResponse(response, ErrorResponseCode.SUSPENDED, authException.getMessage());
            return;
        }
        makeErrorResponse(response, ErrorResponseCode.AUTHENTICATION_FAILED, ExceptionMessages.AUTHENTICATION_FAILED);
    }

    private void makeErrorResponse(HttpServletResponse response, String errorResponseCode, String exceptionMessage) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponseDTO body = ErrorResponseDTO.of(
                errorResponseCode,
                exceptionMessage
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}