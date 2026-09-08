package com.pio.nexverse.specification;

import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.enums.OrganizationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class OrganizationSpecification {
    private OrganizationSpecification() {
    }

    public static Specification<Organization> hasStatus(OrganizationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Organization> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(
                        cb.lower(root.get("name")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("email")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("phone")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("city")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("state")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("country")),
                        search
                ),
                cb.like(
                        cb.lower(root.get("zipCode")),
                        search
                )
        );
    }

    public static Specification<Organization> isEnabled(Boolean isEnabled) {
        if(isEnabled == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }
}