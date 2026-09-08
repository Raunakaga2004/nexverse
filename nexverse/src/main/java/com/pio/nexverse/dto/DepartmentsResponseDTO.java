package com.pio.nexverse.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentsResponseDTO {
    private Long id;
    private String name;
    private String departmentManager;
    private Long employeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEnabled;
}