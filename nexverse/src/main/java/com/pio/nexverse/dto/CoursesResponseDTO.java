package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import com.pio.nexverse.enums.ResourceCreationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursesResponseDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private Level level;
    private CourseVisibility visibility;
    private CourseAccessType accessType;
    private ResourceCreationStatus status;
    private LocalDateTime publishedAt;
    private Integer estimatedDurationSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String owningDepartment;
    private Long owningDepartmentId;
    private Boolean hasAccess;
}