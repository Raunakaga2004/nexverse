package com.pio.nexverse.repository;

import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.enums.CourseAccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepartmentCourseAccessRepository extends JpaRepository<DepartmentCourseAccess, Long>, JpaSpecificationExecutor<DepartmentCourseAccess> {
    boolean existsByCourseIdAndRequestingDepartmentIdAndRequestStatus(Long courseId, Long requestingDepartmentId, CourseAccessRequestStatus courseAccessRequestStatus);

    Optional<DepartmentCourseAccess> findByCourseIdAndRequestingDepartmentId(Long courseId, Long requestingDepartmentId);
}
