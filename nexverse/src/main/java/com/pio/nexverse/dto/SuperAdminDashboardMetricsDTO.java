package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperAdminDashboardMetricsDTO {
    private Long totalOrganizations;
    private Long totalUsers;
    private Long totalStorageBytes;
}
