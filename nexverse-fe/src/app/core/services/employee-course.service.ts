import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpParams } from "@angular/common/http";
import { BrowseCourse, BrowseCoursesRequest } from "../models/browse-course.model";
import { ApiResponse } from "../models/api-response.model";
import { PageResponse } from "../models/page-response.model";
import { CourseViewModel } from "../models/course-view.model";
import { Observable } from "rxjs";
import { LearningContent, LearningCourse } from "../models/course-learning.model";
import { MyLearningCourse, MyLearningRequest } from "../models/my-learning.model";
import { EmployeeSkill, EmployeeSkillRequest } from "../models/employee-skill.model";
import { CourseAccessRequestStatus, CourseRequest, DepartmentCourseRequest, EmployeeCourseRequest } from "../models/course-request.model";
import { AssignCourseRequest } from "../models/assign-course.model";

@Injectable({
  providedIn: 'root'
})
export class EmployeeCourseService {
  private url = `${environment.apiUrl}/employee/course`;
  private baseUrl = `${environment.apiUrl}`

  constructor(private http: HttpClient) { }

  browseCourses(request: BrowseCoursesRequest, page: number, size: number
  ) {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    Object.entries(request).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, value);
      }
    });
    return this.http.get<ApiResponse<PageResponse<BrowseCourse>>>(
      `${this.url}`,
      { params }
    );
  }

  getCourse(courseId: number) {
    return this.http.get<ApiResponse<CourseViewModel>>(
      `${environment.apiUrl}/employee/course/${courseId}`
    );
  }

  accessCourse(courseId: number, reason: string ): Observable<void> {

    return this.http.post<void>(
      `${environment.apiUrl}/course-requests/${courseId}/employee`,
      {reason}
    );
  }

  getLearningCourse(courseId: number) {
    return this.http.get<ApiResponse<LearningCourse>>(
      `${environment.apiUrl}/employee/course/${courseId}/learn`
    );

  }

  getContent(
    courseId: number,
    contentId: number
  ) {
    return this.http.get<ApiResponse<LearningContent>>(
      `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}`
    );

  }

  getTextContent(
    courseId: number,
    contentId: number
  ) {

    return this.http.get<ApiResponse<{ body: string }>>(
      `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/text`
    );

  }

  getVideoUrl(
    courseId: number,
    contentId: number
  ): string {

    return `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/video`;

  }

  getDocumentUrl(
    courseId: number,
    contentId: number
  ): string {

    return `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/document`;

  }

  getDocument(courseId: number, contentId: number): Observable<Blob> {
    return this.http.get(
      `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/document`,
      {
        responseType: 'blob'
      }
    );
  }

  startContent(
    courseId: number,
    contentId: number
  ): Observable<void> {

    return this.http.post<void>(
      `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/start`,
      {}
    );

  }

  completeContent(
    courseId: number,
    contentId: number
  ): Observable<void> {

    return this.http.post<void>(
      `${environment.apiUrl}/employee/course/${courseId}/contents/${contentId}/complete`,
      {}
    );

  }

  getMyLearningCourses(
    search: string,
    progressStatus: string,
    requestStatus: string,
    enrollmentType: string,
    page: number,
    size: number
  ): Observable<ApiResponse<PageResponse<MyLearningCourse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (search) {
      params = params.set('search', search);
    }

    if (progressStatus) {
      params = params.set('progressStatus', progressStatus==='ALL'?'':progressStatus);
    }

    if (requestStatus) {
      params = params.set('requestStatus', requestStatus==='ALL'?'':requestStatus);
    }

    if (enrollmentType) {
      params = params.set('enrollmentType', enrollmentType==='ALL'?'':enrollmentType);
    }

    return this.http.get<ApiResponse<PageResponse<MyLearningCourse>>>(
      `${environment.apiUrl}/employee/course/my-learning`,
      { params }
    );
  }

  getEmployeeSkills(
    request: EmployeeSkillRequest
  ): Observable<ApiResponse<EmployeeSkill[]>> {
    let params = new HttpParams();
    if (request.skillName) {
      params = params.set('skillName', request.skillName);
    }
    if (request.skillLevel) {
      params = params.set('skillLevel', request.skillLevel);
    }
    return this.http.get<ApiResponse<EmployeeSkill[]>>(
      `${environment.apiUrl}/employee/course/skills`,
      {
        params
      }
    );
  }
  getDepartmentCourseRequests(
    request: CourseRequest,
    page: number,
    size: number
  ): Observable<ApiResponse<PageResponse<DepartmentCourseRequest>>> {
 
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt')
      .set('direction', 'DESC');
 
    if (request.search?.trim()) {
      params = params.set('search', request.search.trim());
    }
 
    if (request.requestStatus) {
      params = params.set('requestStatus', request.requestStatus);
    }
 
    return this.http.get<
      ApiResponse<PageResponse<DepartmentCourseRequest>>
    >(
      `${this.baseUrl}/course-requests/department`,
      { params }
    );
  }

  getEmployeeCourseRequests(
    request: CourseRequest,
    page: number,
    size: number
  ): Observable<ApiResponse<PageResponse<EmployeeCourseRequest>>> {
 
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt')
      .set('direction', 'DESC');
 
    if (request.search?.trim()) {
      params = params.set('search', request.search.trim());
    }
 
    if (request.requestStatus) {
      params = params.set('requestStatus', request.requestStatus);
    }
 
    return this.http.get<
      ApiResponse<PageResponse<EmployeeCourseRequest>>
    >(
      `${this.baseUrl}/course-requests/employee`,
      { params }
    );
  }
 
  changeDepartmentRequestStatus(
    requestId: number,
    status: CourseAccessRequestStatus
  ): Observable<ApiResponse<void>> {
 
    return this.http.patch<ApiResponse<void>>(
      `${this.baseUrl}/course-requests/department/${requestId}/status`,
      {
        status : status
      }
    );
  }
 
  changeEmployeeRequestStatus(
    requestId: number,
    status: CourseAccessRequestStatus
  ): Observable<ApiResponse<void>> {
    console.log(status === CourseAccessRequestStatus.APPROVED ? true : false)
 
    return this.http.patch<ApiResponse<void>>(
      `${this.baseUrl}/course-requests/employee/${requestId}/status`,
      {
        status
      }
    );
  }

   assignCourseToEmployees(
    request: AssignCourseRequest
  ): Observable<ApiResponse<void>> {
 
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/course-requests/assign`,
      request
    );
  }
}