export interface LearningCourse {
    id: number;
    title: string;
    shortDescription : string;
    estimatedDurationSeconds : number;
    totalContents : number;
    completedContents : number;
    progressPercent: number;
    modules: LearningModule[];
}

export interface LearningModule {
    id: number;
    title: string;
    sequenceOrder: number;
    totalContents : number;
    completedContents : number;
    progressPercent: number;
    contents: LearningContentSummary[];
}

export interface LearningContentSummary {
    id: number;
    title: string;
    description: string;
    contentType: 'VIDEO' | 'DOCUMENT' | 'TEXT';
    sequenceOrder: number;
    estimatedDurationSeconds : number;
    mandatory: boolean;
    started: boolean;
    completed: boolean;
}

export interface LearningContent {
    id: number;
    title: string;
    description: string;
    contentType: 'VIDEO' | 'DOCUMENT' | 'TEXT';
    estimatedDurationSeconds : number;
    mandatory: boolean;
    progress : 'COMPLETED' | 'NOT_STARTED' | 'IN_PROGRESS';
    previousContentId : number;
    nextContentId : number;
}