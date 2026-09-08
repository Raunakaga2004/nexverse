import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CurrentUser } from '../models/current-user.model';

@Injectable({
  providedIn: 'root'
})
export class CurrentUserService {
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(null);
  constructor() { }

  currentUser$: Observable<CurrentUser | null> = this.currentUserSubject.asObservable();

  getCurrentUserRole() {
    return this.currentUserSubject.value?.role;
  }

  setCurrentUser(user: CurrentUser) {
    this.currentUserSubject.next(user);
  }

  clear() {
    this.currentUserSubject.next(null);
  }

  getCurrentDepartmentId(){
    return this.currentUserSubject.value?.departmentId;
  }

  isLoggedIn(){
    return this.currentUserSubject.value != null;
  }
}
