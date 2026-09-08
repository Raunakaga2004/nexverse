package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import lombok.Data;

@Data
public class EmployeeCourseRequestsResponseDTO {
    private Long id;
    private String requestNote;
    private String employeeName;
    private Long employeeId;
    private String courseName;
    private Long courseId;
    private CourseAccessRequestStatus accessRequestStatus;
    private String department; // department of employee
}