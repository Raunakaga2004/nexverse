package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.pio.nexverse.constants.AppConstants.XML_CONTENT_TYPE;
import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
@PreAuthorize("hasAuthority('ADMIN')")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> createEmployee(@Valid @RequestPart("employee") CreateEmployeeRequestDTO request, @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("Create employee request received. email = {}", request.getEmail());
        employeeService.createEmployee(request, profileImage);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(EMPLOYEE_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{employeeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> updateEmployee(@PathVariable Long employeeId, @Valid @RequestPart("employee") UpdateEmployeeRequestDTO request, @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("Update employee request received. employeeId = {}", employeeId);
        employeeService.updateEmployee(employeeId, request, profileImage);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(EMPLOYEE_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{employeeId}/suspend")
    public ResponseEntity<ApiResponseDTO<Void>> suspendEmployee(@PathVariable Long employeeId) {
        log.info("Suspend employee request received. employeeId = {}", employeeId);
        employeeService.suspendEmployee(employeeId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(EMPLOYEE_SUSPENDED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{employeeId}/reactivate")
    public ResponseEntity<ApiResponseDTO<Void>> reactivateEmployee(@PathVariable Long employeeId) {
        log.info("Unsuspend employee request received. employeeId = {}", employeeId);
        employeeService.reactivateEmployee(employeeId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(EMPLOYEE_REACTIVATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<EmployeeResponseDTO>>> getEmployees(@PageableDefault(page = 1, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @ModelAttribute EmployeeSearchRequest filter) {
        log.info("Fetch employees request received. pageable={}, filter={}", pageable, filter);
        Page<EmployeeResponseDTO> empResponse = employeeService.getAllEmployees(pageable, filter);
        ApiResponseDTO<Page<EmployeeResponseDTO>> response = ApiResponseDTO.<Page<EmployeeResponseDTO>>builder()
                .message(EMPLOYEES_RETRIEVED_SUCCESS)
                .data(empResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponseDTO<EmployeeResponseDTO>> getEmployee(@PathVariable Long employeeId) {
        log.info("Fetch employee request received. employeeId = {}", employeeId);
        EmployeeResponseDTO empResponse = employeeService.getEmployee(employeeId);
        ApiResponseDTO<EmployeeResponseDTO> response = ApiResponseDTO.<EmployeeResponseDTO>builder()
                .message(EMPLOYEE_RETRIEVED_SUCCESS)
                .data(empResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{employeeId}/activation")
    public ResponseEntity<ApiResponseDTO<Void>> updateActivation(@PathVariable Long employeeId, @Valid @RequestBody ActivationUpdateRequestDTO updateActivationRequest) {
        log.info("Update activation employee request received. employeeId = {}, enabled = {}", employeeId, updateActivationRequest.getIsEnabled());
        employeeService.updateActivation(employeeId, updateActivationRequest);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(updateActivationRequest.getIsEnabled() ? EMPLOYEE_ENABLED_SUCCESS : EMPLOYEE_DISABLED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employeeId}/profile-image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable Long employeeId) throws IOException {
        Resource resource = employeeService.getProfileImage(employeeId);
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

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<ImportEmployeeResponseDTO>> importEmployees(@RequestParam("file") MultipartFile file) {
        log.info("Employee import request received. fileName = {}, size = {} bytes", file.getOriginalFilename(), file.getSize());
        ImportEmployeeResponseDTO importResponse = employeeService.importEmployees(file);
        log.info("Employees import completed. success = {}, failed = {}, total = {}", importResponse.getSuccessfulImports(), importResponse.getFailedImports(), importResponse.getTotalRows());
        ApiResponseDTO<ImportEmployeeResponseDTO> response = ApiResponseDTO.<ImportEmployeeResponseDTO>builder()
                .message(EMPLOYEES_IMPORTED_SUCCESS)
                .data(importResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/import/template")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = employeeService.getImportEmployeeTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XML_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_import_template.xlsx")
                .body(resource);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/{employeeId}/learning-progress")
    public ResponseEntity<ApiResponseDTO<EmployeeLearningProgressResponse>> getEmployeeLearningProgress(@PathVariable Long employeeId, @ModelAttribute MyLearningRequestDTO request, @PageableDefault(page = 1, size = 6, sort = "lastAccessedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        EmployeeLearningProgressResponse learningProgress = employeeService.getEmployeeLearningProgress(employeeId, request, pageable);
        ApiResponseDTO<EmployeeLearningProgressResponse> response = ApiResponseDTO.<EmployeeLearningProgressResponse>builder()
                .message(EMPLOYEE_LEARNING_PROGRESS_FETCHED)
                .data(learningProgress)
                .build();
        return ResponseEntity.ok(response);
    }
}