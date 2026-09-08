package com.pio.nexverse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.pio.nexverse.constants.AppConstants.PHONE_NUMBER_REGEX;
import static com.pio.nexverse.constants.ValidationMessages.*;

@Data
public class CreateEmployeeRequestDTO {
    @NotBlank(message = EMPLOYEE_FIRST_NAME_REQUIRED)
    private String firstName;
    private String lastName;

    @NotBlank(message = EMAIL_REQUIRED)
    @Email(message = VALID_EMAIL)
    private String email;

    @Pattern(regexp = PHONE_NUMBER_REGEX, message = VALID_PHONE_NUMBER)
    private String phoneNumber;

    private String employeeCode;
    private String jobTitle;

    @NotBlank(message = DEPARTMENT_NAME_REQUIRED)
    private String departmentName;
}