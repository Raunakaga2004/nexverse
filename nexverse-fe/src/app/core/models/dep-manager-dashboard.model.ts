export interface DepManagerDashboardModel {
    metrics: DepManagerDashboardMetrics,
    growth: DepManagerDashboardGrowth
}

export interface DepManagerDashboardMetrics {
    totalEmployees: number,
    totalCourses: number,
    totalCourseAccess: number,
    pendingApprovals : number
}

export interface DepManagerDashboardGrowth {
    years : number[],
    months : number[],
    completedCourses : number[],
    assignedCourses : number[]
}