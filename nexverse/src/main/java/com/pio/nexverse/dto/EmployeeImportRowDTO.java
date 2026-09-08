package com.pio.nexverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class EmployeeImportRowDTO {
    private int rowNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String employeeCode;
    private String jobTitle;
    private String departmentName;
}