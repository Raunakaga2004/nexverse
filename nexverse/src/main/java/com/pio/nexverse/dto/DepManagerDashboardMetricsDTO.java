package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepManagerDashboardMetricsDTO {
    private long totalEmployees;
    private long totalCourses;
    private long totalCourseAccess;
    private long pendingApprovals;
}
