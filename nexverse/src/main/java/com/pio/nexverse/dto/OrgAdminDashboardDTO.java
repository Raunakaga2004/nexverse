package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrgAdminDashboardDTO {
    private OrgAdminDashboardMetricsDTO metrics;
    private OrgAdminDashboardGrowthDTO growth;
    private List<DepartmentDistributionDTO> departmentDistribution;
    private StorageUsageDTO storage;
}