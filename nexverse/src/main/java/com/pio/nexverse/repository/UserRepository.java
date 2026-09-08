package com.pio.nexverse.repository;

import com.pio.nexverse.dto.GrowthPointDTO;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmployeeCodeAndOrganizationIdAndIsEnabledTrue(String employeeCode, Long organizationId);

    boolean existsByEmailIgnoreCaseAndIsEnabledTrue(String email);

    boolean existsByPhoneNumberAndIsEnabledTrue(String phoneNumber);

    Optional<User> findByEmailIgnoreCaseAndIsEnabledTrue(String email);

    boolean existsByRoleAndIsEnabledTrue(Role role);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmployeeCodeAndOrganizationId(String employeeCode, Long organizationId);

    Optional<User> findByIdAndOrganizationId(Long employeeId, Long organizationId);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhoneNumberAndIsEnabledTrueAndIdNot(String phoneNumber, Long id);

    boolean existsByEmployeeCodeAndOrganizationIdAndIsEnabledTrueAndIdNot(String employeeCode, Long id, Long id1);

    @Query("""
            SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                YEAR(u.createdAt),
                MONTH(u.createdAt),
                COUNT(u)
            )
            FROM User u
            WHERE u.createdAt >= :startDate
            AND u.role != com.pio.nexverse.enums.Role.SUPER_ADMIN
            GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
            ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
            """)
    List<GrowthPointDTO> getUserGrowth(LocalDateTime startDate);

    Long countByRoleNot(Role role);

    boolean existsByEmail(String email);

    Optional<User> findByDepartmentIdAndRole(Long departmentId, Role role);

    @Query("""
            SELECT new com.pio.nexverse.dto.GrowthPointDTO(
                YEAR(u.createdAt),
                MONTH(u.createdAt),
                COUNT(u)
            )
            FROM User u
            WHERE u.organization.id = :organizationId AND u.createdAt >= :startDate
            AND u.role != com.pio.nexverse.enums.Role.ADMIN
            GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
            ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
            """)
    List<GrowthPointDTO> getEmployeeGrowth(Long organizationId, LocalDateTime startDate);

    long countByOrganizationIdAndRoleNot(Long organizationId, Role role);

    Long countByDepartmentId(Long departmentId);
}