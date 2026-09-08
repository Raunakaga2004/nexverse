package com.pio.nexverse.repository;

import com.pio.nexverse.entities.course.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long>, JpaSpecificationExecutor<Skill> {

    boolean existsByNameAndOrganizationId(String name, Long organizationId);

    Optional<Skill> findByIdAndOrganizationId(Long skillId, Long organizationId);

    boolean existsByNameAndOrganizationIdAndIdNot(String name, Long organizationId, Long id);

    long countByOrganizationId(Long organizationId);
}
