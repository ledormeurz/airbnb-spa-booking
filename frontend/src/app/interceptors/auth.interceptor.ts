import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);

  const isPublicRoute = req.url.includes('/api/public/');
  const isApiRoute = req.url.includes('/api/');

  if (!isPublicRoute && isApiRoute) {
    const authHeader = authService.getAuthHeader();
    if (authHeader) {
      const cloned = req.clone({
        setHeaders: {
          Authorization: authHeader
        }
      });
      return next(cloned).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            authService.logout();
            // Use window.location instead of Router to avoid DI issues
            window.location.href = '/login';
          }
          return throwError(() => error);
        })
      );
    }
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicRoute) {
        authService.logout();
        window.location.href = '/login';
      }
      return throwError(() => error);
    })
  );
};