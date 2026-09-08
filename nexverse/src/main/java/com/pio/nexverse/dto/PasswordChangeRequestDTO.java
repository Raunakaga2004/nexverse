package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.pio.nexverse.constants.AppConstants.PASSWORD_REGEX;
import static com.pio.nexverse.constants.ValidationMessages.*;

@Data
public class PasswordChangeRequestDTO {
    @NotBlank(message = OLD_PASSWORD_REQUIRED)
    private String oldPassword;

    @NotBlank(message = NEW_PASSWORD_REQUIRED)
    @Pattern(regexp = PASSWORD_REGEX, message = INVALID_PASSWORD)
    private String newPassword;
}