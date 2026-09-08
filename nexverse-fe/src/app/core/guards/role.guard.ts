import { CanActivateFn, Router } from '@angular/router';
import { CurrentUserService } from '../services/current-user.service';
import { inject } from '@angular/core';

export const RoleGuard: CanActivateFn = (route, state) => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);
  const allowedRoles = route.data['roles'] as string[];
  const userRole = currentUserService.getCurrentUserRole();
  console.log(allowedRoles)
  if(userRole == undefined){
    return false;
  }
  if (allowedRoles?.includes(userRole)) {
    return true;
  }
  return router.createUrlTree(['/unauthorized']);
};
