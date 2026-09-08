package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.*;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.CourseRepository;
import com.pio.nexverse.repository.DepartmentCourseAccessRepository;
import com.pio.nexverse.repository.SkillRepository;
import com.pio.nexverse.service.CourseAuthorizationService;
import com.pio.nexverse.service.CourseService;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.FileStorageService;
import com.pio.nexverse.specification.CourseSpecification;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;
    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;
    private final DepartmentCourseAccessRepository departmentCourseAccessRepository;
    private final CourseAuthorizationService courseAuthorizationService;

    @Transactional
    public void createCourse(CreateCourseRequestDTO request, MultipartFile thumbnail) {
        Department owningDepartment = currentUserService.getCurrentDepartment();
        Course course = modelMapper.map(request, Course.class);
        course.setCourseSkills(null);
        String thumbnailPath = fileStorageService.upload(thumbnail, currentUserService.getCurrentOrganization().getId(), OrganizationFileType.COURSE_THUMBNAIL);
        course.setThumbnailUrl(thumbnailPath);
        course.setCreatedBy(currentUserService.getCurrentUser());
        course.setOwningDepartment(owningDepartment);
        course.setStatus(ResourceCreationStatus.DRAFT);
        courseRepository.save(course);
        if (request.getCourseSkills() != null) {
            for (CourseSkillRequestDTO courseSkillRequest : request.getCourseSkills()) {
                addCourseSkill(course, courseSkillRequest);
            }
        }
        courseRepository.save(course);
    }

    private void addCourseSkill(Course course, CourseSkillRequestDTO courseSkillRequest) {
        Skill skill = skillRepository.findByIdAndOrganizationId(courseSkillRequest.getSkillId(), currentUserService.getCurrentOrganization().getId()).orElseThrow(SkillNotFoundException::new);
        CourseSkill courseSkill = new CourseSkill();
        courseSkill.setCourse(course);
        courseSkill.setSkill(skill);
        courseSkill.setSkillLevel(courseSkillRequest.getSkillLevel());
        courseSkill.setCreatedBy(currentUserService.getCurrentUser());
        if (course.getCourseSkills() == null) {
            course.setCourseSkills(new ArrayList<>());
        }
        course.getCourseSkills().add(courseSkill);
        courseSkill.setCourse(course);
    }

    @Transactional
    public void updateCourse(Long courseId, UpdateCourseRequestDTO request, MultipartFile thumbnail) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() != ResourceCreationStatus.DRAFT) {
            throw new CourseNotInDraftException();
        }
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String oldThumbnail = course.getThumbnailUrl();
            String newThumbnail = fileStorageService.upload(thumbnail, currentUserService.getCurrentOrganization().getId(), OrganizationFileType.COURSE_THUMBNAIL);
            course.setThumbnailUrl(newThumbnail);
            if (oldThumbnail != null && !oldThumbnail.isBlank()) {
                fileStorageService.delete(oldThumbnail);
            }
        }
        mapRequestToCourseForUpdate(request, course);
        if (request.getCourseSkills() != null) {
            updateCourseSkills(course, request.getCourseSkills());
        }
        course.setUpdatedBy(currentUserService.getCurrentUser());
    }

    private void updateCourseSkills(Course course, List<CourseSkillRequestDTO> requestedSkills) {
        if (course.getCourseSkills() == null) {
            course.setCourseSkills(new ArrayList<>());
        }
        Map<Long, CourseSkill> existingSkills = course.getCourseSkills().stream().collect(Collectors.toMap(cs -> cs.getSkill().getId(), Function.identity()));
        Set<Long> requestedSkillIds = new HashSet<>();
        for (CourseSkillRequestDTO dto : requestedSkills) {
            requestedSkillIds.add(dto.getSkillId());
            CourseSkill existing = existingSkills.get(dto.getSkillId());
            if (existing != null) {
                existing.setSkillLevel(dto.getSkillLevel());
            } else {
                addCourseSkill(course, dto);
            }
        }
        course.getCourseSkills().removeIf(cs -> !requestedSkillIds.contains(cs.getSkill().getId()));
    }

    @Transactional
    public Page<CoursesResponseDTO> getCourses(CourseSearchRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<Course> specification = buildCourseSearchSpecification(request);
        Page<Course> courses = courseRepository.findAll(specification, adjustedPageable);
        return courses.map(course -> {
            CoursesResponseDTO courseResponse = modelMapper.map(course, CoursesResponseDTO.class);
            courseResponse.setOwningDepartment(course.getOwningDepartment().getName());
            courseResponse.setHasAccess(hasAccessOfCourse(course));
            return courseResponse;
        });
    }

    private Specification<Course> buildCourseSearchSpecification(CourseSearchRequestDTO request) {
        Specification<Course> specification = CourseSpecification.belongsToOrganization(currentUserService.getCurrentOrganization().getId())
                .and(CourseSpecification.containsKeyword(request.getSearch()))
                .and(CourseSpecification.hasStatus(request.getStatus()))
                .and(CourseSpecification.hasLevel(request.getLevel()));
        if (currentUserService.getCurrentUser().getRole() == Role.MANAGER) {
            Long currentDepartmentId = currentUserService.getCurrentDepartment().getId();
            if (request.getVisibility() == null) {
                specification = specification.and((CourseSpecification.hasVisibility(CourseVisibility.ORGANIZATION)
                        .and(CourseSpecification.hasStatus(ResourceCreationStatus.PUBLISHED)))
                        .or(CourseSpecification.belongsToDepartment(currentDepartmentId)));
            } else if (request.getVisibility() == CourseVisibility.ORGANIZATION) {
                specification = specification.and(CourseSpecification.hasVisibility(CourseVisibility.ORGANIZATION))
                        .and(CourseSpecification.hasStatus(ResourceCreationStatus.PUBLISHED)
                                .or(CourseSpecification.belongsToDepartment(currentDepartmentId)));
            } else {
                specification = specification.and((CourseSpecification.belongsToDepartment(currentDepartmentId)
                        .and(CourseSpecification.hasVisibility(request.getVisibility())))
                );
            }
        } else {
            specification = specification.and(CourseSpecification.hasVisibility(request.getVisibility()));
            specification = specification.and((CourseSpecification.hasStatus(ResourceCreationStatus.PUBLISHED)
                    .or(CourseSpecification.hasStatus(ResourceCreationStatus.ARCHIVED))));
        }
        return specification.and(CourseSpecification.hasAccessType(request.getAccessType()))
                .and(CourseSpecification.hasSkill(request.getSkillId()));
    }

    private boolean hasAccessOfCourse(Course course) {
        if (currentUserService.getCurrentUser().getRole() == Role.ADMIN) return true;
        if (Objects.equals(course.getOwningDepartment().getId(), currentUserService.getCurrentDepartment().getId()))
            return true;
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) return false;
        return departmentCourseAccessRepository.existsByCourseIdAndRequestingDepartmentIdAndRequestStatus(course.getId(), currentUserService.getCurrentDepartment().getId(), CourseAccessRequestStatus.APPROVED)
                || (course.getAccessType() == CourseAccessType.OPEN);
    }

    @Override
    public CourseResponseDTO getCourse(Long courseId) {
        Course course = courseAuthorizationService.getCourseForView(courseId);
        CourseResponseDTO courseResponse = modelMapper.map(course, CourseResponseDTO.class);
        courseResponse.setHasAccess(hasAccessOfCourse(course));
        DepartmentCourseAccess departmentCourseAccess = departmentCourseAccessRepository.findByCourseIdAndRequestingDepartmentId(courseId, currentUserService.getCurrentDepartment().getId()).orElseThrow(DepartmentNotFoundException::new);
        courseResponse.setRequestStatus(departmentCourseAccess.getRequestStatus());
        return courseResponse;
    }

    @Override
    public Resource getCourseThumbnail(Long courseId) {
        Course course = courseAuthorizationService.getCourseForView(courseId);
        if (course.getThumbnailUrl() == null || course.getThumbnailUrl().isBlank()) {
            throw new CourseThumbnailNotFoundException();
        }
        return fileStorageService.get(course.getThumbnailUrl());
    }

    @Transactional
    public void publishCourse(Long courseId) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() != ResourceCreationStatus.DRAFT) {
            throw new CourseNotInDraftException();
        }
        validateCourseForPublishing(course);
        course.setStatus(ResourceCreationStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        course.setPublishedBy(currentUserService.getCurrentUser());
    }

    private void validateCourseForPublishing(Course course) {
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new InvalidCoursePublishException("Course title is required.");
        }
        if (course.getDescription() == null || course.getDescription().isBlank()) {
            throw new InvalidCoursePublishException("Course description is required.");
        }
        if (course.getThumbnailUrl() == null || course.getThumbnailUrl().isBlank()) {
            throw new InvalidCoursePublishException("Course thumbnail is required.");
        }
        if (course.getEstimatedDurationSeconds() == null || course.getEstimatedDurationSeconds() <= 0) {
            throw new InvalidCoursePublishException("Estimated learning time must be greater than zero.");
        }
        if (course.getCourseSkills() == null || course.getCourseSkills().isEmpty()) {
            throw new InvalidCoursePublishException("At least one skill must be mapped.");
        }
        if (course.getModules() == null || course.getModules().isEmpty()) {
            throw new InvalidCoursePublishException("Course must contain at least one module.");
        }
        for (CourseModule module : course.getModules()) {
            if (module.getTitle() == null || module.getTitle().isBlank()) {
                throw new InvalidCoursePublishException("Every module must have a title.");
            }
            if (module.getModuleContents().isEmpty()) {
                throw new InvalidCoursePublishException("Module '" + module.getTitle() + "' must contain at least one content.");
            }
            for (ModuleContent content : module.getModuleContents()) {
                if (content.getTitle() == null || content.getTitle().isBlank()) {
                    throw new InvalidCoursePublishException("Every content must have a title.");
                }
                switch (content.getContentType()) {
                    case VIDEO -> {
                        if (content.getContentUrl() == null || content.getContentUrl().isBlank()) {
                            throw new InvalidCoursePublishException("Video content '" + content.getTitle() + "' has no uploaded video.");
                        }
                        if (content.getEstimatedDurationSeconds() == null || content.getEstimatedDurationSeconds() <= 0) {
                            throw new InvalidCoursePublishException("Video '" + content.getTitle() + "' must have a valid duration.");
                        }
                    }
                    case DOCUMENT -> {
                        if (content.getContentUrl() == null || content.getContentUrl().isBlank()) {
                            throw new InvalidCoursePublishException("Document '" + content.getTitle() + "' has no uploaded document.");
                        }
                    }
                    case TEXT -> {
                        if (content.getTextBody() == null || content.getTextBody().isBlank()) {
                            throw new InvalidCoursePublishException("Text content '" + content.getTitle() + "' cannot be empty.");
                        }
                    }
                    default -> throw new InvalidCoursePublishException("Unsupported content type.");
                }
            }
        }
    }

    @Transactional
    public void archiveCourse(Long courseId) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() == ResourceCreationStatus.ARCHIVED) {
            throw new ArchivedCourseException();
        }
        if (course.getStatus() == ResourceCreationStatus.DRAFT) {
            throw new DraftCourseException();
        }
        course.setStatus(ResourceCreationStatus.ARCHIVED);
    }

    @Transactional
    public void restoreCourse(Long courseId) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() == ResourceCreationStatus.PUBLISHED) {
            throw new AlreadyPublishedCourseException();
        }
        if (course.getStatus() == ResourceCreationStatus.DRAFT) {
            throw new DraftCourseException();
        }
        course.setStatus(ResourceCreationStatus.PUBLISHED);
    }

    @Transactional
    public void deleteDraftCourse(Long courseId) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() == ResourceCreationStatus.DRAFT) {
            courseRepository.delete(course);
        } else {
            throw new CourseNotInDraftException();
        }
    }

    private void mapRequestToCourseForUpdate(UpdateCourseRequestDTO request, Course course) {
        Optional.ofNullable(request.getTitle()).ifPresent(course::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(course::setDescription);
        Optional.ofNullable(request.getShortDescription()).ifPresent(course::setShortDescription);
        Optional.ofNullable(request.getVisibility()).ifPresent(course::setVisibility);
        Optional.ofNullable(request.getAccessType()).ifPresent(course::setAccessType);
        Optional.ofNullable(request.getLevel()).ifPresent(course::setLevel);
    }
}