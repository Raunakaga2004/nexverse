package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.Level;
import lombok.Data;

@Data
public class BrowseCoursesRequestDTO {
    private String search;
    private CourseAccessType accessType;
    private Level level;
}