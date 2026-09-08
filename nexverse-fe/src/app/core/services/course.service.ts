import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpContext, HttpParams } from "@angular/common/http";
import { SKIP_GLOBAL_ERROR_HANDLER } from "../constants/http-context.constants";
import { ApiResponse } from "../models/api-response.model";
import { Observable } from "rxjs";
import { PageResponse } from "../models/page-response.model";
import { Course, CourseModule, ModuleContent } from "../models/course.model";
import { CreateModuleContentRequest, UpdateModuleContentRequest } from "../models/module-content-request.model";
import { BrowseCourse, BrowseCoursesRequest } from "../models/browse-course.model";

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private contentUrl = `${environment.apiUrl}/module-content`;
  private moduleUrl = `${environment.apiUrl}/course-module`;
  private url = `${environment.apiUrl}/course`;
  private baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getCourses(page: number, size: number, sortBy: string, direction: string, search: string, status: string, level: string, visibility: string, accessType: string): Observable<ApiResponse<PageResponse<Course>>> {
    return this.http.get<ApiResponse<PageResponse<Course>>>(`${this.url}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}&search=${search}&status=${status}&level=${level === 'ALL' ? '' : level}&visibility=${visibility === 'ALL' ? '' : visibility}&accessType=${accessType === 'ALL' ? '' : accessType}`, this.skipGlobalErrorHandler)
  }

  getCourse(id: number) {
    return this.http.get<ApiResponse<Course>>(`${this.url}/${id}`);
  }

  editCourse(id: number, formData: FormData): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}`, formData);
  }

  createCourse(formData: FormData): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}`, formData);
  }

  enableCourse(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: true });
  }

  disableCourse(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: false });
  }

  deleteDraftCourse(id: number) {
    return this.http.delete<ApiResponse<void>>(`${this.url}/${id}/delete`);
  }

  createModule(courseId: number, formData: FormData): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/${courseId}/module`, formData);
  }

  editModule(moduleId: number, formData: FormData): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.moduleUrl}/${moduleId}`, formData);
  }

  deleteModule(moduleId: number) {
    return this.http.delete<ApiResponse<void>>(`${this.moduleUrl}/${moduleId}`);
  }

  getModuleById(moduleId: number) {
    return this.http.get<ApiResponse<CourseModule>>(`${this.moduleUrl}/${moduleId}`);
  }

  getContents(moduleId: number) {
    return this.http.get<ApiResponse<ModuleContent>>(`${this.moduleUrl}/${moduleId}/contents`);
  }

  createContent(
    moduleId: number,
    request: CreateModuleContentRequest
  ): Observable<ApiResponse<void>> {

    const formData = this.buildContentFormData(request);

    return this.http.post<ApiResponse<void>>(
      `${environment.apiUrl}/course-module/${moduleId}/content`,
      formData
    );
  }

  updateContent(
    contentId: number,
    request: UpdateModuleContentRequest
  ): Observable<ApiResponse<void>> {

    const formData = this.buildContentFormData(request);

    return this.http.put<ApiResponse<void>>(
      `${environment.apiUrl}/module-content/${contentId}`,
      formData
    );
  }

  deleteContent(contentId: number) {
    return this.http.delete<ApiResponse<void>>(`${this.contentUrl}/${contentId}`);
  }

  getContent(contentId: number) {
    return this.http.get<ApiResponse<ModuleContent>>(`${this.contentUrl}/${contentId}`);
  }

  getDocument(contentId: number): Observable<Blob> {
    return this.http.get(
      `${this.contentUrl}/${contentId}/document`,
      {
        responseType: 'blob'
      }
    );
  }

  private buildContentFormData(
    request: CreateModuleContentRequest | UpdateModuleContentRequest
  ): FormData {

    const formData = new FormData();

    formData.append(
      'request',
      new Blob(
        [
          JSON.stringify({
            title: request.title,
            description: request.description,
            contentType: request.contentType,
            estimatedDurationSeconds: request.estimatedDurationSeconds,
            isMandatory: request.isMandatory,
            textBody: request.textBody
          })
        ],
        {
          type: 'application/json'
        }
      )
    );

    if (request.contentFile) {
      formData.append('content', request.contentFile);
    }
    return formData;
  }

  getDocumentUrl(contentId: number): string {
    return `${environment.apiUrl}/module-content/${contentId}/document`;
  }

  getVideoUrl(contentId: number): string {
    return `${environment.apiUrl}/module-content/${contentId}/video`;
  }

  publishCourse(courseId: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${courseId}/publish`, {});
  }

  archiveCourse(courseId: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${courseId}/archive`, {});
  }

  restoreCourse(courseId: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${courseId}/restore`, {});
  }

  requestForOtherDepartementCourse(courseId: number, reason: string) {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/course-requests/${courseId}/department`, { reason });
  }

  getThumbnail(id: number) {
    return this.http.get(`${this.url}/${id}/thumbnail`, {
      responseType: 'blob'
    });
  }

  updateModuleContentOrder(
    moduleId: number,
    request: {
      contents: {
        contentId: number;
        sequenceOrder: number;
      }[];
    }
  ) {
    return this.http.put(
      `${this.url}/modules/${moduleId}/contents/order`,
      request
    );
  }
  updateCourseModuleOrder(courseId: number,
    request: {
      modules: {
        moduleId: number;
        sequenceOrder: number;
      }[];
    }
  ) {
    return this.http.put(
      `${environment.apiUrl}/course-module/${courseId}/order`,
      request
    );
  }
}
