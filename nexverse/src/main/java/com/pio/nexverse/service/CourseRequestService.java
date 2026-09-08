package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRequestService {
    void accessCourseForDepartment(Long courseId, CourseAccessRequestDTO courseAccessRequest);

    void accessCourseForEmployee(Long courseId, CourseAccessRequestDTO courseAccessRequest);

    Page<DepartmentCourseRequestsResponseDTO> getAllRequestsOfDepartments(CourseRequestDTO request, Pageable pageable);

    Page<EmployeeCourseRequestsResponseDTO> getAllRequestsOfEmployees(CourseRequestDTO request, Pageable pageable);

    void changeRequestStatusForDepartment(Long requestId, ChangeRequestStatusDTO changeRequestStatus);

    void changeRequestStatusForEmployee(Long requestId, ChangeRequestStatusDTO changeRequestStatus);

    void assignCourseToEmployees(AssignCourseRequestDTO request);
}