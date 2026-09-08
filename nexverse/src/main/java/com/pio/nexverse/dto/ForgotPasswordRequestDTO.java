package com.pio.nexverse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.pio.nexverse.constants.ValidationMessages.EMAIL_REQUIRED;
import static com.pio.nexverse.constants.ValidationMessages.VALID_EMAIL;

@Data
public class ForgotPasswordRequestDTO {
    @NotBlank(message = EMAIL_REQUIRED)
    @Email(message = VALID_EMAIL)
    private String email;
}