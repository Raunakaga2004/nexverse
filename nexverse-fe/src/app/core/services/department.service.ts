import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpContext } from "@angular/common/http";
import { SKIP_GLOBAL_ERROR_HANDLER } from "../constants/http-context.constants";
import { ApiResponse } from "../models/api-response.model";
import { Observable } from "rxjs";
import { PageResponse } from "../models/page-response.model";
import { Department } from "../models/department.model";

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  private url = `${environment.apiUrl}/department`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getDepartments(page: number, size: number, sortBy: string, direction: string, search: string, isEnabled: boolean | ""): Observable<ApiResponse<PageResponse<Department>>> {
    return this.http.get<ApiResponse<PageResponse<Department>>>(`${this.url}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}&search=${search}&isEnabled=${isEnabled === true ? 'true' : isEnabled === false ? 'false' : ''}`, this.skipGlobalErrorHandler)
  }

  getDepartment(id: number) {
    return this.http.get<ApiResponse<Department>>(`${this.url}/${id}`);
  }

  editDepartment(id: number, formData: FormData) : Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}`, formData);
  }

  createDepartment(formData: FormData) : Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}`, formData);
  }

  enableDepartment(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, {isEnabled : true});
  }

  disableDepartment(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, {isEnabled : false});
  }

  assignManager(depId: number, employeeId : number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${depId}/manager`, employeeId);
  }
}
