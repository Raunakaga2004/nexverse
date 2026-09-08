package com.pio.nexverse.service;

import com.pio.nexverse.dto.MyLearningCourseDTO;
import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface EmployeeCourseService {
    Page<BrowseCourseResponseDTO> browseCourses(BrowseCoursesRequestDTO request, Pageable pageable);

    CourseViewResponseDTO getCourse(Long courseId);

    LearningCourseResponseDTO getLearningCourse(Long courseId);

    LearningContentResponseDTO getContent(Long courseId, Long contentId);

    ResponseEntity<ResourceRegion> streamVideo(Long courseId, Long contentId, HttpHeaders headers) throws IOException;

    ResponseEntity<Resource> getDocument(Long courseId, Long contentId);

    TextContentResponseDTO getTextContent(Long courseId, Long contentId);

    void startContent(Long courseId, Long contentId);

    void completeContent(Long courseId, Long contentId);

    Page<MyLearningCourseDTO> getMyLearningCourses(MyLearningRequestDTO request, Pageable pageable);

    List<EmployeeSkillResponseDTO> getEmployeeSkills(EmployeeSkillRequestDTO request);
}