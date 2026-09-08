package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.Level;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class BrowseCourseResponseDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private Level level;
    private CourseAccessType accessType;
    private Integer estimatedDurationSeconds;
    private String department;
    private List<CourseSkillResponseDTO> courseSkills;
}