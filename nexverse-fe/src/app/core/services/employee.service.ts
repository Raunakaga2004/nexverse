import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpContext, HttpParams } from "@angular/common/http";
import { SKIP_GLOBAL_ERROR_HANDLER } from "../constants/http-context.constants";
import { ApiResponse } from "../models/api-response.model";
import { Observable } from "rxjs";
import { PageResponse } from "../models/page-response.model";
import { Employee } from "../models/employee.model";
import { ImportEmployeeResponse } from "../models/import-employee-response.model";
import { EmployeeProgressResponse } from "../models/learning-progress.model";
import { MyLearningRequest } from "../models/my-learning.model";

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  getEligibleDepartmentManagers(page: number, departmentName: string, search: string) {
    return this.http.get<ApiResponse<PageResponse<Employee>>>(`${this.url}?page=${page}&size=10&sortBy=createdAt&direction=desc&search=${search}&isEnabled=true&status=ACTIVE`, this.skipGlobalErrorHandler)
  }
  private url = `${environment.apiUrl}/employee`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getEmployees(page: number, size: number, sortBy: string, direction: string, role: string, search: string, isEnabled: boolean | "", status: string): Observable<ApiResponse<PageResponse<Employee>>> {
    return this.http.get<ApiResponse<PageResponse<Employee>>>(`${this.url}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}&role=${role === 'ALL' ? '' : role}&search=${search}&isEnabled=${isEnabled === true ? 'true' : isEnabled === false ? 'false' : ''}&status=${status === 'ALL' ? '' : status}`, this.skipGlobalErrorHandler)
  }

  getEmployee(id: number) {
    return this.http.get<ApiResponse<void>>(`${this.url}/${id}`);
  }

  editEmployee(id: number, formData: FormData): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}`, formData);
  }

  createEmployee(formData: FormData): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}`, formData);
  }

  enableEmployee(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: true });
  }

  disableEmployee(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: false });
  }

  suspendEmployee(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/suspend`, {});
  }

  unsuspendEmployee(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/reactivate`, {});
  }

  importEmployees(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<ImportEmployeeResponse>>(`${this.url}/import`, formData);
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(
      `${this.url}/import/template`,
      { responseType: 'blob' }
    );
  }

  getEmployeeProgress(
    employeeId: number,
    search: string,
    progressStatus: string,
    requestStatus: string,
    enrollmentType: string,
    page: number,
    size: number
  ): Observable<ApiResponse<EmployeeProgressResponse>> {
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
    return this.http.get<ApiResponse<EmployeeProgressResponse>>(
      `${this.url}/${employeeId}/learning-progress`, { params }
    );
  }
}