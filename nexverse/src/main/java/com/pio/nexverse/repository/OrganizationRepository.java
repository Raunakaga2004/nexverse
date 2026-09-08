package com.pio.nexverse.repository;

import com.pio.nexverse.dto.GrowthPointDTO;
import com.pio.nexverse.entities.organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    boolean existsByEmailIgnoreCaseAndIsEnabledTrue(String email);

    boolean existsByPhoneAndIsEnabledTrue(String phone);

    boolean existsByEmailIgnoreCaseAndIsEnabledTrueAndIdNot(String email, Long id);

    @Query("""
            SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                YEAR(o.createdAt),
                MONTH(o.createdAt),
                COUNT(o)
            )
            FROM Organization o
            WHERE o.createdAt >= :startDate
            GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)
            ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)
            """)
    List<GrowthPointDTO> getOrganizationGrowth(@Param("startDate") LocalDateTime startDate);

    List<Organization> findTop5ByOrderByCreatedAtDesc();
}