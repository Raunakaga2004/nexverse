package com.pio.nexverse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.pio.nexverse.constants.AppConstants.PHONE_NUMBER_REGEX;
import static com.pio.nexverse.constants.ValidationMessages.VALID_EMAIL;
import static com.pio.nexverse.constants.ValidationMessages.VALID_PHONE_NUMBER;

@Data
public class UpdateOrganizationRequestDTO {
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = PHONE_NUMBER_REGEX, message = VALID_PHONE_NUMBER)
    private String phone;

    @Email(message = VALID_EMAIL)
    private String email;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
