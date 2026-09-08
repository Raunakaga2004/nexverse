package com.pio.nexverse.repository;

import com.pio.nexverse.entities.course.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findAllByCourseId(Long courseId);

    List<CourseModule> findAllByCourseIdOrderBySequenceOrderAsc(Long courseId);

    @Query("""
            SELECT COALESCE(SUM(cm.estimatedDurationSeconds),0)
            FROM CourseModule cm
            WHERE cm.course.id = :courseId
            """)
    int sumEstimatedDurationByCourseId(Long courseId);

    @Query("""
            SELECT COALESCE(MAX(cm.sequenceOrder), 0)
            FROM CourseModule cm
            WHERE cm.course.id = :courseId
            """)
    int findMaxSequenceOrderByCourseId(Long courseId);

    @Query("""
    SELECT COUNT(mc)
    FROM ModuleContent mc
    WHERE mc.courseModule.id = :moduleId
      AND mc.isMandatory = true
    """)
    int countMandatoryContentsByModuleId(@Param("moduleId") Long moduleId);
}