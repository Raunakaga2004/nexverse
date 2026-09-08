export interface SuperAdminDashboardModel {
    metrics: SuperAdminDashboardMetrics,
    growth: DashboardGrowth,
    recentOrganizations: RecentOrganizations[]
}

export interface SuperAdminDashboardMetrics {
    totalOrganizations: number,
    totalUsers: number,
    totalStorageBytes: number
}

export interface DashboardGrowth {
    years : number[],
    months : number[],
    organizationCounts : number[],
    userCounts : number[]
}

export interface RecentOrganizations {
    id: number,
    organizationName: string,
    organizationAdminName: string,
    createdAt: string,
    status: string
}