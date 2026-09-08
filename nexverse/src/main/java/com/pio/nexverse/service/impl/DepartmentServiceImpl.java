package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.exception.DepartmentNotFoundException;
import com.pio.nexverse.exception.ResourceAlreadyExistsException;
import com.pio.nexverse.exception.UserNotFoundException;
import com.pio.nexverse.repository.DepartmentRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.DepartmentService;
import com.pio.nexverse.specification.DepartmentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.pio.nexverse.constants.ExceptionMessages.DEP_NAME_EXIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    @Transactional
    public void createDepartment(DepartmentRequestDTO request) {
        log.info("Creating department. name = {}", request.getName());
        Organization organization = currentUserService.getCurrentOrganization();
        validateDepartmentCreation(request, organization.getId());
        Department department = modelMapper.map(request, Department.class);
        department.setOrganization(currentUserService.getCurrentOrganization());
        departmentRepository.save(department);
        log.info("Department created successfully. departmentId = {}, organizationId = {}", department.getId(), organization.getId());
    }

    @Transactional
    public void updateDepartment(Long departmentId, DepartmentRequestDTO request) {
        log.info("Updating department. departmentId = {}", departmentId);
        Department department = getDepartmentEntity(departmentId);
        mapRequestToDepartmentForUpdate(request, department);
        log.info("Department updated successfully. departmentId = {}", departmentId);
    }

    public Page<DepartmentsResponseDTO> getDepartments(Pageable pageable, DepartmentSearchRequestDTO filter) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Organization organization = currentUserService.getCurrentOrganization();
        Specification<Department> specification = buildDepartmentSearchSpecification(filter);
        Page<Department> departments = departmentRepository.findAll(specification, adjustedPageable);
        log.info("Successfully retrieved {} departments of organizationId = {}.", departments.getTotalElements(), organization.getId());
        return departments.map(this::mapToDepartmentsResponse);
    }

    private DepartmentsResponseDTO mapToDepartmentsResponse(Department department) {
        DepartmentsResponseDTO departmentResponse = modelMapper.map(department, DepartmentsResponseDTO.class);
        Optional<User> departmentManager = department.getUsers().stream()
                .filter((user -> user.getRole() == Role.MANAGER))
                .findFirst();
        departmentManager.ifPresent(user -> departmentResponse.setDepartmentManager(user.getFirstName() + " " + user.getLastName()));
        departmentResponse.setEmployeeCount(userRepository.countByDepartmentId(department.getId()));
        return departmentResponse;
    }

    private Specification<Department> buildDepartmentSearchSpecification(DepartmentSearchRequestDTO filter) {
        Specification<Department> specification = DepartmentSpecification.belongsToOrganization(currentUserService.getCurrentOrganization().getId());
        if (filter.getIsEnabled() != null) {
            specification = specification.and(DepartmentSpecification.isEnabled(filter.getIsEnabled()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            specification = specification.and(DepartmentSpecification.containsKeyword(filter.getSearch()));
        }
        return specification;
    }

    public DepartmentResponseDTO getDepartment(Long departmentId) {
        DepartmentResponseDTO response = modelMapper.map(getDepartmentEntity(departmentId), DepartmentResponseDTO.class);
        response.setEmployeeCount(userRepository.countByDepartmentId(departmentId));
        return response;
    }

    @Transactional
    public void assignManager(Long departmentId, AssignDepartmentManagerRequestDTO request) {
        Long orgId = currentUserService.getCurrentOrganization().getId();
        Department department = getDepartmentEntity(departmentId);
        User newManager = userRepository.findByIdAndOrganizationId(request.getEmployeeId(), orgId).orElseThrow(UserNotFoundException::new);
        Optional<User> currentManager = userRepository.findByDepartmentIdAndRole(departmentId, Role.MANAGER);
        currentManager.ifPresent(user -> user.setRole(Role.EMPLOYEE));
        newManager.setDepartment(department);
        newManager.setRole(Role.MANAGER);
    }

    @Transactional
    public void updateActivation(Long departmentId, ActivationUpdateRequestDTO updateActivationRequest) {
        Department department = getDepartmentEntity(departmentId);
        department.setEnabled(updateActivationRequest.getIsEnabled());
        log.info("Department activation updated successfully. departmentId = {}", departmentId);
    }

    private void validateDepartmentCreation(DepartmentRequestDTO request, Long organizationId) {
        if (departmentRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            log.warn("Department creation failed. Department name already exists. name = {}, organizationId = {}", request.getName(), organizationId);
            throw new ResourceAlreadyExistsException(DEP_NAME_EXIST);
        }
    }

    private Department getDepartmentEntity(Long departmentId) {
        Long organizationId = currentUserService.getCurrentOrganization().getId();
        Department department = departmentRepository.findByIdAndOrganizationId(departmentId, organizationId).orElseThrow(DepartmentNotFoundException::new);
        log.info("Department retrieved successfully. departmentId = {}, organizationId = {}", departmentId, organizationId);
        return department;
    }

    private void mapRequestToDepartmentForUpdate(DepartmentRequestDTO request, Department department) {
        String name = request.getName();
        Organization organization = currentUserService.getCurrentOrganization();
        if (name != null) {
            if (departmentRepository.existsByNameAndOrganizationIdAndIdNot(name, organization.getId(), department.getId())) {
                throw new ResourceAlreadyExistsException(DEP_NAME_EXIST);
            }
            department.setName(name);
        }
    }
}