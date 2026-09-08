import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequest } from '../models/login.model';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { ForgotPasswordRequest } from '../models/forgot-password.model';
import { SetPasswordRequest } from '../models/set-password.model';
import { SKIP_GLOBAL_ERROR_HANDLER, SKIP_REFRESH } from '../constants/http-context.constants';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private url = `${environment.apiUrl}/auth`;
  constructor(private http: HttpClient) { }
  skipGlobalErrorAndRefresh = {
    context: new HttpContext()
    .set(SKIP_GLOBAL_ERROR_HANDLER, true)
    .set(SKIP_REFRESH, true)
  }

  login(loginFormValue: LoginRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/login`, loginFormValue, this.skipGlobalErrorAndRefresh)
  }

  refreshToken() {
    return this.http.post(`${this.url}/refresh-token`, this.skipGlobalErrorAndRefresh)
  }

  forgotPassword(forgotPassFormValue: ForgotPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/forgot-password`, forgotPassFormValue, this.skipGlobalErrorAndRefresh);
  }

  resetPassword(resetPassFormValue: SetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/reset-password`, resetPassFormValue, this.skipGlobalErrorAndRefresh);
  }

  activateAccount(activateAccountFormValue: SetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/activate-account`, activateAccountFormValue, this.skipGlobalErrorAndRefresh);
  }

  logout(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.url}/logout`, this.skipGlobalErrorAndRefresh);
  }
}