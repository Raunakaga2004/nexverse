package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.CourseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course-requests")
public class CourseRequestController {
    private final CourseRequestService courseRequestService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/{courseId}/department")
    public ResponseEntity<ApiResponseDTO<Void>> accessCourseForDepartment(@PathVariable Long courseId, @RequestBody CourseAccessRequestDTO courseAccessRequest) {
        log.info("Request received to access course by department. courseId={}", courseId);
        courseRequestService.accessCourseForDepartment(courseId, courseAccessRequest);
        log.info("Course access processed successfully by department. courseId={}", courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_ACCESS_PROCESSED_SUCCESSFULLY)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'EMPLOYEE')")
    @PostMapping("/{courseId}/employee")
    public ResponseEntity<ApiResponseDTO<Void>> accessCourseForEmployee(@PathVariable Long courseId, @RequestBody CourseAccessRequestDTO courseAccessRequest) {
        log.info("Request received to access course for personal learning. courseId={}", courseId);
        courseRequestService.accessCourseForEmployee(courseId, courseAccessRequest);
        log.info("Course access processed successfully for personal learning. courseId={}", courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_ACCESS_PROCESSED_SUCCESSFULLY)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/department")
    public ResponseEntity<ApiResponseDTO<Page<DepartmentCourseRequestsResponseDTO>>> getAllRequestsOfDepartments(@ModelAttribute CourseRequestDTO request, @PageableDefault(page = 1, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Request received to load all requests of courses from other department. request={}, pageable={}", request, pageable);
        Page<DepartmentCourseRequestsResponseDTO> response = courseRequestService.getAllRequestsOfDepartments(request, pageable);
        log.info("My learning courses from other department successfully.");
        return ResponseEntity.ok(ApiResponseDTO.<Page<DepartmentCourseRequestsResponseDTO>>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .data(response)
                .build());
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/employee")
    public ResponseEntity<ApiResponseDTO<Page<EmployeeCourseRequestsResponseDTO>>> getAllRequestsOfEmployees(@ModelAttribute CourseRequestDTO request, @PageableDefault(page = 1,  sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Request received to load all requests of courses from employee. request={}, pageable={}", request, pageable);
        Page<EmployeeCourseRequestsResponseDTO> response = courseRequestService.getAllRequestsOfEmployees(request, pageable);
        log.info("My learning courses from employee successfully.");
        return ResponseEntity.ok(ApiResponseDTO.<Page<EmployeeCourseRequestsResponseDTO>>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .data(response)
                .build());
    }

    @PatchMapping("/department/{requestId}/status")
    public ResponseEntity<ApiResponseDTO<Void>> changeRequestStatusForDepartment(@RequestBody ChangeRequestStatusDTO changeRequestStatus, @PathVariable Long requestId) {
        log.info("Request received to change status of requests received to manager for course access.");
        courseRequestService.changeRequestStatusForDepartment(requestId, changeRequestStatus);
        log.info("Request received to change status of requests received to manager for course access processed successfully.");
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .build());
    }

    @PatchMapping("/employee/{requestId}/status")
    public ResponseEntity<ApiResponseDTO<Void>> changeRequestStatusForEmployee(@RequestBody ChangeRequestStatusDTO changeRequestStatus, @PathVariable Long requestId) {
        log.info("Request received to change status of requests received to manager for course access. status ={}", changeRequestStatus);
        courseRequestService.changeRequestStatusForEmployee(requestId, changeRequestStatus);
        log.info("Request received to change status of requests received to manager for course access processed successfully. status={}", changeRequestStatus);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .build());
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponseDTO<Void>> assignCourseToEmployees(@RequestBody AssignCourseRequestDTO request) {
        log.info("Request received to assign courses. course = {}, employees ={}", request.getCourseId(), request.getEmployeesId());
        courseRequestService.assignCourseToEmployees(request);
        log.info("Request processed to assign courses. course = {}, employees ={}", request.getCourseId(), request.getEmployeesId());
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message(COURSE_ASSIGNED_SUCCESS)
                .build());
    }
}