package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {
    void createEmployee(CreateEmployeeRequestDTO request, MultipartFile profileImage);

    void updateEmployee(Long employeeId, UpdateEmployeeRequestDTO request, MultipartFile profileImage);

    EmployeeResponseDTO getEmployee(Long employeeId);

    void updateActivation(Long employeeId, ActivationUpdateRequestDTO updateActivationRequest);

    void suspendEmployee(Long employeeId);

    void reactivateEmployee(Long employeeId);

    Resource getProfileImage(Long employeeId);

    ImportEmployeeResponseDTO importEmployees(MultipartFile file);

    Resource getImportEmployeeTemplate();

    EmployeeLearningProgressResponse getEmployeeLearningProgress(Long employeeId, MyLearningRequestDTO request, Pageable pageable);

    Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable, EmployeeSearchRequest filter);
}