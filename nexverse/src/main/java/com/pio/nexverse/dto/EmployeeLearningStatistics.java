package com.pio.nexverse.dto;

public interface EmployeeLearningStatistics {
    Long getTotalCourses();
    Long getCompletedCourses();
    Long getInProgressCourses();
    Long getNotStartedCourses();
    Double getOverallProgressPercent();
}
