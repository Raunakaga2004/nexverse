package com.pio.nexverse.controller;

import com.pio.nexverse.dto.ApiResponseDTO;
import com.pio.nexverse.dto.PasswordChangeRequestDTO;
import com.pio.nexverse.dto.UserDetailsDTO;
import com.pio.nexverse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.pio.nexverse.constants.SuccessResponseMessages.USER_DETAILS;
import static com.pio.nexverse.constants.SuccessResponseMessages.USER_PASSWORD_CHANGED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(@Valid @RequestBody PasswordChangeRequestDTO request) {
        log.info("Change password request received.");
        userService.changePassword(request.getOldPassword(), request.getNewPassword());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(USER_PASSWORD_CHANGED)
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<UserDetailsDTO>> getMe() {
        log.info("User details request received.");
        UserDetailsDTO userDetailsDTO = userService.getMe();
        ApiResponseDTO<UserDetailsDTO> response = ApiResponseDTO.<UserDetailsDTO>builder()
                .message(USER_DETAILS)
                .data(userDetailsDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization/logo")
    public ResponseEntity<Resource> getOrganizationLogo() throws IOException {
        Resource resource = userService.getOrganizationLogo();
        String contentType;
        try (InputStream is = resource.getInputStream()) {
            contentType = URLConnection.guessContentTypeFromStream(is);
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/profile-image")
    public ResponseEntity<Resource> getProfileImage() throws IOException {
        Resource resource = userService.getProfileImage();
        String contentType;
        try (InputStream is = resource.getInputStream()) {
            contentType = URLConnection.guessContentTypeFromStream(is);
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}