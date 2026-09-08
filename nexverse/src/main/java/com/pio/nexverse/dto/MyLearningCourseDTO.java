package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import com.pio.nexverse.enums.CourseProgressStatus;
import com.pio.nexverse.enums.EnrollmentType;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Data
public class MyLearningCourseDTO {
    private Long courseId;
    private String title;
    private String thumbnailUrl;
    private String shortDescription;
    private double progressPercent;
    private int completedContents;
    private int totalContents;
    private int completedModules;
    private int totalModules;
    private CourseProgressStatus progressStatus;
    private CourseAccessRequestStatus requestStatus;
    private EnrollmentType enrollmentType;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime requestedAt;
    private String rejectionReason;
}