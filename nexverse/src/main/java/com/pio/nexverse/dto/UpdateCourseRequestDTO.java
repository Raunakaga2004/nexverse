package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateCourseRequestDTO {
    private String title;
    private String shortDescription;
    private String description;
    private Level level;
    private CourseVisibility visibility;
    private CourseAccessType accessType;
    private List<CourseSkillRequestDTO> courseSkills;
}