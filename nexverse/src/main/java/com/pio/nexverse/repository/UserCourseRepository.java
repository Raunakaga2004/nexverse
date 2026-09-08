package com.pio.nexverse.repository;

import com.pio.nexverse.dto.EmployeeLearningStatistics;
import com.pio.nexverse.dto.GrowthPointDTO;
import com.pio.nexverse.entities.user.UserCourse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long>, JpaSpecificationExecutor<UserCourse> {
    @Query("""
                SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                    YEAR(uc.completedAt),
                    MONTH(uc.completedAt),
                    COUNT(uc.id)
                )
                FROM UserCourse uc
                WHERE uc.department.id = :departmentId
                  AND uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.COMPLETED
                  AND uc.completedAt >= :startDate
                GROUP BY YEAR(uc.completedAt), MONTH(uc.completedAt)
                ORDER BY YEAR(uc.completedAt), MONTH(uc.completedAt)
            """)
    List<GrowthPointDTO> getCompletedCourses(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("""
                SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                    YEAR(uc.createdAt),
                    MONTH(uc.createdAt),
                    COUNT(uc.id)
                )
                FROM UserCourse uc
                WHERE uc.department.id = :departmentId
                  AND uc.enrollmentType = com.pio.nexverse.enums.EnrollmentType.DEPARTMENT_ASSIGNED
                  AND uc.createdAt >= :startDate
                GROUP BY YEAR(uc.createdAt), MONTH(uc.createdAt)
                ORDER BY YEAR(uc.createdAt), MONTH(uc.createdAt)
            """)
    List<GrowthPointDTO> getAssignedCourses(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("""
                SELECT COUNT(uc.id)
                FROM UserCourse uc
                WHERE uc.user.id = :employeeId
                  AND uc.enrollmentType = com.pio.nexverse.enums.EnrollmentType.DEPARTMENT_ASSIGNED
                  AND uc.accessRequestStatus = com.pio.nexverse.enums.CourseAccessRequestStatus.APPROVED
            """)
    Long countAssignedCourses(@Param("employeeId") Long employeeId);

    @Query("""
                SELECT COUNT(uc.id)
                FROM UserCourse uc
                WHERE uc.user.id = :employeeId
                  AND uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.IN_PROGRESS
            """)
    Long countInProgressCourses(@Param("employeeId") Long employeeId);

    @Query("""
                SELECT COUNT(uc.id)
                FROM UserCourse uc
                WHERE uc.user.id = :employeeId
                  AND uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.COMPLETED
            """)
    Long countCompletedCourses(@Param("employeeId") Long employeeId);

    @Query("""
                SELECT COALESCE(SUM(mc.estimatedDurationSeconds), 0)
                FROM UserModuleContent umc
                JOIN umc.moduleContent mc
                JOIN umc.userCourseModule ucm
                JOIN ucm.userCourse uc
                WHERE uc.user.id = :employeeId
                  AND umc.progress = com.pio.nexverse.enums.CompletionStatus.COMPLETED
            """)
    Long getLearningDurationInSeconds(@Param("employeeId") Long employeeId);

    @Query("""
                SELECT uc
                FROM UserCourse uc
                WHERE uc.user.id = :employeeId
                  AND uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.IN_PROGRESS
                ORDER BY uc.lastAccessedAt DESC
            """)
    List<UserCourse> findRecentInProgressCourses(@Param("employeeId") Long employeeId, Pageable pageable);

    Optional<UserCourse> findByUserIdAndCourseId(Long id, Long courseId);

    boolean existsByUserIdAndCourseId(Long id, Long courseId);

    @Query("""
                SELECT
                    COUNT(uc) AS totalCourses,
                    COALESCE(SUM(
                        CASE
                            WHEN uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.COMPLETED THEN 1
                            ELSE 0
                        END
                    ), 0) AS completedCourses,
                    COALESCE(SUM(
                        CASE
                            WHEN uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.IN_PROGRESS THEN 1
                            ELSE 0
                        END
                    ), 0) AS inProgressCourses,
                    COALESCE(SUM(
                        CASE
                            WHEN uc.courseProgressStatus = com.pio.nexverse.enums.CourseProgressStatus.NOT_STARTED THEN 1
                            ELSE 0
                        END
                    ), 0) AS notStartedCourses,
                    COALESCE(AVG(uc.progressPercent), 0.0) AS overallProgressPercent
                FROM UserCourse uc
                WHERE uc.user.id = :employeeId
            """)
    EmployeeLearningStatistics getEmployeeLearningStatistics(
            @Param("employeeId") Long employeeId
    );
}