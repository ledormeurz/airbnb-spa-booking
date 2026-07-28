import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api';
  private currentUser: User | null = null;
  private credentials: { email: string; password: string } | null = null;
  private authState = new BehaviorSubject<User | null>(null);

  authState$ = this.authState.asObservable();

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<User> {
    this.credentials = { email, password };
    return this.loadUserProfile().pipe(
      tap(user => {
        this.currentUser = user;
        this.authState.next(user);
      }),
      catchError(error => {
        this.credentials = null;
        return throwError(() => error);
      })
    );
  }

  private loadUserProfile(): Observable<User> {
    const headers = this.buildAuthHeaders();
    return this.http.get<User>(`${this.apiUrl}/user/profile`, { headers });
  }

  logout(): void {
    this.currentUser = null;
    this.credentials = null;
    this.authState.next(null);
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }

  isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ROLE_ADMIN' || this.currentUser?.role === 'ADMIN';
  }

  getCredentials(): { email: string; password: string } | null {
    return this.credentials;
  }

  getAuthHeader(): string | null {
    if (!this.credentials) return null;
    return 'Basic ' + btoa(`${this.credentials.email}:${this.credentials.password}`);
  }

  private buildAuthHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    const authHeader = this.getAuthHeader();
    if (authHeader) {
      headers = headers.set('Authorization', authHeader);
    }
    return headers;
  }
}