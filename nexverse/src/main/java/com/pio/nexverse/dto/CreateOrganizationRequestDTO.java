package com.pio.nexverse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.pio.nexverse.constants.AppConstants.PHONE_NUMBER_REGEX;
import static com.pio.nexverse.constants.ValidationMessages.*;

@Data
public class CreateOrganizationRequestDTO {
    @NotBlank(message = ORGANIZATION_NAME_REQUIRED)
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = PHONE_NUMBER_REGEX, message = VALID_ORG_PHONE_NUMBER)
    private String phone;

    @NotBlank(message = ORG_EMAIL_REQUIRED)
    @Email(message = VALID_ORG_EMAIL)
    private String email;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @NotBlank(message = ORG_ADMIN_FIRST_NAME_REQUIRED)
    private String orgAdminFirstName;
    private String orgAdminLastName;

    @NotBlank(message = ORG_ADMIN_EMAIL_REQUIRED)
    @Email(message = VALID_ORG_ADMIN_EMAIL)
    private String orgAdminEmail;

    @Pattern(regexp = PHONE_NUMBER_REGEX, message = VALID_ORG_ADMIN_PHONE_NUMBER)
    private String orgAdminPhone;
    private String orgAdminEmployeeCode;
    private String orgAdminJobTitle;
}