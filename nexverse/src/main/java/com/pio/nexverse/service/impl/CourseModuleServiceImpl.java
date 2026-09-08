package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.course.CourseModule;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.CourseModuleRepository;
import com.pio.nexverse.repository.CourseRepository;
import com.pio.nexverse.repository.DepartmentCourseAccessRepository;
import com.pio.nexverse.service.CourseAuthorizationService;
import com.pio.nexverse.service.CourseModuleService;
import com.pio.nexverse.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseModuleServiceImpl implements CourseModuleService {
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;
    private final DepartmentCourseAccessRepository departmentCourseAccessRepository;
    private final CourseAuthorizationService courseAuthorizationService;

    @Transactional
    public void createModule(Long courseId, CreateCourseModuleRequestDTO request) {
        Course course = getDraftCourseForModification(courseId);
        CourseModule courseModule = modelMapper.map(request, CourseModule.class);
        courseModule.setCourse(course);
        int maxSequenceOrder = courseModuleRepository.  findMaxSequenceOrderByCourseId(courseId);
        courseModule.setSequenceOrder(maxSequenceOrder + 1);
        courseModule.setEstimatedDurationSeconds(0);
        User currentUser = currentUserService.getCurrentUser();
        course.setUpdatedBy(currentUser);
        courseModule.setCreatedBy(currentUser);
        courseModule.setUpdatedBy(currentUser);
        courseModuleRepository.save(courseModule);
    }

    @Override
    public List<CourseModuleResponseDTO> getModules(Long courseId) {
        Course course = courseAuthorizationService.getCourseForView(courseId);
        List<CourseModule> courseModules = courseModuleRepository.findAllByCourseId(course.getId());
        return courseModules.stream().map((courseModule -> mapToCourseModuleResponse(courseModule, course))).collect(Collectors.toList());
    }

    @Override
    public CourseModuleResponseDTO getModule(Long moduleId) {
        CourseModule courseModule = courseModuleRepository.findById(moduleId).orElseThrow(CourseModuleNotFoundException::new);
        Course course = courseAuthorizationService.getCourseForView(courseModule.getCourse().getId());
        return mapToCourseModuleResponse(courseModule, course);
    }

    private CourseModuleResponseDTO mapToCourseModuleResponse(CourseModule courseModule, Course course) {
        CourseModuleResponseDTO courseModuleResponse = modelMapper.map(courseModule, CourseModuleResponseDTO.class);
        courseModuleResponse.setNumberOfContents(courseModule.getModuleContents().size());
        courseModuleResponse.setCourseStatus(course.getStatus());
        courseModuleResponse.setHasAccess(hasAccessOfCourse(courseModule.getId()));
        courseModuleResponse.setContents(courseModule.getModuleContents().stream()
                .map(moduleContent -> modelMapper.map(moduleContent, ModuleContentResponseDTO.class))
                .toList()
        );
        return courseModuleResponse;
    }

    private boolean hasAccessOfCourse(Long moduleId) {
        Course course = courseRepository.findByModuleId(moduleId).orElseThrow(CourseNotFoundException::new);
        if (currentUserService.getCurrentUser().getRole() == Role.ADMIN) {
            return course.getStatus() != ResourceCreationStatus.DRAFT;
        }
        if (Objects.equals(course.getOwningDepartment().getId(), currentUserService.getCurrentDepartment().getId()))
            return true;
        if (course.getStatus() != ResourceCreationStatus.PUBLISHED) return false;
        if (course.getAccessType() == CourseAccessType.OPEN) return true;
        return departmentCourseAccessRepository.existsByCourseIdAndRequestingDepartmentIdAndRequestStatus(course.getId(), currentUserService.getCurrentDepartment().getId(), CourseAccessRequestStatus.APPROVED);
    }

    @Transactional
    public void updateModule(Long moduleId, UpdateCourseModuleRequestDTO request) {
        CourseModule courseModule = courseModuleRepository.findById(moduleId).orElseThrow(CourseModuleNotFoundException::new);
        getDraftCourseForModification(courseModule.getCourse().getId());
        mapRequestToCourseModuleForUpdate(request, courseModule);
        courseModule.setUpdatedBy(currentUserService.getCurrentUser());
    }

    private void mapRequestToCourseModuleForUpdate(UpdateCourseModuleRequestDTO request, CourseModule courseModule) {
        Optional.ofNullable(request.getTitle()).ifPresent(courseModule::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(courseModule::setDescription);
        Optional.ofNullable(request.getIsMandatory()).ifPresent(courseModule::setIsMandatory);
    }

    @Transactional
    public void deleteModule(Long moduleId) {
        CourseModule courseModule = courseModuleRepository.findById(moduleId).orElseThrow(CourseModuleNotFoundException::new);
        getDraftCourseForModification(courseModule.getCourse().getId());
        courseModuleRepository.deleteById(moduleId);
    }

    @Transactional
    @Override
    public void reorderModule(Long courseId, ReorderModuleRequestDTO request) {
        Course course = getDraftCourseForModification(courseId);
        List<CourseModule> existingModules = courseModuleRepository.findAllByCourseIdOrderBySequenceOrderAsc(course.getId());
        if (existingModules.size() != request.getModules().size()) {
            throw new InvalidModuleOrderException("All course modules must be included in the reorder request");
        }
        Set<Long> existingModuleIds = existingModules.stream()
                .map(CourseModule::getId)
                .collect(Collectors.toSet());
        List<Long> requestedModuleIds = request.getModules().stream()
                .map(ModuleOrderDTO::getModuleId)
                .toList();
        if (requestedModuleIds.size() != new HashSet<>(requestedModuleIds).size()) {
            throw new InvalidModuleOrderException("Duplicate module IDs are not allowed");
        }
        if (!existingModuleIds.equals(new HashSet<>(requestedModuleIds))) {
            throw new InvalidModuleOrderException("Invalid module IDs for this course");
        }
        Set<Integer> requestedOrders = request.getModules().stream()
                .map(ModuleOrderDTO::getSequenceOrder)
                .collect(Collectors.toSet());
        Set<Integer> expectedOrders = IntStream
                .rangeClosed(1, existingModules.size())
                .boxed()
                .collect(Collectors.toSet());
        if (!requestedOrders.equals(expectedOrders)) {
            throw new InvalidModuleOrderException("Module order must contain consecutive values starting from 1");
        }
        Map<Long, Integer> orderMap = request.getModules().stream()
                .collect(Collectors.toMap(
                        ModuleOrderDTO::getModuleId,
                        ModuleOrderDTO::getSequenceOrder
                ));
        for (CourseModule module : existingModules) {
            module.setSequenceOrder(-(module.getId().intValue()));
        }
        courseModuleRepository.saveAllAndFlush(existingModules);
        for (CourseModule module : existingModules) {
            module.setSequenceOrder(
                    orderMap.get(module.getId())
            );
        }
        courseModuleRepository.saveAllAndFlush(existingModules);
    }

    private Course getDraftCourseForModification(Long courseId) {
        Course course = courseAuthorizationService.getCourseForEdit(courseId);
        if (course.getStatus() != ResourceCreationStatus.DRAFT) {
            throw new CourseNotInDraftException();
        }
        return course;
    }
}