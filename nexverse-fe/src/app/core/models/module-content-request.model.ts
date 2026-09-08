import { ContentType } from "./course.model";

export interface CreateModuleContentRequest {
  title: string;
  description: string;
  contentType: ContentType;
  estimatedDurationSeconds: number;
  isMandatory: boolean;
  textBody?: string;
  contentFile?: File;
}
 
export interface UpdateModuleContentRequest
  extends CreateModuleContentRequest {}