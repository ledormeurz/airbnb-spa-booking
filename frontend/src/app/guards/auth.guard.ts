import { Injectable, inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuardService {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    if (this.auth.isAuthenticated()) {
      return of(true);
    }

    const creds = this.auth.getCredentials();
    if (creds && !this.auth.isAuthenticated()) {
      return this.auth.login(creds.email, creds.password).pipe(
        map(() => true),
        catchError(() => {
          this.auth.logout();
          return of(this.router.parseUrl('/login'));
        })
      );
    }

    return of(this.router.parseUrl('/login'));
  }
}

export const AuthGuard: CanActivateFn = () => {
  return inject(AuthGuardService).canActivate();
};