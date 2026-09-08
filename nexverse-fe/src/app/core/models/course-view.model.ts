import { Skill } from "./skill.model";

export interface CourseViewModel {
  id: number;
  title: string;
  shortDescription: string;
  description: string;
  level: string;
  accessType: string;
  visibility: string;
  estimatedDurationSeconds: number;
  publishedAt: string;
  publisherId: number;
  publisherName: string;
  skills: CourseSkillModel[];
  totalModules: number;
  totalContents: number;
  progressPercent: number;
  modules: CourseModuleViewModel[];
  requestStatus : string;
}
 
export interface CourseSkillModel {
  id: number;
  skill: Skill;
  skillLevel: string;
}
 
export interface CourseModuleViewModel {
  id: number;
  title: string;
  shortDescription: string;
  description: string;
  sequenceOrder: number;
  estimatedDurationSeconds: number;
  totalContents: number;
  contents: ModuleContentViewModel[];
}
 
export interface ModuleContentViewModel {
  id: number
  title: string;
  description: string;
  contentType: string;
  sequenceOrder: number;
  estimatedDurationSeconds: number;
  mandatory: boolean;
  completionStatus: string;
}