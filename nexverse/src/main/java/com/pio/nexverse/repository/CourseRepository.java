package com.pio.nexverse.repository;

import com.pio.nexverse.dto.GrowthPointDTO;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.enums.ResourceCreationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    long countByOwningDepartment_OrganizationId(Long organizationId);

    @Query("""
            SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                YEAR(c.createdAt),
                MONTH(c.createdAt),
                COUNT(c)
            )
            FROM Course c
            WHERE c.owningDepartment.organization.id = :organizationId AND c.createdAt >= :startDate
            GROUP BY YEAR(c.createdAt), MONTH(c.createdAt)
            ORDER BY YEAR(c.createdAt), MONTH(c.createdAt)
            """)
    List<GrowthPointDTO> getCourseGrowth(Long organizationId, LocalDateTime startDate);

    Optional<Course> findByIdAndStatusAndIsEnabledTrue(Long courseId, ResourceCreationStatus resourceCreationStatus);

    @Query("""
            SELECT c
            FROM Course c
            JOIN c.modules m
            JOIN m.moduleContents mc
            WHERE mc.id = :contentId
            AND c.owningDepartment.organization.id = :organizationId
            """)
    Optional<Course> findByContentIdAndOrganizationId(@Param("contentId") Long contentId, @Param("organizationId") Long organizationId);

    @Query("""
            SELECT c
            FROM Course c
            JOIN c.modules m
            WHERE m.id = :moduleId
            """)
    Optional<Course> findByModuleId(@Param("moduleId") Long moduleId);

    Optional<Course> findByIdAndOwningDepartment_OrganizationId(Long courseId, Long organizationId);
}