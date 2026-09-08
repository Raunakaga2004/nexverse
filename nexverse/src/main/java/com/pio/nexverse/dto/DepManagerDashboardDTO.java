package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepManagerDashboardDTO {
    private DepManagerDashboardMetricsDTO metrics;
    private DepManagerDashboardGrowthDTO growth;
}
