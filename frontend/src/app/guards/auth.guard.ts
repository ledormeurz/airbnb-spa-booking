import { Injectable, inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuardService {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    if (this.auth.isAuthenticated()) {
      return of(true);
    }
    return of(this.router.parseUrl('/login'));
  }
}

export const AuthGuard: CanActivateFn = () => {
  return inject(AuthGuardService).canActivate();
};
