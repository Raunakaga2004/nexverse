package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.CourseModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('MANAGER')")
public class CourseModuleController {
    private final CourseModuleService courseModuleService;

    @PostMapping("/course/{courseId}/module")
    public ResponseEntity<ApiResponseDTO<Void>> createModule(@PathVariable Long courseId, @Valid @RequestBody CreateCourseModuleRequestDTO request) {
        log.info("Create course + received. name = {}", request.getTitle());
        courseModuleService.createModule(courseId, request);
        log.info("Course created successfully. name = {}", request.getTitle());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_MODULE_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/course/{courseId}/modules")
    public ResponseEntity<ApiResponseDTO<List<CourseModuleResponseDTO>>> getModules(@PathVariable Long courseId) {
        List<CourseModuleResponseDTO> modules = courseModuleService.getModules(courseId);
        ApiResponseDTO<List<CourseModuleResponseDTO>> response = ApiResponseDTO.<List<CourseModuleResponseDTO>>builder()
                .message(COURSE_MODULES_RETRIEVED_SUCCESS)
                .data(modules)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/course-module/{moduleId}")
    public ResponseEntity<ApiResponseDTO<CourseModuleResponseDTO>> getModule(@PathVariable Long moduleId) {
        CourseModuleResponseDTO module = courseModuleService.getModule(moduleId);
        ApiResponseDTO<CourseModuleResponseDTO> response = ApiResponseDTO.<CourseModuleResponseDTO>builder()
                .message(COURSE_MODULE_RETRIEVED_SUCCESS)
                .data(module)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/course-module/{moduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateModule(@PathVariable Long moduleId, @RequestBody UpdateCourseModuleRequestDTO request) {
        log.info("Course module update request received for moduleId = {} by request = {}", moduleId, request);
        courseModuleService.updateModule(moduleId, request);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_MODULE_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/course-module/{moduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteModule(@PathVariable Long moduleId) {
        courseModuleService.deleteModule(moduleId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_MODULE_DELETED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/course-module/{courseId}/order")
    public ResponseEntity<ApiResponseDTO<Void>> reorderModuleContents(@PathVariable Long courseId, @Valid @RequestBody ReorderModuleRequestDTO request) {
        courseModuleService.reorderModule(courseId, request);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(COURSE_REORDERED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}