package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import lombok.Data;

@Data
public class DepartmentCourseRequestsResponseDTO {
    private Long id;
    private String requestNote;
    private CourseAccessRequestStatus requestStatus;
    private String courseName;
    private Long courseId;
    private String requestedBy;
    private String requestingDepartment;
}