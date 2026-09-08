package com.pio.nexverse.specification;

import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.course.CourseSkill;
import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import com.pio.nexverse.enums.ResourceCreationStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecification {
    private CourseSpecification() {
    }

    public static Specification<Course> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), search);
    }

    public static Specification<Course> hasStatus(ResourceCreationStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Course> hasLevel(Level level) {
        if (level == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("level"), level);
    }

    public static Specification<Course> hasVisibility(CourseVisibility visibility) {
        if (visibility == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("visibility"), visibility);
    }

    public static Specification<Course> hasAccessType(CourseAccessType accessType) {
        if (accessType == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("accessType"), accessType);
    }

    public static Specification<Course> belongsToDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("owningDepartment").get("id"), departmentId));
    }

    public static Specification<Course> belongsToOrganization(Long organizationId) {
        if (organizationId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("owningDepartment").get("organization").get("id"), organizationId));
    }

    public static Specification<Course> isEnabled(boolean isEnabled) {
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }

    public static Specification<Course> hasSkill(Long skillId) {
        if (skillId == null) return null;
        return ((root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Course, CourseSkill> courseSkill = root.join("courseSkills");
            return cb.equal(courseSkill.get("skill").get("id"), skillId);
        });
    }

    public static Specification<Course> isVisibleToEmployee(Long departmentId) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("visibility"), CourseVisibility.ORGANIZATION),
                cb.and(
                        cb.equal(root.get("visibility"), CourseVisibility.DEPARTMENT),
                        cb.equal(root.get("owningDepartment").get("id"), departmentId)
                )
        );
    }
}