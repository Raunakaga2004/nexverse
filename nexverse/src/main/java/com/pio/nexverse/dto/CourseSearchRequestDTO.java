package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import com.pio.nexverse.enums.ResourceCreationStatus;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class CourseSearchRequestDTO {
    private String search;
    private ResourceCreationStatus status;
    private CourseVisibility visibility;
    private CourseAccessType accessType;
    private Level level;
    private Long departmentId;
    private Long skillId;
}