package com.pio.nexverse.service;

import com.pio.nexverse.dto.CourseModuleResponseDTO;
import com.pio.nexverse.dto.CreateCourseModuleRequestDTO;
import com.pio.nexverse.dto.ReorderModuleRequestDTO;
import com.pio.nexverse.dto.UpdateCourseModuleRequestDTO;

import java.util.List;

public interface CourseModuleService {
    void createModule(Long courseId, CreateCourseModuleRequestDTO request);

    List<CourseModuleResponseDTO> getModules(Long courseId);

    CourseModuleResponseDTO getModule(Long moduleId);

    void updateModule(Long moduleId, UpdateCourseModuleRequestDTO request);

    void deleteModule(Long moduleId);

    void reorderModule(Long courseId, ReorderModuleRequestDTO request);
}