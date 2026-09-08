import { Skill } from "./skill.model";

export interface Course {
    id : number;
    title : string;
    shortDescription : string;
    description : string;
    owningDepartment : string;
    owningDepartmentId : number;
    level : string;
    visibility : string;
    accessType : string;
    status : string;
    publishedAt : string;
    estimatedDurationSeconds : number;
    isEnabled : boolean;
    createdAt : string;
    updatedAt : string;
    courseSkills : CourseSkill[];
    modules :  CourseModule[];
    hasAccess? :boolean;
    requestStatus : string;
}

export interface CourseSkill {
    id? : number;
    skill : Skill;
    skillLevel : 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
}

export interface CourseModule {
  id: number;
  title: string;
  shortDescription : string;
  description: string;
  estimatedDurationSeconds: number;
  sequenceOrder: number;
  isMandatory: boolean;
  contents : ModuleContent[];
  numberOfContents : number;
  courseStatus : string;
  hasAccess? :boolean;
}

export interface ModuleContent {
  id: number;
  title: string;
  description: string;
  contentType: ContentType;
  sequenceOrder: number;
  isMandatory: boolean;
  isDownloadable: boolean;
  estimatedDurationSeconds: number;
  contentUrl?: string;
  textBody?: string;
  format?: string;
  thumbnailUrl?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: UserSummary;
  updatedBy?: UserSummary;
}

export enum ContentType {
  VIDEO = 'VIDEO',
  DOCUMENT = 'DOCUMENT',
  TEXT = 'TEXT'
}

export interface UserSummary {
  id: number;
  fullName: string;
}