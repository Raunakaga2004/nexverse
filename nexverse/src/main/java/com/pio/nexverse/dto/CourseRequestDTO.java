package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import lombok.Data;

@Data
public class CourseRequestDTO {
    private String search; // course name
    private CourseAccessRequestStatus requestStatus;
}