package com.pio.nexverse.specification;

import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.course.CourseSkill;
import com.pio.nexverse.entities.course.Skill;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.enums.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserCourseSpecification {
    private UserCourseSpecification() {}

    public static Specification<UserCourse> belongsToEmployee(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), employeeId);
    }

    public static Specification<UserCourse> belongsToCourseOwningDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("course").get("owningDepartment").get("id"), departmentId);
    }

    public static Specification<UserCourse> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("course").get("title")), search);
    }

    public static Specification<UserCourse> hasEnrollmentType(EnrollmentType type) {
        if (type == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("enrollmentType"), type);
    }

    public static Specification<UserCourse> hasProgressStatus(CourseProgressStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("courseProgressStatus"), status);
    }

    public static Specification<UserCourse> hasRequestStatus(CourseAccessRequestStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) ->
                cb.equal(root.get("accessRequestStatus"), status);
    }

    public static Specification<UserCourse> hasSkills(String keyword, Level level) {
        if ((keyword == null || keyword.isBlank()) && level == null) {
            return null;
        }
        return (root, query, cb) -> {
            if(query != null) query.distinct(true);
            Join<UserCourse, Course> course = root.join("course");
            Join<Course, CourseSkill> courseSkill = course.join("courseSkills");
            Join<CourseSkill, Skill> skill = courseSkill.join("skill");
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(skill.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (level != null) {
                predicates.add(cb.equal(courseSkill.get("skillLevel"), level));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserCourse> isCompleted() {
        return (root, query, cb) ->
                cb.equal(root.get("courseProgressStatus"), CourseProgressStatus.COMPLETED);
    }
}