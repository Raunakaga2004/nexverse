package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentDistributionDTO {
    private Long departmentId;
    private String departmentName;
    private long employeeCount;
}