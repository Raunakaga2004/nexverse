export interface CourseRequest {
  search?: string;
  requestStatus?: CourseAccessRequestStatus;
}
 
export interface EmployeeCourseRequest {
  id: number;
  requestNote: string | null;
  employeeName: string;
  employeeId: number;
  courseName: string;
  courseId: number;
  accessRequestStatus: CourseAccessRequestStatus;
  department: string;
}
 
export interface DepartmentCourseRequest {
  id: number;
  requestNote: string | null;
  requestStatus: CourseAccessRequestStatus;
  courseId: number;
  courseName : string;
  requestedBy: string;
  requestingDepartment: string;
}

export enum CourseAccessRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}