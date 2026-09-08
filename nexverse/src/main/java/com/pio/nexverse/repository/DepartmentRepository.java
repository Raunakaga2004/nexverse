package com.pio.nexverse.repository;

import com.pio.nexverse.dto.DepartmentDistributionDTO;
import com.pio.nexverse.entities.organization.Department;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    boolean existsByNameAndOrganizationId(String name, Long id);

    Optional<Department> findByIdAndOrganizationId(Long departmentId, Long organizationId);

    Optional<Department> findByNameAndOrganizationId(String departmentName, Long organizationId);

    boolean existsByNameAndOrganizationIdAndIdNot(String name, Long organizationId, Long id);

    long countByOrganizationId(Long organizationId);

    @Query("""
            SELECT new com.pio.nexverse.dto.DepartmentDistributionDTO(
                d.id,
                d.name,
                COUNT(u)
            )
            FROM Department d
            LEFT JOIN d.users u
            WHERE d.organization.id = :organizationId
            GROUP BY d.id, d.name
            ORDER BY COUNT(u) DESC
            """)
    List<DepartmentDistributionDTO> getDepartmentDistribution(@Param("organizationId") Long organizationId, Pageable pageable);
}