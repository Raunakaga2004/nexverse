package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course")
@PreAuthorize("hasAuthority('MANAGER')")
public class CourseController {
    private final CourseService courseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> createCourse(@Valid @RequestPart("course") CreateCourseRequestDTO request, @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        log.info("Create course request received. name = {}", request.getTitle());
        courseService.createCourse(request, thumbnail);
        log.info("Course created successfully. name = {}", request.getTitle());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> updateCourse(@PathVariable Long courseId, @Valid @RequestPart("course") UpdateCourseRequestDTO request, @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        log.info("Update course request received. courseId = {}", courseId);
        courseService.updateCourse(courseId, request, thumbnail);
        log.info("Course updated successfully. courseId = {}", courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<CoursesResponseDTO>>> getCourses(@PageableDefault(page = 1, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @ModelAttribute CourseSearchRequestDTO request) {
        log.info("Request received to fetch courses. request = {}, pageable = {}", request, pageable);
        Page<CoursesResponseDTO> courseResponse = courseService.getCourses(request, pageable);
        log.info("Courses list fetched successfully. request = {}", request);
        ApiResponseDTO<Page<CoursesResponseDTO>> response = ApiResponseDTO.<Page<CoursesResponseDTO>>builder()
                .message(COURSES_RETRIEVED_SUCCESS)
                .data(courseResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponseDTO<CourseResponseDTO>> getCourse(@PathVariable Long courseId) {
        log.info("Fetching course. courseId = {}", courseId);
        CourseResponseDTO courseResponse = courseService.getCourse(courseId);
        log.info("Course retrieved successfully. courseId = {}", courseId);
        ApiResponseDTO<CourseResponseDTO> response = ApiResponseDTO.<CourseResponseDTO>builder()
                .message(COURSE_RETRIEVED_SUCCESS)
                .data(courseResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}/delete")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDraftCourse(@PathVariable Long courseId) {
        log.info("Delete draft course request received. courseId = {}", courseId);
        courseService.deleteDraftCourse(courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(DRAFT_COURSE_DELETED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @GetMapping("/{courseId}/thumbnail")
    public ResponseEntity<Resource> getCourseThumbnail(@PathVariable Long courseId) throws IOException {
        Resource resource = courseService.getCourseThumbnail(courseId);
        String contentType;
        try (InputStream is = resource.getInputStream()) {
            contentType = URLConnection.guessContentTypeFromStream(is);
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<ApiResponseDTO<Void>> publishCourse(@PathVariable Long courseId) {
        courseService.publishCourse(courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_PUBLISHED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{courseId}/archive")
    public ResponseEntity<ApiResponseDTO<Void>> archiveCourse(@PathVariable Long courseId) {
        courseService.archiveCourse(courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_ARCHIVED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{courseId}/restore")
    public ResponseEntity<ApiResponseDTO<Void>> restoreCourse(@PathVariable Long courseId) {
        courseService.restoreCourse(courseId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_RESTORED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}