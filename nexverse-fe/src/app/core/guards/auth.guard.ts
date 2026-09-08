import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CurrentUserService } from '../services/current-user.service';

export const AuthGuard: CanActivateFn = (route, state) => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);
  if (currentUserService.isLoggedIn()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
