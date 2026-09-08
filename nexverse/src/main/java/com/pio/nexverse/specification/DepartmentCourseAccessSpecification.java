package com.pio.nexverse.specification;

import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.enums.CourseAccessRequestStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class DepartmentCourseAccessSpecification {

    private DepartmentCourseAccessSpecification() {
    }

    public static Specification<DepartmentCourseAccess> hasDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("requestedToDepartment").get("id"),
                        departmentId
                );
    }

    public static Specification<DepartmentCourseAccess> hasSearch(
            String search
    ) {
        return (root, query, criteriaBuilder) -> {

            if (!StringUtils.hasText(search)) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.get("course").get("title")
                    ),
                    "%" + search.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<DepartmentCourseAccess> hasRequestStatus(
            CourseAccessRequestStatus status
    ) {
        return (root, query, criteriaBuilder) -> {

            if (status == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("requestStatus"),
                    status
            );
        };
    }
}
