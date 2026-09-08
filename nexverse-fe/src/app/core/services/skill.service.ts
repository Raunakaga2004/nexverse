import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpContext } from "@angular/common/http";
import { SKIP_GLOBAL_ERROR_HANDLER } from "../constants/http-context.constants";
import { ApiResponse } from "../models/api-response.model";
import { Observable } from "rxjs";
import { PageResponse } from "../models/page-response.model";
import { Skill } from "../models/skill.model";

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private url = `${environment.apiUrl}/skill`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getSkills(page: number, size: number, sortBy: string, direction: string, search: string, isEnabled: boolean | ""): Observable<ApiResponse<PageResponse<Skill>>> {
    return this.http.get<ApiResponse<PageResponse<Skill>>>(`${this.url}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}&search=${search}&isEnabled=${isEnabled === true ? 'true' : isEnabled === false ? 'false' : ''}`, this.skipGlobalErrorHandler)
  }

  getSkill(id: number) {
    return this.http.get<ApiResponse<void>>(`${this.url}/${id}`);
  }

  editSkill(id: number, formData: FormData): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}`, formData);
  }

  createSkill(formData: FormData): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}`, formData);
  }

  enableSkill(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: true });
  }

  disableSkill(id: number) {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}/activation`, { isEnabled: false });
  }

  getIcon(id: number) {
    return this.http.get(`${this.url}/${id}/icon`, {
      responseType: 'blob'
    });
  }
}
