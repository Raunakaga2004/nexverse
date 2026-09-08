package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeDashboardDTO {
    private EmployeeDashboardMetricsDTO metrics;
    private List<EmployeeDashboardRecentCoursesDTO> recentInProgressCourses;
}