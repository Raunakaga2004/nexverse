package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDashboardMetricsDTO {
    private long assignedCourses;
    private long inProgressCourses;
    private long completedCourses;
    private long learningHours;
}