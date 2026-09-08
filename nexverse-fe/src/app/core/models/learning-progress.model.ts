import { PageResponse } from "./page-response.model";

export interface EmployeeProgressResponse {
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  departmentName: string;
  totalCourses: number;
  completedCourses: number;
  inProgressCourses: number;
  notStartedCourses: number;
  overallProgressPercent: number;
  courses: PageResponse<EmployeeCourseProgress>;
}

export interface EmployeeCourseProgress {
  courseId: number;
  courseTitle: string;
  enrollmentType: string;
  progressPercent: number;
  totalModules: number;
  completedModules: number;
  totalContents: number;
  completedContents: number;
  estimatedDurationSeconds: number;
  startedAt: string | null;
  lastAccessedAt: string | null;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
}