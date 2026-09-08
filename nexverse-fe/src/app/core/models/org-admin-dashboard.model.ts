export interface OrgAdminDashboardModel {
    metrics: OrgAdminDashboardMetrics,
    growth: OrgAdminDashboardGrowth,
    departmentDistribution: DepartmentDistribution[],
    storage : StorageUsageDTO;
}

export interface OrgAdminDashboardMetrics {
    totalEmployees: number,
    totalDepartments: number,
    totalSkills: number,
    totalCourses : number
}

export interface OrgAdminDashboardGrowth {
    years : number[],
    months : number[],
    employeeCounts : number[],
    courseCounts : number[]
}

export interface DepartmentDistribution {
    departmentId: number,
    departmentName: string,
    employeeCount: number,
}

export interface StorageUsageDTO {
    usedStorage: number,
    totalStorage: number,
}