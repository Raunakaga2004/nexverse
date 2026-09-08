package com.pio.nexverse.service.impl;

import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.ResourceCreationStatus;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.exception.ArchivedCourseException;
import com.pio.nexverse.exception.CourseNotAccessibleException;
import com.pio.nexverse.exception.CourseNotFoundException;
import com.pio.nexverse.exception.DraftCourseException;
import com.pio.nexverse.repository.CourseRepository;
import com.pio.nexverse.service.CourseAuthorizationService;
import com.pio.nexverse.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseAuthorizationServiceImpl implements CourseAuthorizationService {
    private final CurrentUserService currentUserService;
    private final CourseRepository courseRepository;

    @Override
    public Course getCourseForView(Long courseId) {
        User currentUser = currentUserService.getCurrentUser();
        Course course = getCourse(courseId, currentUser);
        return switch (currentUser.getRole()) {
            case ADMIN -> course;
            case MANAGER -> authorizeManager(course, currentUser);
            case EMPLOYEE -> authorizeEmployee(course, currentUser);
            default -> throw new CourseNotAccessibleException();
        };
    }

    @Override
    public Course getCourseForEdit(Long courseId) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.MANAGER) {
            throw new CourseNotAccessibleException();
        }
        Course course = courseRepository.findByIdAndOwningDepartment_OrganizationId(courseId, currentUser.getOrganization().getId())
                .orElseThrow(CourseNotFoundException::new);
        if (!Objects.equals(course.getOwningDepartment().getId(), currentUser.getDepartment().getId())) {
            throw new CourseNotAccessibleException();
        }
        return course;
    }

    private void validateCourseStatus(Course course) {
        if (course.getStatus() == ResourceCreationStatus.DRAFT) {
            throw new DraftCourseException();
        }
        if (course.getStatus() == ResourceCreationStatus.ARCHIVED) {
            throw new ArchivedCourseException();
        }
    }

    private Course authorizeEmployee(Course course, User currentUser) {
        validateCourseStatus(course);
        if (course.getVisibility() == CourseVisibility.ORGANIZATION) return course;
        if (course.getVisibility() == CourseVisibility.DEPARTMENT && Objects.equals(currentUser.getDepartment().getId(), course.getOwningDepartment().getId())) {
            return course;
        } else {
            throw new CourseNotAccessibleException();
        }
    }

    private Course authorizeManager(Course course, User currentUser) {
        if (Objects.equals(currentUser.getDepartment().getId(), course.getOwningDepartment().getId())) {
            return course;
        }
        validateCourseStatus(course);
        if (course.getVisibility() == CourseVisibility.ORGANIZATION) {
            return course;
        } else {
            throw new CourseNotAccessibleException();
        }
    }

    private Course getCourse(Long courseId, User currentUser) {
        return courseRepository.findByIdAndOwningDepartment_OrganizationId(courseId, currentUser.getOrganization().getId()).orElseThrow(CourseNotFoundException::new);
    }
}