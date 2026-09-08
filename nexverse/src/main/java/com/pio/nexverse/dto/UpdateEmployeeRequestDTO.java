package com.pio.nexverse.dto;

import lombok.Data;

@Data
public class UpdateEmployeeRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String employeeCode;
    private String jobTitle;
    private String departmentName;
}