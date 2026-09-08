package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgAdminDashboardMetricsDTO {
    private long totalEmployees;
    private long totalDepartments;
    private long totalSkills;
    private long totalCourses;
}