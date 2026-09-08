package com.pio.nexverse.service;

import com.pio.nexverse.dto.DepManagerDashboardDTO;
import com.pio.nexverse.dto.EmployeeDashboardDTO;
import com.pio.nexverse.dto.OrgAdminDashboardDTO;
import com.pio.nexverse.dto.SuperAdminDashboardDTO;
import com.pio.nexverse.enums.GrowthPeriod;

public interface DashboardService {
    SuperAdminDashboardDTO getSuperAdminDashboard(GrowthPeriod period);

    OrgAdminDashboardDTO getOrgAdminDashboard(GrowthPeriod period);

    DepManagerDashboardDTO getManagerDashboard(GrowthPeriod period);

    EmployeeDashboardDTO getEmployeeDashboard();
}
