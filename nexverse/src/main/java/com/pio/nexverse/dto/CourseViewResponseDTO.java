package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseViewResponseDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private Level level;
    private CourseAccessType accessType;
    private CourseVisibility visibility;
    private Integer estimatedDurationSeconds;
    private LocalDateTime publishedAt;
    private Long publisherId;
    private String publisherName;
    private List<CourseSkillResponseDTO> skills;
    private Integer totalModules;
    private Integer totalContents;
    private Double progressPercent;
    private List<CourseModuleViewDTO> modules;
    private Boolean hasAccess;
    private CourseAccessRequestStatus requestStatus;
}