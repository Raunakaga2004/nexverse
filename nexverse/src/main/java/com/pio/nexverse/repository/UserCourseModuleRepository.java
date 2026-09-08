package com.pio.nexverse.repository;

import com.pio.nexverse.entities.user.UserCourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCourseModuleRepository extends JpaRepository<UserCourseModule, Long> {
    Optional<UserCourseModule> findByUserCourseIdAndCourseModuleId(Long courseId, Long moduleId);

    @Query("""
            SELECT COUNT(ucm)
            FROM UserCourseModule ucm
            WHERE ucm.userCourse.id = :userCourseId
              AND ucm.progressPercent = 100
            """)
    int countCompletedModules(@Param("userCourseId") Long userCourseId);
}
