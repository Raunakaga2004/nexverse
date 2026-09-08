import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SnackbarService } from '../services/snackbar.service';
import { catchError, throwError } from 'rxjs';
import { ErrorResponse } from '../models/error-response.model';
import { SKIP_GLOBAL_ERROR_HANDLER } from '../constants/http-context.constants';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackbarService = inject(SnackbarService);
  return next(req).pipe(
    catchError((httpError: HttpErrorResponse) => {
      if (httpError.status === 401) { // if auth failure refresh interceptor will handle it
        return throwError(() => httpError);
      }
      if (req.context.get(SKIP_GLOBAL_ERROR_HANDLER)) {
        return throwError(() => httpError);
      }
      const error = httpError.error as ErrorResponse;
      snackbarService.error(error.message);
      return throwError(() => httpError);
    })
  );
};