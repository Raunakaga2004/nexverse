package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.DepartmentService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/department")
@PreAuthorize("hasAuthority('ADMIN')")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> createDepartment(@Valid @RequestBody DepartmentRequestDTO request) {
        log.info("Create department request received. name = {}", request.getName());
        departmentService.createDepartment(request);
        log.info("Department created successfully. name = {}", request.getName());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(DEPARTMENT_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{departmentId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateDepartment(@PathVariable Long departmentId, @Valid @RequestBody DepartmentRequestDTO request) {
        log.info("Update department request received. departmentId = {}", departmentId);
        departmentService.updateDepartment(departmentId, request);
        log.info("Department updated successfully. departmentId = {}", departmentId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(DEPARTMENT_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<DepartmentsResponseDTO>>> getDepartments(@PageableDefault(page = 1, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @ModelAttribute DepartmentSearchRequestDTO filter) {
        log.info("Fetching departments. filter={}, pageable={}", filter, pageable);
        Page<DepartmentsResponseDTO> departmentResponse = departmentService.getDepartments(pageable, filter);
        ApiResponseDTO<Page<DepartmentsResponseDTO>> response = ApiResponseDTO.<Page<DepartmentsResponseDTO>>builder()
                .message(DEPARTMENTS_RETRIEVED_SUCCESS)
                .data(departmentResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<ApiResponseDTO<DepartmentResponseDTO>> getDepartment(@PathVariable Long departmentId) {
        log.info("Fetching department. departmentId = {}", departmentId);
        DepartmentResponseDTO departmentResponse = departmentService.getDepartment(departmentId);
        log.info("Department retrieved successfully. departmentId = {}", departmentId);
        ApiResponseDTO<DepartmentResponseDTO> response = ApiResponseDTO.<DepartmentResponseDTO>builder()
                .message(DEPARTMENT_RETRIEVED_SUCCESS)
                .data(departmentResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{departmentId}/activation")
    public ResponseEntity<ApiResponseDTO<Void>> updateActivation(@PathVariable Long departmentId, @Valid @RequestBody ActivationUpdateRequestDTO updateActivationRequest) {
        log.info("Update activation department request received. departmentId = {}, enabled = {}", departmentId, updateActivationRequest.getIsEnabled());
        departmentService.updateActivation(departmentId, updateActivationRequest);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(updateActivationRequest.getIsEnabled() ? DEPARTMENT_ENABLED_SUCCESS : DEPARTMENT_DISABLED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{departmentId}/manager")
    public ResponseEntity<ApiResponseDTO<Void>> assignManager(@PathVariable Long departmentId, @Valid @RequestBody AssignDepartmentManagerRequestDTO request) {
        log.info("Assign department manager request received. departmentId = {}", departmentId);
        departmentService.assignManager(departmentId, request);
        log.info("Assign department manager successfully. departmentId = {}", departmentId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(DEPARTMENT_ENABLED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}