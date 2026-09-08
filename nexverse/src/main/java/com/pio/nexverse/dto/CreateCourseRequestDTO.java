package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequestDTO {
    @NotBlank
    private String title;
    private String shortDescription;
    private String description;
    @NotNull
    private Level level;
    @NotNull
    private CourseVisibility visibility;
    @NotNull
    private CourseAccessType accessType;
    private Long estimatedDurationSeconds;
    private List<CourseSkillRequestDTO> courseSkills;
}