import { Level } from "../../components/course/pages/course-management/course-management.component";
import { CourseSkill } from "./course.model";

export interface BrowseCoursesRequest {
  search: string;
  visibility: 'DEPARTMENT' | 'ORGANIZATION' | null;
  accessType: 'OPEN' | 'REQUEST_REQUIRED' | null;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | null;
}

export interface BrowseCourse {
  id: number;
  title: string;
  shortDescription: string;
  level: Level;
  accessType: 'OPEN' | 'REQUEST_REQUIRED';
  estimatedDurationSeconds: number;
  department: string;
  courseSkills: CourseSkill[];
}