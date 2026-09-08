export interface AssignCourseDialogCourse {
  id: number;
  name: string;
  thumbnailUrl?: string | null;
  description?: string | null;
  estimatedDurationSeconds?: number;
  totalModules?: number;
}
 
export interface AssignCourseDialogEmployee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  department?: string;
  profileImageUrl?: string | null;
}
 
export interface AssignCourseDialogData {
  course: AssignCourseDialogCourse;
  employees: AssignCourseDialogEmployee[];
}

export interface AssignCourseRequest{
    courseId: number;
    employeesId: number[];
}