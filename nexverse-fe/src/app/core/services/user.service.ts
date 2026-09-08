import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { ApiResponse } from '../models/api-response.model';
import { CurrentUser } from '../models/current-user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private url = `${environment.apiUrl}/user`;
  constructor(private http: HttpClient) { }

  getCurrentUser() {
    return this.http.get<ApiResponse<CurrentUser>>(`${this.url}/me`);
  }
}