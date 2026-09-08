package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import com.pio.nexverse.enums.CourseProgressStatus;
import com.pio.nexverse.enums.EnrollmentType;
import lombok.Data;

@Data
public class MyLearningRequestDTO {
    private String search;
    private CourseProgressStatus progressStatus;
    private CourseAccessRequestStatus requestStatus;
    private EnrollmentType enrollmentType;
}