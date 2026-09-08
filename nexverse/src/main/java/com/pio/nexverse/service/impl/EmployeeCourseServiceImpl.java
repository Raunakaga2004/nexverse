package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.*;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.entities.user.UserCourseModule;
import com.pio.nexverse.entities.user.UserModuleContent;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.ContentNotFoundException;
import com.pio.nexverse.exception.CourseNotAccessibleException;
import com.pio.nexverse.exception.CourseNotFoundException;
import com.pio.nexverse.repository.*;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.EmployeeCourseService;
import com.pio.nexverse.service.LearningContentService;
import com.pio.nexverse.specification.CourseSpecification;
import com.pio.nexverse.specification.UserCourseSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeCourseServiceImpl implements EmployeeCourseService {
    private final CurrentUserService currentUserService;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseModuleRepository userCourseModuleRepository;
    private final UserModuleContentRepository userModuleContentRepository;
    private final ModuleContentRepository moduleContentRepository;
    private final LearningContentService learningContentService;
    private final CourseModuleRepository courseModuleRepository;

    @Override
    public Page<BrowseCourseResponseDTO> browseCourses(BrowseCoursesRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<Course> specification = buildBrowseCourseSpecification(request);
        return courseRepository.findAll(specification, adjustedPageable).map(this::toBrowseCourseResponseDTO);
    }

    @Override
    public CourseViewResponseDTO getCourse(Long courseId) {
        User currentUser = currentUserService.getCurrentUser();
        Department currentDepartment = currentUserService.getCurrentDepartment();
        Course course = courseRepository.findByIdAndStatusAndIsEnabledTrue(courseId, ResourceCreationStatus.PUBLISHED).orElseThrow(CourseNotFoundException::new);
        validateCourseVisibility(course, currentDepartment);
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUser.getId(), courseId).orElse(null);
        return toCourseViewResponseDTO(course, userCourse);
    }

    @Override
    public LearningCourseResponseDTO getLearningCourse(Long courseId) {
        User currentUser = currentUserService.getCurrentUser();
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUser.getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        Course course = userCourse.getCourse();
        LearningCourseResponseDTO response = new LearningCourseResponseDTO();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setShortDescription(course.getShortDescription());
        response.setEstimatedDurationSeconds(course.getEstimatedDurationSeconds());
        response.setProgressPercent(userCourse.getProgressPercent());
        List<LearningModuleResponseDTO> modules = course.getModules().stream()
                .sorted(Comparator.comparing(CourseModule::getSequenceOrder))
                .map(module -> toLearningModuleResponseDTO(module, userCourse))
                .toList();
        response.setModules(modules);
        int totalContents = modules.stream().mapToInt(module -> module.getContents().size()).sum();
        int completedContents = (int) modules.stream().flatMap(module -> module.getContents().stream()).filter(LearningContentItemResponseDTO::isCompleted).count();
        response.setTotalContents(totalContents);
        response.setCompletedContents(completedContents);
        return response;
    }

    @Override
    @Transactional
    public LearningContentResponseDTO getContent(Long courseId, Long contentId) {
        User currentUser = currentUserService.getCurrentUser();
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUser.getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        userCourse.setLastAccessedAt(LocalDateTime.now());
        ModuleContent content = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, content);
        return toLearningContentResponseDTO(userCourse, content);
    }

    @Override
    public ResponseEntity<ResourceRegion> streamVideo(Long courseId, Long contentId, HttpHeaders headers) throws IOException {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUserService.getCurrentUser().getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        ModuleContent moduleContent = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, moduleContent);
        return learningContentService.streamVideo(contentId, headers);
    }

    @Override
    public ResponseEntity<Resource> getDocument(Long courseId, Long contentId) {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUserService.getCurrentUser().getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        ModuleContent moduleContent = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, moduleContent);
        return learningContentService.getDocumentContent(contentId);
    }

    @Override
    public TextContentResponseDTO getTextContent(Long courseId, Long contentId) {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUserService.getCurrentUser().getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        ModuleContent moduleContent = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, moduleContent);
        return learningContentService.getTextContent(contentId);
    }

    @Override
    @Transactional
    public void startContent(Long courseId, Long contentId) {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUserService.getCurrentUser().getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        userCourse.setCourseProgressStatus(CourseProgressStatus.IN_PROGRESS);
        ModuleContent content = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, content);
        UserCourseModule userCourseModule = getOrCreateUserCourseModule(userCourse, content.getCourseModule());
        UserModuleContent userModuleContent = getOrCreateUserModuleContent(userCourseModule, content);
        if (userModuleContent.getProgress() == CompletionStatus.NOT_STARTED) {
            userModuleContent.setProgress(CompletionStatus.IN_PROGRESS);
            userModuleContent.setStartedAt(LocalDateTime.now());
        }
        userModuleContent.setLastAccessedAt(LocalDateTime.now());
        userModuleContentRepository.save(userModuleContent);
    }

    @Override
    @Transactional
    public void completeContent(Long courseId, Long contentId) {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(currentUserService.getCurrentUser().getId(), courseId).orElseThrow(CourseNotAccessibleException::new);
        ModuleContent content = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        validateLearningContent(userCourse, content);
        UserCourseModule userCourseModule = getOrCreateUserCourseModule(userCourse, content.getCourseModule());
        UserModuleContent userModuleContent = getOrCreateUserModuleContent(userCourseModule, content);
        if (userModuleContent.getProgress() == CompletionStatus.COMPLETED) {
            return;
        }
        userModuleContent.setProgress(CompletionStatus.COMPLETED);
        userModuleContentRepository.save(userModuleContent);
        updateModuleProgress(userCourseModule);
        updateCourseProgress(userCourse);
    }

    @Override
    public Page<MyLearningCourseDTO> getMyLearningCourses(MyLearningRequestDTO request, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        User employee = currentUserService.getCurrentUser();
        Specification<UserCourse> specification = UserCourseSpecification.belongsToEmployee(employee.getId())
                .and(UserCourseSpecification.hasEnrollmentType(request.getEnrollmentType()))
                .and(UserCourseSpecification.hasProgressStatus(request.getProgressStatus()))
                .and(UserCourseSpecification.hasRequestStatus(request.getRequestStatus()))
                .and(UserCourseSpecification.containsKeyword(request.getSearch()));
        return userCourseRepository.findAll(specification, adjustedPageable).map(this::mapToMyLearningCourseDTO);
    }

    @Override
    public List<EmployeeSkillResponseDTO> getEmployeeSkills(EmployeeSkillRequestDTO request) {
        User currentUser = currentUserService.getCurrentUser();
        Specification<UserCourse> specification = UserCourseSpecification
                .belongsToEmployee(currentUser.getId())
                .and(UserCourseSpecification.isCompleted())
                .and(UserCourseSpecification.hasSkills(request.getSkillName(), request.getSkillLevel()
                ));
        List<UserCourse> userCourses = userCourseRepository.findAll(specification);
        String keyword = request.getSkillName() == null ? null : request.getSkillName().trim().toLowerCase();
        Map<Long, EmployeeSkillResponseDTO> skills = new LinkedHashMap<>();
        for (UserCourse userCourse : userCourses) {
            for (CourseSkill courseSkill : userCourse.getCourse().getCourseSkills()) {
                Skill skill = courseSkill.getSkill();
                if (keyword != null && !keyword.isBlank() && !skill.getName().toLowerCase().contains(keyword)) {
                    continue;
                }
                if (request.getSkillLevel() != null && courseSkill.getSkillLevel() != request.getSkillLevel()) {
                    continue;
                }
                EmployeeSkillResponseDTO dto = skills.computeIfAbsent(skill.getId(), id -> EmployeeSkillResponseDTO.builder()
                        .skillId(skill.getId())
                        .skillName(skill.getName())
                        .levels(new ArrayList<>())
                        .build()
                );
                Optional<EmployeeSkillLevelResponseDTO> existingLevel = dto.getLevels().stream()
                        .filter(level -> level.getLevel() == courseSkill.getSkillLevel())
                        .findFirst();
                if (existingLevel.isPresent()) {
                    existingLevel.get().setCourseCount(existingLevel.get().getCourseCount() + 1);
                } else {
                    dto.getLevels().add(EmployeeSkillLevelResponseDTO.builder()
                            .level(courseSkill.getSkillLevel())
                            .courseCount(1L)
                            .build()
                    );
                }
            }
        }
        return new ArrayList<>(skills.values());
    }

    private MyLearningCourseDTO mapToMyLearningCourseDTO(UserCourse userCourse) {
        Course course = userCourse.getCourse();
        return MyLearningCourseDTO.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .totalModules(course.getModules().size())
                .completedModules(userCourseModuleRepository.countCompletedModules(userCourse.getId()))
                .totalContents(moduleContentRepository.getTotalMandatoryContents(course.getId()))
                .completedContents(userModuleContentRepository.countCompletedMandatoryContents(userCourse.getId()))
                .progressPercent(userCourse.getProgressPercent())
                .progressStatus(userCourse.getCourseProgressStatus())
                .requestStatus(userCourse.getAccessRequestStatus())
                .enrollmentType(userCourse.getEnrollmentType())
                .lastAccessedAt(userCourse.getLastAccessedAt())
                .build();
    }

    private void updateCourseProgress(UserCourse userCourse) {
        int total = moduleContentRepository.getTotalMandatoryContents(userCourse.getCourse().getId());
        long completed = userModuleContentRepository.countCompletedMandatoryContents(userCourse.getId());
        double progress = total == 0 ? 0 : completed * 100.0 / total;
        userCourse.setProgressPercent(progress);
        if (Double.compare(userCourse.getProgressPercent(), 100) == 0) {
            userCourse.setCourseProgressStatus(CourseProgressStatus.COMPLETED);
        }
        userCourseRepository.save(userCourse);
    }

    private void updateModuleProgress(UserCourseModule userCourseModule) {
        int total = courseModuleRepository.countMandatoryContentsByModuleId(userCourseModule.getCourseModule().getId());
        long completed = userModuleContentRepository.countByUserCourseModuleIdAndProgress(userCourseModule.getId(), CompletionStatus.COMPLETED);
        double progress = total == 0 ? 0 : completed * 100.0 / total;
        userCourseModule.setProgressPercent(progress);
        userCourseModuleRepository.save(userCourseModule);
    }

    private UserModuleContent getOrCreateUserModuleContent(UserCourseModule userCourseModule, ModuleContent moduleContent) {
        return userModuleContentRepository.findByUserCourseModuleIdAndModuleContentId(userCourseModule.getId(), moduleContent.getId()).orElseGet(() -> {
            UserModuleContent userModuleContent = new UserModuleContent();
            userModuleContent.setUserCourseModule(userCourseModule);
            userModuleContent.setModuleContent(moduleContent);
            userModuleContent.setProgress(CompletionStatus.NOT_STARTED);
            return userModuleContentRepository.save(userModuleContent);
        });
    }

    private UserCourseModule getOrCreateUserCourseModule(UserCourse userCourse, CourseModule module) {
        return userCourseModuleRepository.findByUserCourseIdAndCourseModuleId(userCourse.getId(), module.getId()).orElseGet(() -> {
            UserCourseModule courseModule = new UserCourseModule();
            courseModule.setUserCourse(userCourse);
            courseModule.setCourseModule(module);
            courseModule.setProgressPercent(0d);
            return userCourseModuleRepository.save(courseModule);
        });
    }

    private LearningContentResponseDTO toLearningContentResponseDTO(UserCourse userCourse, ModuleContent content) {
        LearningContentResponseDTO response = new LearningContentResponseDTO();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setContentType(content.getContentType());
        response.setEstimatedDurationSeconds(content.getEstimatedDurationSeconds());
        response.setMandatory(content.getIsMandatory());
        response.setPreviousContentId(getPreviousContentId(content));
        response.setNextContentId(getNextContentId(content));
        UserModuleContent userModuleContent = userModuleContentRepository.findByUserCourseIdAndModuleContentId(userCourse.getId(), content.getId()).orElse(null);
        response.setProgress(userModuleContent == null ? CompletionStatus.NOT_STARTED : userModuleContent.getProgress());
        return response;
    }

    private Long getPreviousContentId(ModuleContent content) {
        return content.getCourseModule().getModuleContents().stream()
                .filter(c -> c.getSequenceOrder() < content.getSequenceOrder())
                .max(Comparator.comparing(ModuleContent::getSequenceOrder))
                .map(ModuleContent::getId)
                .orElse(null);
    }

    private Long getNextContentId(ModuleContent content) {
        return content.getCourseModule().getModuleContents().stream()
                .filter(c -> c.getSequenceOrder() > content.getSequenceOrder())
                .min(Comparator.comparing(ModuleContent::getSequenceOrder))
                .map(ModuleContent::getId)
                .orElse(null);
    }

    private void validateLearningContent(UserCourse userCourse, ModuleContent content) {
        if (!content.getCourseModule().getCourse().getId().equals(userCourse.getCourse().getId())) {
            throw new CourseNotAccessibleException();
        }
        if (userCourse.getAccessRequestStatus() != CourseAccessRequestStatus.APPROVED) {
            throw new CourseNotAccessibleException();
        }
    }

    private LearningModuleResponseDTO toLearningModuleResponseDTO(CourseModule module, UserCourse userCourse) {
        UserCourseModule userCourseModule = getOrCreateUserCourseModule(userCourse, module);
        LearningModuleResponseDTO response = new LearningModuleResponseDTO();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setSequenceOrder(module.getSequenceOrder());
        response.setProgressPercent(userCourseModule.getProgressPercent());
        List<LearningContentItemResponseDTO> contents = module.getModuleContents().stream()
                .sorted(Comparator.comparing(ModuleContent::getSequenceOrder))
                .map(content ->
                        toLearningContentItemResponseDTO(content, userCourseModule))
                .toList();
        response.setContents(contents);
        response.setCompletedContents((int) contents.stream()
                .filter(LearningContentItemResponseDTO::isCompleted)
                .count());
        response.setTotalContents(contents.size());
        return response;
    }

    private LearningContentItemResponseDTO toLearningContentItemResponseDTO(ModuleContent content, UserCourseModule userCourseModule) {
        UserModuleContent userModuleContent = getOrCreateUserModuleContent(userCourseModule, content);
        LearningContentItemResponseDTO response = new LearningContentItemResponseDTO();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setContentType(content.getContentType());
        response.setSequenceOrder(content.getSequenceOrder());
        response.setEstimatedDurationSeconds(content.getEstimatedDurationSeconds());
        response.setMandatory(content.getIsMandatory());
        response.setStarted(userModuleContent.getStartedAt() != null);
        response.setCompleted(userModuleContent.getProgress() == CompletionStatus.COMPLETED);
        return response;
    }

    private CourseViewResponseDTO toCourseViewResponseDTO(Course course, UserCourse userCourse) {
        CourseViewResponseDTO response = new CourseViewResponseDTO();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setShortDescription(course.getShortDescription());
        response.setDescription(course.getDescription());
        response.setLevel(course.getLevel());
        response.setAccessType(course.getAccessType());
        response.setVisibility(course.getVisibility());
        response.setEstimatedDurationSeconds(course.getEstimatedDurationSeconds());
        response.setPublishedAt(course.getPublishedAt());
        response.setPublisherId(course.getPublishedBy().getId());
        response.setPublisherName(course.getPublishedBy().getFirstName() + " " + course.getPublishedBy().getLastName());
        response.setSkills(course.getCourseSkills().stream()
                .map((courseSkill -> modelMapper.map(courseSkill, CourseSkillResponseDTO.class)))
                .toList()
        );
        response.setModules(course.getModules().stream()
                .sorted(Comparator.comparing(CourseModule::getSequenceOrder))
                .map(this::toCourseModuleViewDTO)
                .toList()
        );
        response.setTotalModules(course.getModules().size());
        response.setTotalContents(
                course.getModules()
                        .stream()
                        .mapToInt(module -> module.getModuleContents().size())
                        .sum()
        );
        if (userCourse != null) {
            response.setProgressPercent(userCourse.getProgressPercent());
            response.setRequestStatus(userCourse.getAccessRequestStatus());
        } else {
            response.setProgressPercent(0.0);
        }
        return response;
    }

    private CourseModuleViewDTO toCourseModuleViewDTO(CourseModule module) {
        CourseModuleViewDTO response = new CourseModuleViewDTO();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setSequenceOrder(module.getSequenceOrder());
        response.setEstimatedDurationSeconds(module.getEstimatedDurationSeconds());
        response.setTotalContents(module.getModuleContents().size());
        response.setContents(module.getModuleContents().stream()
                .sorted(Comparator.comparing(ModuleContent::getSequenceOrder))
                .map(this::toModuleContentViewDTO)
                .toList()
        );
        return response;
    }

    private ModuleContentViewDTO toModuleContentViewDTO(ModuleContent content) {
        ModuleContentViewDTO response = new ModuleContentViewDTO();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setContentType(content.getContentType());
        response.setSequenceOrder(content.getSequenceOrder());
        response.setEstimatedDurationSeconds(content.getEstimatedDurationSeconds());
        response.setMandatory(content.getIsMandatory());
        response.setCompletionStatus(CompletionStatus.NOT_STARTED);
        return response;
    }

    private void validateCourseVisibility(Course course, Department currentDepartment) {
        switch (course.getVisibility()) {
            case PRIVATE:
                throw new CourseNotFoundException();
            case ORGANIZATION:
                if (!course.getOwningDepartment().getOrganization().getId().equals(currentDepartment.getOrganization().getId())) {
                    throw new RuntimeException("You do not have access to this course.");
                }
                return;
            case DEPARTMENT:
                if (!course.getOwningDepartment().getId().equals(currentDepartment.getId())) {
                    throw new RuntimeException("You do not have access to this course.");
                }
                return;
            default:
                throw new RuntimeException("You do not have access to this course.");
        }
    }

    private Specification<Course> buildBrowseCourseSpecification(BrowseCoursesRequestDTO request) {
        return CourseSpecification.belongsToOrganization(currentUserService.getCurrentOrganization().getId())
                .and(CourseSpecification.isEnabled(true))
                .and(CourseSpecification.containsKeyword(request.getSearch()))
                .and(CourseSpecification.hasStatus(ResourceCreationStatus.PUBLISHED))
                .and(CourseSpecification.hasLevel(request.getLevel()))
                .and(CourseSpecification.isVisibleToEmployee(currentUserService.getCurrentDepartment().getId()))
                .and(CourseSpecification.hasAccessType(request.getAccessType()));
    }

    private BrowseCourseResponseDTO toBrowseCourseResponseDTO(Course course) {
        return BrowseCourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .level(course.getLevel())
                .accessType(course.getAccessType())
                .estimatedDurationSeconds(course.getEstimatedDurationSeconds())
                .department(course.getOwningDepartment().getName())
                .courseSkills(course.getCourseSkills().stream()
                        .map((courseSkill) -> modelMapper.map(courseSkill, CourseSkillResponseDTO.class))
                        .toList())
                .build();
    }
}