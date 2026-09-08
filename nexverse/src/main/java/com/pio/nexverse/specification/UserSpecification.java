package com.pio.nexverse.specification;

import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {
    private UserSpecification() {
    }

    public static Specification<User> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return ((root, query, cb) -> cb.or(
                cb.like(
                        cb.concat(cb.concat(cb.lower(root.get("firstName")), " "), cb.lower(root.get("lastName"))),
                        search
                ),
                cb.like(
                        cb.lower(root.get("email")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("jobTitle")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("employeeCode")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("department").get("name")),
                        search
                )
        ));
    }

    public static Specification<User> belongsToOrganization(Long organizationId) {
        if (organizationId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("organization").get("id"), organizationId));
    }

    public static Specification<User> belongsToDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("department").get("id"), departmentId));
    }

    public static Specification<User> hasRole(Role role) {
        if (role == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("role"), role));
    }

    public static Specification<User> hasStatus(UserStatus status) {
        if (status == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("status"), status));
    }

    public static Specification<User> isEnabled(boolean isEnabled) {
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }

    public static Specification<User> hasDepartment(String departmentName) {
        if (departmentName == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("department").get("name"), departmentName));
    }
}