export interface MyLearningRequest {
  search?: string;
  progressStatus?: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | null;
  requestStatus?: 'PENDING' | 'APPROVED' | 'REJECTED' | null;
  enrollmentType?: 'SELF_ENROLLED' | 'DEPARTMENT_ASSIGNED' | 'SELF_REQUESTED' | null;
}
 
export interface MyLearningCourse {
  courseId: number;
  title: string;
  thumbnailUrl: string;
  progressPercent: number;
  progressStatus: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
  requestStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  enrollmentType: 'SELF_ENROLLED' | 'DEPARTMENT_ASSIGNED' | 'SELF_REQUESTED';
  totalModules: number;
  completedModules: number;
  totalContents: number;
  completedContents: number;
}