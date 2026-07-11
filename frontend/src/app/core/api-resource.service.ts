import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiResourceService {
  constructor(private readonly http: HttpClient) {}

  list<T>(resource: string): Observable<T[]> {
    return this.http.get<T[]>(`${environment.apiUrl}/${resource}`);
  }

  path<T>(path: string): Observable<T> {
    return this.http.get<T>(`${environment.apiUrl}/${path}`);
  }

  get<T>(resource: string, id: number): Observable<T> {
    return this.http.get<T>(`${environment.apiUrl}/${resource}/${id}`);
  }

  create<T>(resource: string, payload: unknown): Observable<T> {
    return this.http.post<T>(`${environment.apiUrl}/${resource}`, payload);
  }

  update<T>(resource: string, id: number, payload: unknown): Observable<T> {
    return this.http.put<T>(`${environment.apiUrl}/${resource}/${id}`, payload);
  }

  delete(resource: string, id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/${resource}/${id}`);
  }

  patch<T>(resource: string, id: number, action: string, payload: unknown = {}): Observable<T> {
    return this.http.patch<T>(`${environment.apiUrl}/${resource}/${id}/${action}`, payload);
  }
}
