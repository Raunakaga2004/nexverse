package com.pio.nexverse.specification;

import com.pio.nexverse.entities.organization.Department;
import org.springframework.data.jpa.domain.Specification;

public final class DepartmentSpecification {
    private DepartmentSpecification() {
    }

    public static Specification<Department> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("name")),
                search
        );
    }

    public static Specification<Department> belongsToOrganization(Long organizationId) {
        if (organizationId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("organization").get("id"), organizationId));
    }

    public static Specification<Department> isEnabled(boolean isEnabled) {
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }
}