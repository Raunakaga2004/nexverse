package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.EmployeeCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee/course")
@PreAuthorize("hasAnyAuthority('MANAGER', 'EMPLOYEE')")
public class EmployeeCourseController {
    private final EmployeeCourseService employeeCourseService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<BrowseCourseResponseDTO>>> getCourses(@ModelAttribute BrowseCoursesRequestDTO request, @PageableDefault(size = 6, page = 1, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Request received to browse courses. request = {}", request);
        Page<BrowseCourseResponseDTO> courseResponse = employeeCourseService.browseCourses(request, pageable);
        log.info("Courses fetched successfully. request = {}", request);
        ApiResponseDTO<Page<BrowseCourseResponseDTO>> response = ApiResponseDTO.<Page<BrowseCourseResponseDTO>>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .data(courseResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponseDTO<List<EmployeeSkillResponseDTO>>> getEmployeeSkills(@ModelAttribute EmployeeSkillRequestDTO request) {
        log.info("Request received to fetch employee skills. request={}", request);
        List<EmployeeSkillResponseDTO> response = employeeCourseService.getEmployeeSkills(request);
        return ResponseEntity.ok(ApiResponseDTO.<List<EmployeeSkillResponseDTO>>builder()
                .message(SKILLS_RETRIEVED_SUCCESS)
                .data(response)
                .build()
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponseDTO<CourseViewResponseDTO>> getCourse(@PathVariable Long courseId) {
        log.info("Request received to fetch course details. courseId={}", courseId);
        CourseViewResponseDTO courseResponse = employeeCourseService.getCourse(courseId);
        log.info("Course details fetched successfully. courseId={}", courseId);
        ApiResponseDTO<CourseViewResponseDTO> response = ApiResponseDTO.<CourseViewResponseDTO>builder()
                .message(COURSE_RETRIEVED_SUCCESS)
                .data(courseResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courseId}/learn")
    public ResponseEntity<ApiResponseDTO<LearningCourseResponseDTO>> getLearningCourse(@PathVariable Long courseId) {
        log.info("Request received to load learning course. courseId={}", courseId);
        LearningCourseResponseDTO response = employeeCourseService.getLearningCourse(courseId);
        log.info("Learning course loaded successfully. courseId={}", courseId);
        return ResponseEntity.ok(ApiResponseDTO.<LearningCourseResponseDTO>builder()
                .message(COURSE_RETRIEVED_SUCCESS)
                .data(response)
                .build()
        );
    }

    @GetMapping("/{courseId}/contents/{contentId}")
    public ResponseEntity<ApiResponseDTO<LearningContentResponseDTO>> getContent(@PathVariable Long courseId, @PathVariable Long contentId) {
        log.info("Request received to get content. courseId={}, contentId={}", courseId, contentId);
        LearningContentResponseDTO response = employeeCourseService.getContent(courseId, contentId);
        log.info("Content retrieved successfully. courseId={}, contentId={}", courseId, contentId);
        return ResponseEntity.ok(ApiResponseDTO.<LearningContentResponseDTO>builder()
                .message(CONTENT_RETRIEVED_SUCCESS)
                .data(response)
                .build()
        );
    }

    @GetMapping("/{courseId}/contents/{contentId}/video")
    public ResponseEntity<ResourceRegion> streamVideo(@PathVariable Long courseId, @PathVariable Long contentId, @RequestHeader HttpHeaders headers) throws IOException {
        log.info("Request received to stream video. courseId={}, contentId={}", courseId, contentId);
        return employeeCourseService.streamVideo(courseId, contentId, headers);
    }

    @GetMapping("/{courseId}/contents/{contentId}/document")
    public ResponseEntity<Resource> getDocument(@PathVariable Long courseId, @PathVariable Long contentId) {
        log.info("Request received to get document. courseId={}, contentId={}", courseId, contentId);
        return employeeCourseService.getDocument(courseId, contentId);
    }

    @GetMapping("/{courseId}/contents/{contentId}/text")
    public ResponseEntity<ApiResponseDTO<TextContentResponseDTO>> getTextContent(@PathVariable Long courseId, @PathVariable Long contentId) {
        log.info("Request received to get text content. courseId={}, contentId={}", courseId, contentId);
        TextContentResponseDTO response = employeeCourseService.getTextContent(courseId, contentId);
        return ResponseEntity.ok(ApiResponseDTO.<TextContentResponseDTO>builder()
                .message(CONTENT_RETRIEVED_SUCCESS)
                .data(response)
                .build()
        );
    }

    @PostMapping("/{courseId}/contents/{contentId}/start")
    public ResponseEntity<ApiResponseDTO<Void>> startContent(@PathVariable Long courseId, @PathVariable Long contentId) {
        log.info("Request received to start content. courseId={}, contentId={}", courseId, contentId);
        employeeCourseService.startContent(courseId, contentId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message(CONTENT_STARTED_SUCCESS)
                .build()
        );
    }

    @PostMapping("/{courseId}/contents/{contentId}/complete")
    public ResponseEntity<ApiResponseDTO<Void>> completeContent(@PathVariable Long courseId, @PathVariable Long contentId) {
        log.info("Request received to complete content. courseId={}, contentId={}", courseId, contentId);
        employeeCourseService.completeContent(courseId, contentId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message(CONTENT_COMPLETED_SUCCESS)
                .build()
        );
    }

    @GetMapping("/my-learning")
    public ResponseEntity<ApiResponseDTO<Page<MyLearningCourseDTO>>> getMyLearningCourses(@ModelAttribute MyLearningRequestDTO request, @PageableDefault(page = 1, size = 6, sort = "lastAccessedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Request received to load my learning courses. request={}, pageable={}", request, pageable);
        Page<MyLearningCourseDTO> response = employeeCourseService.getMyLearningCourses(request, pageable);
        log.info("My learning courses loaded successfully.");
        return ResponseEntity.ok(ApiResponseDTO.<Page<MyLearningCourseDTO>>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .data(response)
                .build());
    }
}