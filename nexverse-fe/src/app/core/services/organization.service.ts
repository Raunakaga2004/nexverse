import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { HttpClient, HttpContext } from '@angular/common/http';
import { SKIP_GLOBAL_ERROR_HANDLER } from '../constants/http-context.constants';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { Organization } from '../models/organizations.model';
import { PageResponse } from '../models/page-response.model';
import { OrganizationDetail } from '../models/organization-detail.model';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private url = `${environment.apiUrl}/organization`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getOrganizations(page: number, size: number, sort: string, direction: string, search: string, isEnabled: boolean | '', status: string): Observable<ApiResponse<PageResponse<Organization>>> {
    return this.http.get<ApiResponse<PageResponse<Organization>>>(`${this.url}?page=${page}&size=${size}&sort=${sort},${direction}&search=${search}&isEnabled=${isEnabled === true ? 'true' : isEnabled === false ? 'false' : ''}&status=${status === 'ALL' ? '' : status}`, this.skipGlobalErrorHandler)
  }

  getOrganization(id: number) {
    return this.http.get<ApiResponse<OrganizationDetail>>(`${this.url}/${id}`);
  }

  editOrganization(id: number, formData: FormData) : Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.url}/${id}`, formData);
  }

  createOrganization(formData: FormData) : Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}`, formData);
  }

  enableOrganization(id: number) {
    return this.http.patch<ApiResponse<OrganizationDetail>>(`${this.url}/${id}/activation`, {isEnabled : true});
  }

  disableOrganization(id: number) {
    return this.http.patch<ApiResponse<OrganizationDetail>>(`${this.url}/${id}/activation`, {isEnabled : false});
  }

  suspendOrganization(id: number) {
    return this.http.patch<ApiResponse<OrganizationDetail>>(`${this.url}/${id}/suspend`, {});
  }

  unsuspendOrganization(id: number) {
    return this.http.patch<ApiResponse<OrganizationDetail>>(`${this.url}/${id}/reactivate`, {});
  }

  getLogo(id : number){
    return this.http.get(`${this.url}/${id}/logo`, {
      responseType: 'blob'
    })
  }
}
