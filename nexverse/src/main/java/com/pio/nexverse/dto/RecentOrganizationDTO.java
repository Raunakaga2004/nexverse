package com.pio.nexverse.dto;

import com.pio.nexverse.enums.OrganizationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentOrganizationDTO {
    private Long id;
    private String organizationName;
    private String organizationAdminName;
    private LocalDateTime createdAt;
    private OrganizationStatus status;
}