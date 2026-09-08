package com.pio.nexverse.specification;

import com.pio.nexverse.entities.course.Skill;
import org.springframework.data.jpa.domain.Specification;

public final class SkillSpecification {
    private SkillSpecification() {
    }

    public static Specification<Skill> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String search = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("name")),
                search
        );
    }

    public static Specification<Skill> belongsToOrganization(Long organizationId) {
        if (organizationId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("organization").get("id"), organizationId));
    }

    public static Specification<Skill> isEnabled(boolean isEnabled) {
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }
}
