package com.pio.nexverse.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pio.nexverse.constants.ErrorResponseCode;
import com.pio.nexverse.constants.ExceptionMessages;
import com.pio.nexverse.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Anonymous";
        Collection<? extends GrantedAuthority> authorities = authentication != null ? authentication.getAuthorities() : List.of();
        log.warn("Access denied. user={}, roles={}, method={}, uri={}", username, authorities, request.getMethod(), request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponseDTO body = ErrorResponseDTO.of(
                ErrorResponseCode.ACCESS_DENIED,
                ExceptionMessages.ACCESS_DENIED
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}