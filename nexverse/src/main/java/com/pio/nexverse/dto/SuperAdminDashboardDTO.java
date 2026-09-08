package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SuperAdminDashboardDTO {
    private SuperAdminDashboardMetricsDTO metrics;
    private SuperAdminDashboardGrowthDTO growth;
    private List<RecentOrganizationDTO> recentOrganizations;
}