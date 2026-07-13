import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { isJwtUsable } from './jwt.util';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('comunigest_token');
  const usableToken = isJwtUsable(token);

  if (token && !usableToken) {
    clearSession();
  }

  const authenticatedRequest = usableToken
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !request.url.endsWith('/auth/login')) {
        clearSession();
        void router.navigateByUrl('/login');
      }
      return throwError(() => error);
    })
  );
};

function clearSession(): void {
  localStorage.removeItem('comunigest_user');
  localStorage.removeItem('comunigest_token');
}
