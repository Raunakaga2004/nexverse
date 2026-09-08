package com.pio.nexverse.repository;

import com.pio.nexverse.entities.course.ModuleContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleContentRepository extends JpaRepository<ModuleContent, Long> {
    @Query("""
            SELECT COALESCE(MAX(mc.sequenceOrder),0)
            FROM ModuleContent mc
            WHERE mc.courseModule.id = :moduleId
            """)
    int findMaxSequenceOrder(Long moduleId);

    @Query("""
            SELECT COALESCE(SUM(mc.estimatedDurationSeconds),0)
            FROM ModuleContent mc
            WHERE mc.courseModule.id = :moduleId
            """)
    int sumEstimatedDurationByModuleId(Long moduleId);

    List<ModuleContent> findByCourseModuleIdAndSequenceOrderGreaterThanOrderBySequenceOrderAsc(Long moduleId, Integer deletedSequenceOrder);

    List<ModuleContent> findByCourseModuleIdOrderBySequenceOrderAsc(Long moduleId);

    @Query("""
            SELECT COUNT(content)
            FROM ModuleContent content
            WHERE content.courseModule.course.id = :courseId
              AND content.isMandatory = true
            """)
    int getTotalMandatoryContents(@Param("courseId") Long courseId);
}
