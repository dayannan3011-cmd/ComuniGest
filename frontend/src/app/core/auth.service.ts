import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';
import { isJwtUsable } from './jwt.util';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'comunigest_user';
  private readonly tokenKey = 'comunigest_token';
  readonly accessDeniedMessage = signal('');

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap((response) => {
        localStorage.setItem(this.storageKey, JSON.stringify(response));
        localStorage.setItem(this.tokenKey, response.token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    localStorage.removeItem(this.tokenKey);
    this.clearAccessDenied();
    this.router.navigateByUrl('/login');
  }

  isAuthenticated(): boolean {
    const authenticated = isJwtUsable(localStorage.getItem(this.tokenKey));
    if (!authenticated) {
      this.clearSession();
    }
    return authenticated;
  }

  currentUser(): LoginResponse | null {
    if (!this.isAuthenticated()) return null;
    const raw = localStorage.getItem(this.storageKey);
    try {
      return raw ? JSON.parse(raw) as LoginResponse : null;
    } catch {
      this.clearSession();
      return null;
    }
  }

  showAccessDenied(): void {
    this.accessDeniedMessage.set('Acceso denegado. No tienes permisos para acceder a esta sección.');
  }

  clearAccessDenied(): void {
    this.accessDeniedMessage.set('');
  }

  private clearSession(): void {
    localStorage.removeItem(this.storageKey);
    localStorage.removeItem(this.tokenKey);
  }
}
