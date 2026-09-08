package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.enums.CourseAccessRequestStatus;
import com.pio.nexverse.enums.GrowthPeriod;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.exception.DepartmentNotFoundException;
import com.pio.nexverse.exception.UserNotFoundException;
import com.pio.nexverse.repository.*;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.DashboardService;
import com.pio.nexverse.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SkillRepository skillRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    @Override
    public SuperAdminDashboardDTO getSuperAdminDashboard(GrowthPeriod period) {
        SuperAdminDashboardMetricsDTO metrics = getSuperAdminDashboardMetrics();
        SuperAdminDashboardGrowthDTO growth = getSuperAdminDashboardGrowth(period);
        List<RecentOrganizationDTO> recentOrganizations = getRecentOrganizations();
        return SuperAdminDashboardDTO.builder()
                .metrics(metrics)
                .growth(growth)
                .recentOrganizations(recentOrganizations)
                .build();
    }

    @Override
    public OrgAdminDashboardDTO getOrgAdminDashboard(GrowthPeriod period) {
        Long organizationId = currentUserService.getCurrentOrganization().getId();
        return OrgAdminDashboardDTO.builder()
                .metrics(buildOrgAdminDashboardMetrics(organizationId))
                .growth(buildOrgAdminDashboardGrowth(organizationId, period))
                .departmentDistribution(buildDepartmentDistribution(organizationId))
                .storage(buildStorageUsage(organizationId))
                .build();
    }

    @Override
    public DepManagerDashboardDTO getManagerDashboard(GrowthPeriod period) {
        Long departmentId = currentUserService.getCurrentDepartment().getId();
        return DepManagerDashboardDTO.builder()
                .metrics(buildManagerDashboardMetrics(departmentId))
                .growth(buildManagerDashboardGrowth(departmentId, period))
                .build();
    }

    @Override
    public EmployeeDashboardDTO getEmployeeDashboard() {
        Long employeeId = currentUserService.getCurrentUser().getId();
        return EmployeeDashboardDTO.builder()
                .metrics(buildEmployeeDashboardMetrics(employeeId))
                .recentInProgressCourses(buildRecentInProgressCourses(employeeId))
                .build();
    }

    private List<EmployeeDashboardRecentCoursesDTO> buildRecentInProgressCourses(Long employeeId) {
        List<UserCourse> userCourses = userCourseRepository.findRecentInProgressCourses(employeeId, PageRequest.of(0, 3));
        return userCourses.stream()
                .map(this::mapToEmployeeDashboardRecentCourse)
                .toList();
    }

    private EmployeeDashboardRecentCoursesDTO mapToEmployeeDashboardRecentCourse(UserCourse userCourse) {
        Course course = userCourse.getCourse();
        return EmployeeDashboardRecentCoursesDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .lastAccessedAt(userCourse.getLastAccessedAt())
                .progressPercent(userCourse.getProgressPercent())
                .build();
    }

    private EmployeeDashboardMetricsDTO buildEmployeeDashboardMetrics(Long employeeId) {
        Long assignedCourses = userCourseRepository.countAssignedCourses(employeeId);
        Long inProgressCourses = userCourseRepository.countInProgressCourses(employeeId);
        Long completedCourses = userCourseRepository.countCompletedCourses(employeeId);
        Long learningHours = userCourseRepository.getLearningDurationInSeconds(employeeId);
        return EmployeeDashboardMetricsDTO.builder()
                .assignedCourses(assignedCourses)
                .inProgressCourses(inProgressCourses)
                .completedCourses(completedCourses)
                .learningHours(learningHours)
                .build();
    }

    private DepManagerDashboardGrowthDTO buildManagerDashboardGrowth(Long departmentId, GrowthPeriod period) {
        LocalDateTime startDate = getStartDate(period);
        List<GrowthPointDTO> completeCoursesGrowth = userCourseRepository.getCompletedCourses(departmentId, startDate);
        List<GrowthPointDTO> assignedCoursesGrowth = userCourseRepository.getAssignedCourses(departmentId, startDate);
        List<YearMonth> timeline = buildTimeline(period);
        Map<YearMonth, Long> completeCoursesMap = toMap(completeCoursesGrowth);
        Map<YearMonth, Long> assignedCoursesMap = toMap(assignedCoursesGrowth);
        return buildManagerDashboardGrowthDTO(timeline, completeCoursesMap, assignedCoursesMap);
    }

    private DepManagerDashboardMetricsDTO buildManagerDashboardMetrics(Long departmentId) {
        Department department = departmentRepository.findById(departmentId).orElseThrow(DepartmentNotFoundException::new);
        long totalCourseAccess = department.getCourseAccesses().stream()
                .filter((departmentCourseAccess -> departmentCourseAccess.getRequestStatus() == CourseAccessRequestStatus.APPROVED))
                .toList().size();
        long departmentCourseAccessPending = department.getDepartmentCourseAccessRequest().stream()
                .filter((departmentCourseAccess -> departmentCourseAccess.getRequestStatus() == CourseAccessRequestStatus.PENDING))
                .toList().size();
        long employeeCourseAccessPending = department.getUsers().stream()
                .flatMap(user -> user.getUserCourses().stream())
                .filter(userCourse ->
                        userCourse.getAccessRequestStatus() == CourseAccessRequestStatus.PENDING)
                .count();
        return DepManagerDashboardMetricsDTO.builder()
                .totalEmployees(department.getUsers().size() - 1)
                .totalCourses(department.getCourses().size())
                .totalCourseAccess(totalCourseAccess)
                .pendingApprovals(departmentCourseAccessPending + employeeCourseAccessPending)
                .build();
    }

    private DepManagerDashboardGrowthDTO buildManagerDashboardGrowthDTO(List<YearMonth> timeline, Map<YearMonth, Long> completeCoursesMap, Map<YearMonth, Long> assignedCoursesMap) {
        List<Integer> years = new ArrayList<>(timeline.size());
        List<Integer> months = new ArrayList<>(timeline.size());
        List<Long> completeCoursesCount = new ArrayList<>(timeline.size());
        List<Long> assignedCoursesCount = new ArrayList<>(timeline.size());
        for (YearMonth yearMonth : timeline) {
            years.add(yearMonth.getYear());
            months.add(yearMonth.getMonthValue());
            completeCoursesCount.add(completeCoursesMap.getOrDefault(yearMonth, 0L));
            assignedCoursesCount.add(assignedCoursesMap.getOrDefault(yearMonth, 0L));
        }
        return DepManagerDashboardGrowthDTO.builder()
                .years(years)
                .months(months)
                .completedCourses(completeCoursesCount)
                .assignedCourses(assignedCoursesCount)
                .build();
    }

    private OrgAdminDashboardMetricsDTO buildOrgAdminDashboardMetrics(Long organizationId) {
        return OrgAdminDashboardMetricsDTO.builder()
                .totalEmployees(userRepository.countByOrganizationIdAndRoleNot(organizationId, Role.ADMIN))
                .totalDepartments(departmentRepository.countByOrganizationId(organizationId))
                .totalSkills(skillRepository.countByOrganizationId(organizationId))
                .totalCourses(courseRepository.countByOwningDepartment_OrganizationId(organizationId))
                .build();
    }

    private OrgAdminDashboardGrowthDTO buildOrgAdminDashboardGrowth(Long organizationId, GrowthPeriod period) {
        LocalDateTime startDate = getStartDate(period);
        List<GrowthPointDTO> employeeGrowth = userRepository.getEmployeeGrowth(organizationId, startDate);
        List<GrowthPointDTO> courseGrowth = courseRepository.getCourseGrowth(organizationId, startDate);
        List<YearMonth> timeline = buildTimeline(period);
        Map<YearMonth, Long> employeeMap = toMap(employeeGrowth);
        Map<YearMonth, Long> courseMap = toMap(courseGrowth);
        return buildOrgAdminDashboardGrowthDTO(timeline, employeeMap, courseMap);
    }

    private OrgAdminDashboardGrowthDTO buildOrgAdminDashboardGrowthDTO(List<YearMonth> timeline, Map<YearMonth, Long> employeeMap, Map<YearMonth, Long> courseMap) {
        List<Integer> years = new ArrayList<>(timeline.size());
        List<Integer> months = new ArrayList<>(timeline.size());
        List<Long> employeeCounts = new ArrayList<>(timeline.size());
        List<Long> courseCounts = new ArrayList<>(timeline.size());
        for (YearMonth yearMonth : timeline) {
            years.add(yearMonth.getYear());
            months.add(yearMonth.getMonthValue());
            employeeCounts.add(employeeMap.getOrDefault(yearMonth, 0L));
            courseCounts.add(courseMap.getOrDefault(yearMonth, 0L));
        }
        return OrgAdminDashboardGrowthDTO.builder()
                .years(years)
                .months(months)
                .employeeCounts(employeeCounts)
                .courseCounts(courseCounts)
                .build();
    }

    private List<DepartmentDistributionDTO> buildDepartmentDistribution(Long organizationId) {
        return departmentRepository.getDepartmentDistribution(organizationId, PageRequest.of(0, 5));
    }

    private StorageUsageDTO buildStorageUsage(Long organizationId) {
        long totalOrgFileStorageBytes = fileStorageService.getTotalOrgFileStorageBytes(organizationId);
        return StorageUsageDTO.builder()
                .usedStorage(totalOrgFileStorageBytes)
                .totalStorage(10L * 1024 * 1024 * 1024) // 10 GB limit hardcoded right now
                .build();
    }

    private List<RecentOrganizationDTO> getRecentOrganizations() {
        return organizationRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapRecentOrganization)
                .toList();
    }

    private RecentOrganizationDTO mapRecentOrganization(Organization organization) {
        User orgAdmin = organization.getUsers().stream()
                .filter((user) -> user.getRole() == Role.ADMIN)
                .findFirst().orElseThrow(UserNotFoundException::new);
        return RecentOrganizationDTO.builder()
                .id(organization.getId())
                .organizationName(organization.getName())
                .status(organization.getStatus())
                .createdAt(organization.getCreatedAt())
                .organizationAdminName(orgAdmin.getFirstName() + " " + orgAdmin.getLastName())
                .build();
    }

    private SuperAdminDashboardGrowthDTO getSuperAdminDashboardGrowth(GrowthPeriod period) {
        LocalDateTime startDate = getStartDate(period);
        List<GrowthPointDTO> organizationGrowth = organizationRepository.getOrganizationGrowth(startDate);
        List<GrowthPointDTO> userGrowth = userRepository.getUserGrowth(startDate);
        List<YearMonth> timeline = buildTimeline(period);
        Map<YearMonth, Long> organizationMap = toMap(organizationGrowth);
        Map<YearMonth, Long> userMap = toMap(userGrowth);
        return buildSuperAdminDashboardGrowthDTO(
                timeline,
                organizationMap,
                userMap
        );
    }

    private SuperAdminDashboardGrowthDTO buildSuperAdminDashboardGrowthDTO(List<YearMonth> timeline, Map<YearMonth, Long> organizationMap, Map<YearMonth, Long> userMap) {
        List<Integer> years = new ArrayList<>(timeline.size());
        List<Integer> months = new ArrayList<>(timeline.size());
        List<Long> organizationCounts = new ArrayList<>(timeline.size());
        List<Long> userCounts = new ArrayList<>(timeline.size());
        for (YearMonth yearMonth : timeline) {
            years.add(yearMonth.getYear());
            months.add(yearMonth.getMonthValue());
            organizationCounts.add(organizationMap.getOrDefault(yearMonth, 0L));
            userCounts.add(userMap.getOrDefault(yearMonth, 0L));
        }
        return SuperAdminDashboardGrowthDTO.builder()
                .years(years)
                .months(months)
                .organizationCounts(organizationCounts)
                .userCounts(userCounts)
                .build();
    }

    private Map<YearMonth, Long> toMap(List<GrowthPointDTO> growthPoints) {
        return growthPoints.stream()
                .collect(Collectors.toMap(point -> YearMonth.of(point.getYear(), point.getMonth()), GrowthPointDTO::getCount));
    }

    private List<YearMonth> buildTimeline(GrowthPeriod period) {
        List<YearMonth> timeline = new ArrayList<>(period.getMonths());
        YearMonth current = YearMonth.now().minusMonths(period.getMonths() - 1);
        for (int i = 0; i < period.getMonths(); i++) {
            timeline.add(current);
            current = current.plusMonths(1);
        }
        return timeline;
    }

    private LocalDateTime getStartDate(GrowthPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case LAST_12_MONTHS -> now.minusMonths(12);
            case LAST_3_YEARS -> now.minusYears(3);
            case LAST_5_YEARS -> now.minusYears(5);
        };
    }

    private SuperAdminDashboardMetricsDTO getSuperAdminDashboardMetrics() {
        return SuperAdminDashboardMetricsDTO.builder()
                .totalOrganizations(organizationRepository.count())
                .totalUsers(userRepository.countByRoleNot(Role.SUPER_ADMIN))
                .totalStorageBytes(fileStorageService.getTotalFileStorageBytes())
                .build();
    }
}
