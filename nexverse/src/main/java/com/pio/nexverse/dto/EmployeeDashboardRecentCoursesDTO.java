package com.pio.nexverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardRecentCoursesDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private Double progressPercent;
    private LocalDateTime lastAccessedAt;
}
