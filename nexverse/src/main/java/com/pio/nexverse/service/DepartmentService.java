package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    void createDepartment(DepartmentRequestDTO request);

    void updateDepartment(Long departmentId, DepartmentRequestDTO request);

    DepartmentResponseDTO getDepartment(Long departmentId);

    void assignManager(Long departmentId, AssignDepartmentManagerRequestDTO request);

    void updateActivation(Long departmentId, ActivationUpdateRequestDTO updateActivationRequest);

    Page<DepartmentsResponseDTO> getDepartments(Pageable pageable, DepartmentSearchRequestDTO filter);
}