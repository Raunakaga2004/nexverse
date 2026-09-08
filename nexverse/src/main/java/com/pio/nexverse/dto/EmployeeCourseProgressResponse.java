package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseProgressStatus;
import com.pio.nexverse.enums.EnrollmentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeCourseProgressResponse {
    private Long courseId;
    private String courseTitle;
    private EnrollmentType enrollmentType;
    private Double progressPercent;
    private Integer totalModules;
    private Integer completedModules;
    private Integer totalContents;
    private Integer completedContents;
    private Integer estimatedDurationSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime lastAccessedAt;
    private CourseProgressStatus status;
}