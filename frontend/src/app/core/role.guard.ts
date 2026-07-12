import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.currentUser()?.perfil === 'ADMINISTRADOR') {
    auth.clearAccessDenied();
    return true;
  }

  auth.showAccessDenied();
  return router.createUrlTree(['/turnos']);
};
