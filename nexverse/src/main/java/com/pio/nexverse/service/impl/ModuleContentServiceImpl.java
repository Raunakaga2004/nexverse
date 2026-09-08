package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.course.CourseModule;
import com.pio.nexverse.entities.course.ModuleContent;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.CourseModuleRepository;
import com.pio.nexverse.repository.CourseRepository;
import com.pio.nexverse.repository.DepartmentCourseAccessRepository;
import com.pio.nexverse.repository.ModuleContentRepository;
import com.pio.nexverse.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleContentServiceImpl implements ModuleContentService {
    private final ModelMapper modelMapper;
    private final ModuleContentRepository moduleContentRepository;
    private final FileStorageService fileStorageService;
    private final CourseModuleRepository courseModuleRepository;
    private final LearningContentService learningContentService;
    private final CurrentUserService currentUserService;
    private final DepartmentCourseAccessRepository departmentCourseAccessRepository;
    private final CourseRepository courseRepository;
    private final CourseAuthorizationService courseAuthorizationService;

    @Override
    @Transactional
    public void addContent(Long moduleId, CreateModuleContentRequestDTO request, MultipartFile content) {
        CourseModule module = getEditableModule(moduleId);
        validateCreateRequest(request, content);
        ModuleContent moduleContent = modelMapper.map(request, ModuleContent.class);
        moduleContent.setCourseModule(module);
        int sequenceOrder = moduleContentRepository.findMaxSequenceOrder(moduleId) + 1;
        moduleContent.setSequenceOrder(sequenceOrder);
        uploadFiles(moduleContent, module, content);
        moduleContentRepository.save(moduleContent);
        recalculateModuleDuration(module);
        log.info("Content created. moduleId={}, contentId={}", moduleId, moduleContent.getId());
    }

    @Override
    @Transactional
    public void updateContent(Long contentId, UpdateModuleContentRequestDTO request, MultipartFile content) {
        ModuleContent moduleContent = getModuleContent(contentId);
        CourseModule module = getEditableModule(moduleContent.getCourseModule().getId());
        validateUpdateRequest(request, content, moduleContent);
        modelMapper.map(request, moduleContent);
        updateContentFile(moduleContent, module, content);
        moduleContentRepository.save(moduleContent);
        recalculateModuleDuration(module);
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId) {
        ModuleContent moduleContent = getModuleContent(contentId);
        CourseModule module = getEditableModule(moduleContent.getCourseModule().getId());
        if (moduleContent.getContentUrl() != null) {
            fileStorageService.delete(moduleContent.getContentUrl());
        }
        Integer deletedSequenceOrder = moduleContent.getSequenceOrder();
        moduleContentRepository.delete(moduleContent);
        reorderRemainingContents(module.getId(), deletedSequenceOrder);
        recalculateModuleDuration(module);
    }

    private void reorderRemainingContents(Long moduleId, Integer deletedSequenceOrder) {
        List<ModuleContent> contents = moduleContentRepository.findByCourseModuleIdAndSequenceOrderGreaterThanOrderBySequenceOrderAsc(moduleId, deletedSequenceOrder);
        for (ModuleContent content : contents) {
            content.setSequenceOrder(content.getSequenceOrder() - 1);
        }
    }

    @Transactional
    public void reorderContents(Long moduleId, ReorderModuleContentsRequestDTO request) {
        CourseModule module = getEditableModule(moduleId);
        List<ModuleContent> existingContents = moduleContentRepository.findByCourseModuleIdOrderBySequenceOrderAsc(module.getId());
        validateReorderRequest(existingContents, request);
        Map<Long, Integer> orderMap = request.getContents().stream()
                .collect(Collectors.toMap(
                        ContentOrderDTO::getContentId,
                        ContentOrderDTO::getSequenceOrder
                ));
        for (ModuleContent content : existingContents) {
            content.setSequenceOrder(-(content.getId().intValue()));
        }
        moduleContentRepository.saveAllAndFlush(existingContents);
        for (ModuleContent content : existingContents) {
            content.setSequenceOrder(orderMap.get(content.getId()));
        }
        moduleContentRepository.saveAll(existingContents);
    }

    private void validateReorderRequest(List<ModuleContent> existingContents, ReorderModuleContentsRequestDTO request) {
        if (existingContents.size() != request.getContents().size()) {
            throw new InvalidContentOrderException("All module contents must be included in the reorder request");
        }
        Set<Long> existingContentIds = existingContents.stream()
                .map(ModuleContent::getId)
                .collect(Collectors.toSet());
        List<Long> requestedContentIds = request.getContents().stream()
                .map(ContentOrderDTO::getContentId)
                .toList();
        if (requestedContentIds.size() != new HashSet<>(requestedContentIds).size()) {
            throw new InvalidContentOrderException("Duplicate content IDs are not allowed");
        }
        if (!existingContentIds.equals(new HashSet<>(requestedContentIds))) {
            throw new InvalidContentOrderException("Invalid content IDs for this module");
        }
        Set<Integer> requestedOrders = request.getContents().stream()
                .map(ContentOrderDTO::getSequenceOrder)
                .collect(Collectors.toSet());
        Set<Integer> expectedOrders = IntStream.rangeClosed(1, existingContents.size())
                .boxed()
                .collect(Collectors.toSet());
        if (!requestedOrders.equals(expectedOrders)) {
            throw new InvalidContentOrderException("Content order must contain consecutive values starting from 1");
        }
    }

    @Override
    public ModuleContentResponseDTO getContent(Long contentId) {
        if (notHaveCourseAccess(contentId)) {
            throw new CourseNotAccessibleException();
        }
        ModuleContent moduleContent = moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
        return modelMapper.map(moduleContent, ModuleContentResponseDTO.class);
    }

    private boolean notHaveCourseAccess(Long contentId) {
        Course course = courseRepository.findByContentIdAndOrganizationId(contentId, currentUserService.getCurrentOrganization().getId()).orElseThrow(CourseNotFoundException::new);
        if (currentUserService.getCurrentUser().getRole() == Role.ADMIN) return false;
        Long currentDeptId = currentUserService.getCurrentDepartment().getId();
        if (Objects.equals(course.getOwningDepartment().getId(), currentDeptId))
            return false;
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) return true;
        boolean approvedAccess = departmentCourseAccessRepository.existsByCourseIdAndRequestingDepartmentIdAndRequestStatus(
                course.getId(),
                currentDeptId,
                CourseAccessRequestStatus.APPROVED
        );
        return !approvedAccess && course.getAccessType() != CourseAccessType.OPEN;
    }

    @Override
    public ResponseEntity<Resource> getDocumentContent(Long contentId) {
        if (notHaveCourseAccess(contentId)) {
            throw new CourseNotAccessibleException();
        }
        return learningContentService.getDocumentContent(contentId);
    }

    @Override
    public ResponseEntity<ResourceRegion> streamVideo(Long contentId, HttpHeaders headers) throws IOException {
        if (notHaveCourseAccess(contentId)) {
            throw new CourseNotAccessibleException();
        }
        return learningContentService.streamVideo(contentId, headers);
    }

    @Override
    public TextContentResponseDTO getTextContent(Long contentId) {
        if (notHaveCourseAccess(contentId)) {
            throw new CourseNotAccessibleException();
        }
        return learningContentService.getTextContent(contentId);
    }

    private void updateContentFile(ModuleContent content, CourseModule module, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        Organization organization = module.getCourse().getOwningDepartment().getOrganization();
        String newUrl = fileStorageService.upload(file, organization.getId(), OrganizationFileType.COURSE_CONTENT);
        String oldUrl = content.getContentUrl();
        content.setContentUrl(newUrl);
        content.setFormat(file.getContentType());
        if (oldUrl != null) {
            fileStorageService.delete(oldUrl);
        }
    }

    private void validateUpdateRequest(UpdateModuleContentRequestDTO request, MultipartFile file, ModuleContent existingContent) {
        switch (request.getContentType()) {
            case VIDEO, DOCUMENT -> {
                boolean hasExistingFile = existingContent.getContentUrl() != null;
                boolean hasUploadedFile = file != null && !file.isEmpty();
                if (!hasExistingFile && !hasUploadedFile) {
                    throw new InvalidModuleContentException("Content file is required");
                }
                request.setTextBody(null);
            }
            case TEXT -> {
                validateTextContent(request.getTextBody());
                moduleContentCleanup(existingContent);
            }
            default -> throw new InvalidModuleContentException("Unsupported content type");
        }
    }

    private void moduleContentCleanup(ModuleContent moduleContent) {
        if (moduleContent.getContentUrl() != null) {
            fileStorageService.delete(moduleContent.getContentUrl());
            moduleContent.setContentUrl(null);
            moduleContent.setFormat(null);
        }
    }

    private ModuleContent getModuleContent(Long contentId) {
        return moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
    }

    private void uploadFiles(ModuleContent moduleContent, CourseModule module, MultipartFile content) {
        Organization organization = module.getCourse().getOwningDepartment().getOrganization();
        if (content != null && !content.isEmpty()) {
            String contentUrl = fileStorageService.upload(content, organization.getId(), OrganizationFileType.COURSE_CONTENT);
            moduleContent.setContentUrl(contentUrl);
            moduleContent.setFormat(content.getContentType());
        }
    }

    private void validateCreateRequest(CreateModuleContentRequestDTO request, MultipartFile file
    ) {
        switch (request.getContentType()) {
            case VIDEO, DOCUMENT -> validateFileContent(file);
            case TEXT -> validateTextContent(request.getTextBody());
            default -> throw new InvalidModuleContentException("Unsupported content type");
        }
    }

    private void validateFileContent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidModuleContentException("Content file is required");
        }
    }

    private void validateTextContent(String textBody) {
        if (textBody == null || textBody.isBlank()) {
            throw new InvalidModuleContentException("Text content is required");
        }
    }

    private CourseModule getEditableModule(Long moduleId) {
        CourseModule module = courseModuleRepository.findById(moduleId).orElseThrow(CourseModuleNotFoundException::new);
        Course course = courseAuthorizationService.getCourseForEdit(module.getCourse().getId());
        if (course.getStatus() != ResourceCreationStatus.DRAFT) {
            throw new CourseNotInDraftException();
        }
        return module;
    }

    private void recalculateModuleDuration(CourseModule module) {
        int duration = moduleContentRepository.sumEstimatedDurationByModuleId(module.getId());
        module.setEstimatedDurationSeconds(duration);
        recalculateCourseDuration(module.getCourse());
    }

    private void recalculateCourseDuration(Course course) {
        int duration = courseModuleRepository.sumEstimatedDurationByCourseId(course.getId());
        course.setEstimatedDurationSeconds(duration);
    }
}