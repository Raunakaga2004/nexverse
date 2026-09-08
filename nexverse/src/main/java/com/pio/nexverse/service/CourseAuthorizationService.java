package com.pio.nexverse.service;

import com.pio.nexverse.entities.course.Course;

public interface CourseAuthorizationService {
    Course getCourseForView(Long courseId);

    Course getCourseForEdit(Long courseId);
}