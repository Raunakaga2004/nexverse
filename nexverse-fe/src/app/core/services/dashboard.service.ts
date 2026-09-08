import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { HttpClient, HttpContext } from '@angular/common/http';
import { SKIP_GLOBAL_ERROR_HANDLER } from '../constants/http-context.constants';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { SuperAdminDashboardModel } from '../models/super-admin-dashboard.model';
import { OrgAdminDashboardModel } from '../models/org-admin-dashboard.model';
import { DepManagerDashboardModel } from '../models/dep-manager-dashboard.model';
import { EmployeeDashboardModel } from '../models/employee-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private url = `${environment.apiUrl}/dashboard`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorHandler = {
    context: new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true)
  }

  getSuperAdminDashboard(period: string): Observable<ApiResponse<SuperAdminDashboardModel>> {
    return this.http.get<ApiResponse<SuperAdminDashboardModel>>(`${this.url}/super-admin?period=${period}`, this.skipGlobalErrorHandler)
  }

  getOrgAdminDashboard(period: string) {
    return this.http.get<ApiResponse<OrgAdminDashboardModel>>(`${this.url}/org-admin?period=${period}`, this.skipGlobalErrorHandler)
  }

  getDepManagerDashboard(period: string) {
    return this.http.get<ApiResponse<DepManagerDashboardModel>>(`${this.url}/manager?period=${period}`, this.skipGlobalErrorHandler)
  }

  getEmployeeDashboard(period: string) {
    return this.http.get<ApiResponse<EmployeeDashboardModel>>(`${this.url}/employee?period=${period}`, this.skipGlobalErrorHandler)
  }
}