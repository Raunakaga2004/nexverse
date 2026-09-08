package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.pio.nexverse.constants.ValidationMessages.DEPARTMENT_NAME_REQUIRED;

@Data
public class DepartmentRequestDTO {
    @NotBlank(message = DEPARTMENT_NAME_REQUIRED)
    private String name;
}