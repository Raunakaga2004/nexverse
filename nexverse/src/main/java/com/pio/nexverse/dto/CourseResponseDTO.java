package com.pio.nexverse.dto;

import com.pio.nexverse.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {
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
    private List<CourseSkillResponseDTO> courseSkills;
    private List<CourseModuleResponseDTO> modules;
    private boolean hasAccess;
    private CourseAccessRequestStatus requestStatus;
}