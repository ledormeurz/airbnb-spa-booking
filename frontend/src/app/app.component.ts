import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { AuthService } from './services/auth.service';
import { User } from './models/user.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  isAdmin = false;
  showMobileMenu = false;
  showUserMenu = false;
  currentYear = new Date().getFullYear();
  private sub?: Subscription;

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.sub = this.auth.authState$.subscribe(user => {
      this.currentUser = user;
      this.isAdmin = user ? this.auth.isAdmin() : false;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  toggleMobileMenu(): void {
    this.showMobileMenu = !this.showMobileMenu;
    if (this.showMobileMenu) this.showUserMenu = false;
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  closeMenus(): void {
    this.showMobileMenu = false;
    this.showUserMenu = false;
  }

  logout(): void {
    this.auth.logout();
    this.closeMenus();
  }
}