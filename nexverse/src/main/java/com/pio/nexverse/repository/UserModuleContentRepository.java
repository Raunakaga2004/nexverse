package com.pio.nexverse.repository;

import com.pio.nexverse.entities.user.UserModuleContent;
import com.pio.nexverse.enums.CompletionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserModuleContentRepository extends JpaRepository<UserModuleContent, Long> {
    Optional<UserModuleContent> findByUserCourseModuleIdAndModuleContentId(Long moduleId, Long contentId);

    @Query("""
            SELECT COUNT(umc)
            FROM UserModuleContent umc
            WHERE umc.userCourseModule.userCourse.id = :userCourseId
              AND umc.moduleContent.isMandatory = true
              AND umc.progress = com.pio.nexverse.enums.CompletionStatus.COMPLETED
            """)
    int countCompletedMandatoryContents(@Param("userCourseId") Long userCourseId);

    @Query("""
            SELECT COUNT(umc)
            FROM UserModuleContent umc
            WHERE umc.userCourseModule.id = :userCourseModuleId
              AND umc.moduleContent.isMandatory = true
              AND umc.progress = :progress
            """)
    long countByUserCourseModuleIdAndProgress(@Param("userCourseModuleId") Long userCourseModuleId, @Param("progress") CompletionStatus progress);

    @Query("""
                SELECT umc
                FROM UserModuleContent umc
                JOIN umc.userCourseModule ucm
                JOIN ucm.userCourse uc
                WHERE uc.id = :userCourseId
                  AND umc.moduleContent.id = :moduleContentId
            """)
    Optional<UserModuleContent> findByUserCourseIdAndModuleContentId(
            @Param("userCourseId") Long userCourseId,
            @Param("moduleContentId") Long moduleContentId
    );
}
