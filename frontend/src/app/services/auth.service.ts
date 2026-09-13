import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { AuthResponse, User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api';
  private currentUser: User | null = null;
  private accessToken: string | null = null;
  private authState = new BehaviorSubject<User | null>(null);

  authState$ = this.authState.asObservable();

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<User> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/public/login`, { email, password }).pipe(
      tap(response => {
        this.accessToken = response.accessToken;
        this.currentUser = response.user;
        this.authState.next(response.user);
      }),
      map(response => response.user),
      catchError(error => {
        this.clearSession();
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.clearSession();
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }

  isAuthenticated(): boolean {
    return this.currentUser !== null && this.accessToken !== null;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ROLE_ADMIN' || this.currentUser?.role === 'ADMIN';
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getAuthHeader(): string | null {
    if (!this.accessToken) return null;
    return `Bearer ${this.accessToken}`;
  }

  private clearSession(): void {
    this.currentUser = null;
    this.accessToken = null;
    this.authState.next(null);
  }
}
