package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface CourseService {

    void createCourse(CreateCourseRequestDTO request, MultipartFile thumbnail);

    void updateCourse(Long courseId, UpdateCourseRequestDTO request, MultipartFile thumbnail);

    Page<CoursesResponseDTO> getCourses(CourseSearchRequestDTO request, Pageable pageable);

    CourseResponseDTO getCourse(Long courseId);

    Resource getCourseThumbnail(Long courseId);

    void publishCourse(Long courseId);

    void archiveCourse(Long courseId);

    void restoreCourse(Long courseId);

    void deleteDraftCourse(Long courseId);
}