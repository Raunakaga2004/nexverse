package com.pio.nexverse.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class EmployeeLearningProgressResponse {
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private Integer totalCourses;
    private Integer completedCourses;
    private Integer inProgressCourses;
    private Integer notStartedCourses;
    private Double overallProgressPercent;
    private Page<EmployeeCourseProgressResponse> courses;
}