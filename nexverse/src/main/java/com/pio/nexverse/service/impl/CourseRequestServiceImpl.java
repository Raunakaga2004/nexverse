package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.CourseRepository;
import com.pio.nexverse.repository.DepartmentCourseAccessRepository;
import com.pio.nexverse.repository.UserCourseRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.service.CourseAuthorizationService;
import com.pio.nexverse.service.CourseRequestService;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.specification.DepartmentCourseAccessSpecification;
import com.pio.nexverse.specification.UserCourseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRequestServiceImpl implements CourseRequestService {
    private final CourseAuthorizationService courseAuthorizationService;
    private final CurrentUserService currentUserService;
    private final DepartmentCourseAccessRepository departmentCourseAccessRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void accessCourseForDepartment(Long courseId, CourseAccessRequestDTO courseAccessRequest) {
        Course course = courseAuthorizationService.getCourseForView(courseId);
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) {
            throw new CourseNotAccessibleException();
        }
        if (!Objects.equals(currentUserService.getCurrentDepartment().getId(), course.getOwningDepartment().getId())) {
            if (course.getAccessType() == CourseAccessType.OPEN) {
                throw new CourseAlreadyAccessibleException();
            }
            DepartmentCourseAccess departmentCourseAccess = departmentCourseAccessRepository.findByCourseIdAndRequestingDepartmentId(course.getId(), currentUserService.getCurrentDepartment().getId()).orElse(new DepartmentCourseAccess());
            departmentCourseAccess.setRequestStatus(CourseAccessRequestStatus.PENDING);
            departmentCourseAccess.setRequestNote(courseAccessRequest.getReason());
            departmentCourseAccess.setCourse(course);
            departmentCourseAccess.setRequestedBy(currentUserService.getCurrentUser());
            departmentCourseAccess.setRequestingDepartment(currentUserService.getCurrentDepartment());
            departmentCourseAccess.setRequestedToDepartment(course.getOwningDepartment());
            departmentCourseAccessRepository.save(departmentCourseAccess);
        } else {
            throw new CourseAlreadyAccessibleException();
        }
    }

    @Transactional
    public void accessCourseForEmployee(Long courseId, CourseAccessRequestDTO courseAccessRequest) {
        Course course = courseAuthorizationService.getCourseForView(courseId);
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) {
            throw new CourseNotAccessibleException();
        }
        User currentUser = currentUserService.getCurrentUser();
        Department currentDepartment = currentUserService.getCurrentDepartment();
        if (course.getAccessType() == CourseAccessType.OPEN) {
            if (userCourseRepository.existsByUserIdAndCourseId(
                    currentUser.getId(),
                    course.getId())) {
                throw new CourseAlreadyAccessibleException();
            }
            enrollUser(course, currentUser, currentDepartment);
        } else createAccessRequest(course, currentUser, currentDepartment, courseAccessRequest);
    }

    @Override
    public Page<DepartmentCourseRequestsResponseDTO> getAllRequestsOfDepartments(CourseRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<DepartmentCourseAccess> specification = DepartmentCourseAccessSpecification.hasDepartment(currentUserService.getCurrentDepartment().getId())
                .and(DepartmentCourseAccessSpecification.hasSearch(request.getSearch()))
                .and(DepartmentCourseAccessSpecification.hasRequestStatus(request.getRequestStatus()));
        Page<DepartmentCourseAccess> allDepartmentCourseAccessRequests = departmentCourseAccessRepository.findAll(specification, adjustedPageable);
        return allDepartmentCourseAccessRequests.map(departmentCourseAccess -> {
            DepartmentCourseRequestsResponseDTO courseRequestsResponse = new DepartmentCourseRequestsResponseDTO();
            courseRequestsResponse.setId(departmentCourseAccess.getId());
            courseRequestsResponse.setRequestNote(departmentCourseAccess.getRequestNote());
            courseRequestsResponse.setRequestStatus(departmentCourseAccess.getRequestStatus());
            Course requestedCourse = departmentCourseAccess.getCourse();
            courseRequestsResponse.setCourseId(requestedCourse.getId());
            courseRequestsResponse.setCourseName(requestedCourse.getTitle());
            User requestBy = departmentCourseAccess.getRequestedBy();
            courseRequestsResponse.setRequestedBy(requestBy.getFirstName() + " " + requestBy.getLastName());
            courseRequestsResponse.setRequestingDepartment(departmentCourseAccess.getRequestingDepartment().getName());
            return courseRequestsResponse;
        });
    }

    @Override
    public Page<EmployeeCourseRequestsResponseDTO> getAllRequestsOfEmployees(CourseRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<UserCourse> specification = UserCourseSpecification.hasEnrollmentType(EnrollmentType.SELF_REQUESTED)
                .and(UserCourseSpecification.belongsToCourseOwningDepartment(currentUserService.getCurrentDepartment().getId()))
                .and(UserCourseSpecification.containsKeyword(request.getSearch()))
                .and(UserCourseSpecification.hasRequestStatus(request.getRequestStatus()));
        Page<UserCourse> userCourses = userCourseRepository.findAll(specification, adjustedPageable);
        return userCourses.map((userCourse -> {
                    EmployeeCourseRequestsResponseDTO response = new EmployeeCourseRequestsResponseDTO();
                    response.setId(userCourse.getId());
                    response.setRequestNote(userCourse.getRequestNote());
                    User requestedBy = userCourse.getUser();
                    response.setEmployeeId(requestedBy.getId());
                    response.setEmployeeName(requestedBy.getFirstName() + " " + requestedBy.getLastName());
                    Course requestedCourse = userCourse.getCourse();
                    response.setCourseId(requestedCourse.getId());
                    response.setCourseName(requestedCourse.getTitle());
                    response.setAccessRequestStatus(userCourse.getAccessRequestStatus());
                    response.setDepartment(requestedBy.getDepartment().getName());
                    return response;
                })
        );
    }

    @Transactional
    public void changeRequestStatusForDepartment(Long requestId, ChangeRequestStatusDTO changeRequestStatus) {
        DepartmentCourseAccess departmentCourseAccess = departmentCourseAccessRepository.findById(requestId).orElseThrow(CourseRequestNotFoundException::new);
        Course course = departmentCourseAccess.getCourse();
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) {
            throw new CourseNotAccessibleException();
        }
        if (!Objects.equals(course.getOwningDepartment().getId(), currentUserService.getCurrentDepartment().getId())) {
            throw new CourseNotAccessibleException();
        }
        departmentCourseAccess.setRequestStatus(changeRequestStatus.getStatus());
    }

    @Transactional
    public void changeRequestStatusForEmployee(Long requestId, ChangeRequestStatusDTO changeRequestStatus) {
        UserCourse userCourse = userCourseRepository.findById(requestId).orElseThrow(CourseRequestNotFoundException::new);
        Course course = userCourse.getCourse();
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) {
            throw new CourseNotAccessibleException();
        }
        if (!Objects.equals(course.getOwningDepartment().getId(), currentUserService.getCurrentDepartment().getId())) {
            throw new CourseNotAccessibleException();
        }
        userCourse.setAccessRequestStatus(changeRequestStatus.getStatus());
    }

    @Transactional
    public void assignCourseToEmployees(AssignCourseRequestDTO request) {
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(CourseNotFoundException::new);
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) {
            throw new CourseNotAccessibleException();
        }
        if (!Objects.equals(course.getOwningDepartment().getId(), currentUserService.getCurrentDepartment().getId())) {
            if (!departmentCourseAccessRepository.existsByCourseIdAndRequestingDepartmentIdAndRequestStatus(course.getId(), currentUserService.getCurrentDepartment().getId(), CourseAccessRequestStatus.APPROVED)) {
                throw new CourseNotAccessibleException();
            }
        }
        List<User> employees = userRepository.findAllById(request.getEmployeesId());
        if (employees.size() != request.getEmployeesId().size()) {
            throw new UserNotFoundException();
        }
        for (User employee : employees) {
            boolean alreadyAssigned = userCourseRepository.existsByUserIdAndCourseId(employee.getId(), course.getId());
            if (alreadyAssigned) {
                continue;
            }
            UserCourse employeeCourse = new UserCourse();
            employeeCourse.setUser(employee);
            employeeCourse.setCourse(course);
            employeeCourse.setDepartment(currentUserService.getCurrentDepartment());
            employeeCourse.setEnrollmentType(EnrollmentType.DEPARTMENT_ASSIGNED);
            employeeCourse.setAccessRequestStatus(CourseAccessRequestStatus.APPROVED);
            employeeCourse.setCourseProgressStatus(CourseProgressStatus.NOT_STARTED);
            userCourseRepository.save(employeeCourse);
        }
    }

    private void enrollUser(Course course, User user, Department department) {
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setDepartment(department);
        userCourse.setCourse(course);
        userCourse.setEnrollmentType(EnrollmentType.SELF_ENROLLED);
        userCourse.setAccessRequestStatus(CourseAccessRequestStatus.APPROVED);
        userCourse.setCourseProgressStatus(CourseProgressStatus.NOT_STARTED);
        userCourse.setStartedAt(LocalDateTime.now());
        userCourseRepository.save(userCourse);
    }

    private void createAccessRequest(Course course, User user, Department department, CourseAccessRequestDTO courseAccessRequest) {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(user.getId(), course.getId()).orElse(new UserCourse());
        userCourse.setUser(user);
        userCourse.setDepartment(department);
        userCourse.setCourse(course);
        userCourse.setEnrollmentType(EnrollmentType.SELF_REQUESTED);
        userCourse.setAccessRequestStatus(CourseAccessRequestStatus.PENDING);
        userCourse.setCourseProgressStatus(CourseProgressStatus.NOT_STARTED);
        if (courseAccessRequest.getReason() != null && !courseAccessRequest.getReason().isBlank()) {
            userCourse.setRequestNote(courseAccessRequest.getReason());
        }
        userCourseRepository.save(userCourse);
    }
}