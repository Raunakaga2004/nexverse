package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.auth.UserSetPasswordToken;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.DepartmentRepository;
import com.pio.nexverse.repository.UserCourseRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.repository.UserSetPasswordRepository;
import com.pio.nexverse.service.*;
import com.pio.nexverse.specification.UserCourseSpecification;
import com.pio.nexverse.specification.UserSpecification;
import com.pio.nexverse.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.pio.nexverse.constants.ExceptionMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final UserSetPasswordRepository userSetPasswordRepository;
    private final FileStorageService fileStorageService;
    private final EmployeeImportService employeeImportService;
    private final UserCourseRepository userCourseRepository;

    @Transactional
    public void createEmployee(CreateEmployeeRequestDTO request, MultipartFile profileImage) {
        log.info("Creating employee email = {}", request.getEmail());
        Organization organization = currentUserService.getCurrentOrganization();
        validateEmployeeCreation(request, organization.getId());
        Department department = departmentRepository.findByNameAndOrganizationId(request.getDepartmentName(), organization.getId()).orElseThrow(DepartmentNotFoundException::new);
        if (!department.isEnabled()) {
            log.warn("Employee creation failed. Department id disabled. departmentName = {}, organizationId = {}", department.getName(), organization.getId());
            throw new DepartmentDisableException();
        }
        User employee = modelMapper.map(request, User.class);
        employee.setRole(Role.EMPLOYEE);
        employee.setDepartment(department);
        employee.setStatus(UserStatus.PENDING);
        employee.setOrganization(organization);
        userRepository.save(employee);
        if (profileImage != null) {
            String profilePath = fileStorageService.upload(profileImage, organization.getId(), OrganizationFileType.PROFILE_IMAGE);
            employee.setProfileImageUrl(profilePath);
        }
        userRepository.save(employee);
        log.info("Employee created successfully. employeeId = {}", employee.getId());
        sendAccountActivationEmail(employee, organization);
    }

    private void sendAccountActivationEmail(User employee, Organization organization) {
        String token = TokenUtils.generatePasswordSetToken();
        UserSetPasswordToken userSetPasswordToken = userSetPasswordRepository.findByUserId(employee.getId()).orElse(new UserSetPasswordToken());
        userSetPasswordToken.setUser(employee);
        userSetPasswordToken.setToken(TokenUtils.hashToken(token));
        userSetPasswordToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        userSetPasswordToken.setType(TokenType.ACCOUNT_ACTIVATION);
        userSetPasswordRepository.save(userSetPasswordToken);
        emailService.sendAccountActivationEmail(employee.getEmail(), token, organization.getName());
    }

    private void validateEmployeeCreation(CreateEmployeeRequestDTO request, Long organizationId) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("Employee creation failed. Email already exists. email = {}", request.getEmail());
            throw new ResourceAlreadyExistsException(USER_EMAIL_EXIST);
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Employee creation failed. Phone number already exists. phone number = {}", request.getPhoneNumber());
            throw new ResourceAlreadyExistsException(USER_PHONE_NUMBER_EXIST);
        }
        if (request.getEmployeeCode() != null && userRepository.existsByEmployeeCodeAndOrganizationId(request.getEmployeeCode(), organizationId)) {
            log.warn("Employee creation failed. Employee code already exists in organization = {}. employee code = {}", organizationId, request.getEmployeeCode());
            throw new ResourceAlreadyExistsException(USER_EMP_CODE_EXIST);
        }
    }

    @Transactional
    public void updateEmployee(Long employeeId, UpdateEmployeeRequestDTO request, MultipartFile profileImage) {
        log.info("Updating employee. employeeId = {}", employeeId);
        User employee = getUserEntity(employeeId);
        if (profileImage != null) {
            if (employee.getProfileImageUrl() != null && !employee.getProfileImageUrl().isBlank())
                fileStorageService.delete(employee.getProfileImageUrl());
            String profilePath = fileStorageService.upload(profileImage, currentUserService.getCurrentOrganization().getId(), OrganizationFileType.PROFILE_IMAGE);
            employee.setProfileImageUrl(profilePath);
        }
        mapRequestToUserForUpdate(request, employee);
        log.info("Employee updated successfully. employeeId = {}", employeeId);
    }

    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable, EmployeeSearchRequest filter) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<User> specification = buildEmployeeSearchSpecification(filter);
        Page<User> employees = userRepository.findAll(specification, adjustedPageable);
        log.info("Successfully retrieved {} employees.", employees.getTotalElements());
        return employees.map(org -> modelMapper.map(org, EmployeeResponseDTO.class));
    }

    private Specification<User> buildEmployeeSearchSpecification(EmployeeSearchRequest filter) {
        Organization organization = currentUserService.getCurrentOrganization();
        Specification<User> specification = UserSpecification.belongsToOrganization(organization.getId());
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            specification = specification.and(UserSpecification.containsKeyword(filter.getSearch()));
        }
        Role currentUserRole = currentUserService.getCurrentUser().getRole();
        if (currentUserRole == Role.ADMIN) {
            if (filter.getRole() != null && filter.getRole() != Role.SUPER_ADMIN && filter.getRole() != Role.ADMIN) {
                specification = specification.and(UserSpecification.hasRole(filter.getRole()));
            } else {
                specification = specification.and(UserSpecification.hasRole(Role.EMPLOYEE).or(UserSpecification.hasRole(Role.MANAGER)));
            }
            if (filter.getDepartmentName() != null && !filter.getDepartmentName().isBlank()) {
                specification = specification.and(UserSpecification.hasDepartment(filter.getDepartmentName()));
            }
            if (filter.getIsEnabled() != null) {
                specification = specification.and(UserSpecification.isEnabled(filter.getIsEnabled()));
            }
        } else {
            Department currentDepartment = currentUserService.getCurrentDepartment();
            specification = specification
                    .and(UserSpecification.hasDepartment(currentDepartment.getName()))
                    .and(UserSpecification.isEnabled(true));
        }
        if (filter.getStatus() != null) {
            specification = specification.and(UserSpecification.hasStatus(filter.getStatus()));
        }
        return specification;
    }

    @Override
    public EmployeeResponseDTO getEmployee(Long employeeId) {
        Role currentUserRole = currentUserService.getCurrentUser().getRole();
        User employee = getUserEntity(employeeId);
        if (currentUserRole == Role.MANAGER && (!Objects.equals(employee.getDepartment().getId(), currentUserService.getCurrentDepartment().getId()) || !employee.isEnabled())) {
            throw new UserNotFoundException();
        }
        return modelMapper.map(employee, EmployeeResponseDTO.class);
    }

    @Transactional
    public void updateActivation(Long employeeId, ActivationUpdateRequestDTO updateActivationRequest) {
        User employee = getUserEntity(employeeId);
        employee.setEnabled(updateActivationRequest.getIsEnabled());
        if (employee.getStatus() == UserStatus.PENDING) {
            sendAccountActivationEmail(employee, currentUserService.getCurrentOrganization());
        }
    }

    @Transactional
    public void suspendEmployee(Long employeeId) {
        log.info("Suspending employee. employeeId = {}", employeeId);
        User employee = getUserEntity(employeeId);
        if (employee.getStatus() == UserStatus.PENDING) {
            throw new UserPendingException();
        }
        employee.setStatus(UserStatus.SUSPENDED);
        log.info("Employee suspended successfully. employeeId = {}", employeeId);
    }

    @Transactional
    public void reactivateEmployee(Long employeeId) {
        log.info("Unsuspending employee. employeeId = {}", employeeId);
        User employee = getUserEntity(employeeId);
        if (employee.getStatus() != UserStatus.SUSPENDED) {
            throw new UserNotSuspendedException();
        }
        employee.setStatus(UserStatus.ACTIVE);
        log.info("Employee unsuspended successfully. employeeId = {}", employeeId);
    }

    public Resource getProfileImage(Long employeeId) {
        User employee = userRepository.findByIdAndOrganizationId(employeeId, currentUserService.getCurrentOrganization().getId()).orElseThrow(UserNotFoundException::new);
        return fileStorageService.get(employee.getProfileImageUrl());
    }

    @Override
    @Transactional
    public ImportEmployeeResponseDTO importEmployees(MultipartFile file) {
        EmployeeImportPreviewResponse preview = employeeImportService.readEmployees(file);
        Organization organization = currentUserService.getCurrentOrganization();
        for (EmployeeImportRowDTO dto : preview.getValidEmployees()) {
            Department department = departmentRepository.findByNameAndOrganizationId(dto.getDepartmentName(), organization.getId()).orElseThrow(DepartmentNotFoundException::new);
            User employee = modelMapper.map(dto, User.class);
            if (employee.getLastName() != null && employee.getLastName().isBlank()) {
                employee.setLastName(null);
            }
            if (employee.getPhoneNumber() != null && employee.getPhoneNumber().isBlank()) {
                employee.setPhoneNumber(null);
            }
            if (employee.getEmployeeCode() != null && employee.getEmployeeCode().isBlank()) {
                employee.setEmployeeCode(null);
            }
            employee.setDepartment(department);
            employee.setOrganization(organization);
            employee.setRole(Role.EMPLOYEE);
            employee.setStatus(UserStatus.PENDING);
            userRepository.save(employee);
            sendAccountActivationEmail(employee, organization);
        }
        return ImportEmployeeResponseDTO.builder()
                .successfulImports(preview.getValidRows())
                .failedImports(preview.getInvalidRows())
                .errors(preview.getErrors())
                .totalRows(preview.getTotalRows())
                .build();
    }

    @Override
    public Resource getImportEmployeeTemplate() {
        return fileStorageService.get("/templates/employee-template.xlsx");
    }

    @Override
    public EmployeeLearningProgressResponse getEmployeeLearningProgress(Long employeeId, MyLearningRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        User employee = userRepository.findByIdAndOrganizationId(employeeId, currentUserService.getCurrentOrganization().getId()).orElseThrow(UserNotFoundException::new);
        validateManagerAccess(employee);
        Specification<UserCourse> specification = buildLearningSpecification(employeeId, request);
        Page<UserCourse> userCourses = userCourseRepository.findAll(specification, adjustedPageable);
        return buildResponse(employee, userCourses);
    }

    private Specification<UserCourse> buildLearningSpecification(Long employeeId, MyLearningRequestDTO request) {
        Specification<UserCourse> specification = UserCourseSpecification.belongsToEmployee(employeeId);
        if (request.getSearch() != null && !request.getSearch().isBlank()) {
            specification = specification.and(UserCourseSpecification.containsKeyword(request.getSearch()));
        }
        if (request.getProgressStatus() != null) {
            specification = specification.and(UserCourseSpecification.hasProgressStatus(request.getProgressStatus()));
        }
        if (request.getRequestStatus() != null) {
            specification = specification.and(UserCourseSpecification.hasRequestStatus(request.getRequestStatus()));
        }
        if (request.getEnrollmentType() != null) {
            specification = specification.and(UserCourseSpecification.hasEnrollmentType(request.getEnrollmentType()));
        }
        return specification;
    }

    private EmployeeLearningProgressResponse buildResponse(User employee, Page<UserCourse> userCourses) {
        EmployeeLearningProgressResponse response = new EmployeeLearningProgressResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(String.join(" ", employee.getFirstName(), Optional.ofNullable(
                employee.getLastName()).orElse("")
        ).trim());
        response.setEmployeeEmail(employee.getEmail());
        response.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null);
        Page<EmployeeCourseProgressResponse> courses = userCourses.map(this::buildCourseProgressResponse);
        response.setCourses(courses);
        EmployeeLearningStatistics statistics = userCourseRepository.getEmployeeLearningStatistics(employee.getId());
        response.setTotalCourses(statistics.getTotalCourses().intValue());
        response.setCompletedCourses(statistics.getCompletedCourses().intValue());
        response.setInProgressCourses(statistics.getInProgressCourses().intValue());
        response.setNotStartedCourses(statistics.getNotStartedCourses().intValue());
        response.setOverallProgressPercent(statistics.getOverallProgressPercent());
        return response;
    }

    private EmployeeCourseProgressResponse buildCourseProgressResponse(UserCourse userCourse) {
        EmployeeCourseProgressResponse response = new EmployeeCourseProgressResponse();
        Course course = userCourse.getCourse();
        response.setCourseId(course.getId());
        response.setCourseTitle(course.getTitle());
        response.setEnrollmentType(userCourse.getEnrollmentType());
        response.setProgressPercent(userCourse.getProgressPercent());
        response.setStartedAt(userCourse.getStartedAt());
        response.setLastAccessedAt(userCourse.getLastAccessedAt());
        response.setEstimatedDurationSeconds(course.getEstimatedDurationSeconds());
        int totalModules = course.getModules().size();
        int completedModules = (int) userCourse.getUserCourseModules().stream()
                .filter(module -> module.getProgressPercent() >= 100.0)
                .count();
        int totalContents = userCourse.getUserCourseModules().stream()
                .mapToInt(module -> module.getUserModuleContents().size())
                .sum();
        int completedContents = (int) userCourse.getUserCourseModules()
                .stream()
                .flatMap(module -> module.getUserModuleContents().stream())
                .filter(content -> content.getProgress() == CompletionStatus.COMPLETED)
                .count();
        response.setTotalModules(totalModules);
        response.setCompletedModules(completedModules);
        response.setTotalContents(totalContents);
        response.setCompletedContents(completedContents);
        double progressPercent = totalContents == 0 ? 0.0 : (completedContents * 100.0) / totalContents;
        response.setProgressPercent(progressPercent);
        response.setStatus(calculateStatus(totalContents, completedContents));
        return response;
    }

    private CourseProgressStatus calculateStatus(int totalContents, int completedContents) {
        if (totalContents == 0 || completedContents == 0) {
            return CourseProgressStatus.NOT_STARTED;
        }
        if (completedContents == totalContents) {
            return CourseProgressStatus.COMPLETED;
        }
        return CourseProgressStatus.IN_PROGRESS;
    }

    private void validateManagerAccess(User employee) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.MANAGER) {
            if (!Objects.equals(currentUser.getDepartment().getId(), employee.getDepartment().getId())) {
                throw new UserNotFoundException();
            }
        }
    }

    private User getUserEntity(Long employeeId) {
        Long organizationId = currentUserService.getCurrentOrganization().getId();
        User employee = userRepository.findByIdAndOrganizationId(employeeId, organizationId).orElseThrow(UserNotFoundException::new);
        log.info("Employee retrieved successfully. employeeId = {}, organizationId = {}", employeeId, organizationId);
        return employee;
    }

    private void mapRequestToUserForUpdate(UpdateEmployeeRequestDTO request, User employee) {
        Optional.ofNullable(request.getFirstName()).ifPresent(employee::setFirstName);
        Optional.ofNullable(request.getLastName()).ifPresent(employee::setLastName);
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), employee.getId())) {
                throw new ResourceAlreadyExistsException(USER_EMAIL_EXIST);
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumberAndIsEnabledTrueAndIdNot(request.getPhoneNumber(), employee.getId())) {
                throw new ResourceAlreadyExistsException(USER_PHONE_NUMBER_EXIST);
            }
            employee.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmployeeCode() != null) {
            if (userRepository.existsByEmployeeCodeAndOrganizationIdAndIsEnabledTrueAndIdNot(request.getEmployeeCode(), employee.getOrganization().getId(), employee.getId())) {
                throw new ResourceAlreadyExistsException(USER_EMP_CODE_EXIST);
            }
            employee.setEmployeeCode(request.getEmployeeCode());
        }
        Optional.ofNullable(request.getJobTitle()).ifPresent(employee::setJobTitle);
        if (request.getDepartmentName() != null) {
            Department department = departmentRepository.findByNameAndOrganizationId(request.getDepartmentName(), employee.getOrganization().getId()).orElseThrow(DepartmentNotFoundException::new);
            if (!department.isEnabled()) {
                log.warn("Employee creation failed. Department id disabled. departmentName = {}", department.getName());
                throw new DepartmentDisableException();
            }
            employee.setDepartment(department);
        }
    }
}