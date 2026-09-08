export interface EmployeeSkillRequest {
    skillName?: string;
    skillLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
}
 
export interface EmployeeSkill {
    skillId: number;
    skillName: string;
    levels: EmployeeSkillLevel[];
}
 
export interface EmployeeSkillLevel {
    level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    courseCount: number;
}