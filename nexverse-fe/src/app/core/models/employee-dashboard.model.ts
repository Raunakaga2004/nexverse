export interface EmployeeDashboardModel {
    metrics: EmployeeDashboardMetrics,
    recentInProgressCourses: EmployeeDashboardRecentCourses[];
}

export interface EmployeeDashboardMetrics {
    assignedCourses: number,
    inProgressCourses: number,
    completedCourses: number,
    learningHours : number
}

export interface EmployeeDashboardRecentCourses {
    id : number,
    title : string,
    shortDescription : string,
    progressPercent : number,
    lastAccessedAt : string
}