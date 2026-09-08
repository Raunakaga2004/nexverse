import { inject } from '@angular/core';
import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { Router } from '@angular/router';
import {
  BehaviorSubject,
  catchError,
  filter,
  switchMap,
  take,
  throwError
} from 'rxjs';

import { SKIP_REFRESH } from '../constants/http-context.constants';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshSubject = new BehaviorSubject<boolean>(false);
export const refreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (req.context.get(SKIP_REFRESH)) {
    return next(req);
  }
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401) {
        return throwError(() => error);
      }
      if (isRefreshing) {
        return refreshSubject.pipe(
          filter(Boolean),
          take(1),
          switchMap(() => next(req))
        );
      }
      isRefreshing = true;
      refreshSubject.next(false);
      return authService.refreshToken().pipe(
        switchMap(() => {
          isRefreshing = false;
          refreshSubject.next(true);
          return next(req);
        }),
        catchError((refreshError) => {
          isRefreshing = false;
          authService.logout();
          router.navigateByUrl('/login');
          return throwError(() => refreshError);
        })
      );
    })
  );
};