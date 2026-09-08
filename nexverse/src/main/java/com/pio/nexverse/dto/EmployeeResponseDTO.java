package com.pio.nexverse.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private String email;
    private String phoneNumber;
    private String employeeCode;
    private String jobTitle;
    private String profileImageUrl;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEnabled;
}