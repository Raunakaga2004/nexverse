package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.enums.GrowthPeriod;
import com.pio.nexverse.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/super-admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<SuperAdminDashboardDTO>> getSuperAdminDashboard(@RequestParam(defaultValue = "LAST_12_MONTHS") GrowthPeriod period) {
        log.info("Super admin dashboard request received.");
        SuperAdminDashboardDTO superAdminDashboard = dashboardService.getSuperAdminDashboard(period);
        ApiResponseDTO<SuperAdminDashboardDTO> response = ApiResponseDTO.<SuperAdminDashboardDTO>builder()
                .message(SUPER_ADMIN_DASHBOARD_RETRIEVED)
                .data(superAdminDashboard)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/org-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrgAdminDashboardDTO>> getOrgAdminDashboard(@RequestParam(defaultValue = "LAST_12_MONTHS") GrowthPeriod period) {
        log.info("Org admin dashboard request received.");
        OrgAdminDashboardDTO orgAdminDashboard = dashboardService.getOrgAdminDashboard(period);
        ApiResponseDTO<OrgAdminDashboardDTO> response = ApiResponseDTO.<OrgAdminDashboardDTO>builder()
                .message(ORG_ADMIN_DASHBOARD_RETRIEVED)
                .data(orgAdminDashboard)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponseDTO<DepManagerDashboardDTO>> getDepManagerDashboard(@RequestParam(defaultValue = "LAST_12_MONTHS") GrowthPeriod period) {
        log.info("Department manager dashboard request received.");
        DepManagerDashboardDTO depManagerDashboard = dashboardService.getManagerDashboard(period);
        ApiResponseDTO<DepManagerDashboardDTO> response = ApiResponseDTO.<DepManagerDashboardDTO>builder()
                .message(DEP_MANAGER_DASHBOARD_RETRIEVED)
                .data(depManagerDashboard)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<EmployeeDashboardDTO>> getEmployeeDashboard(@RequestParam(defaultValue = "LAST_12_MONTHS") GrowthPeriod period) {
        log.info("Employee dashboard request received.");
        EmployeeDashboardDTO employeeDashboard = dashboardService.getEmployeeDashboard();
        ApiResponseDTO<EmployeeDashboardDTO> response = ApiResponseDTO.<EmployeeDashboardDTO>builder()
                .message(EMPLOYEE_DASHBOARD_RETRIEVED)
                .data(employeeDashboard)
                .build();
        return ResponseEntity.ok(response);
    }
}