package com.pio.nexverse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.pio.nexverse.constants.ValidationMessages.*;

@Data
public class LoginRequestDTO {
    @NotBlank(message = EMAIL_REQUIRED)
    @Email(message = VALID_EMAIL)
    private String email;

    @NotBlank(message = PASSWORD_REQUIRED)
    private String password;
}