import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);

  const isApiRoute = req.url.includes('/api/');
  const skipAuth =
    req.url.includes('/api/public/login') ||
    req.url.includes('/api/public/register');

  let request = req;
  if (isApiRoute && !skipAuth) {
    const authHeader = authService.getAuthHeader();
    if (authHeader) {
      request = req.clone({
        setHeaders: {
          Authorization: authHeader
        }
      });
    }
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && isApiRoute && !skipAuth) {
        authService.logout();
        window.location.href = '/login';
      }
      return throwError(() => error);
    })
  );
};
