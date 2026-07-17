import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  private apiService = inject(ApiService);

  users: User[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  showModal = false;
  editingId: number | null = null;
  form: {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    enabled: boolean;
  } = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    role: 'USER',
    enabled: true
  };

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.apiService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des utilisateurs.';
      }
    });
  }

  openCreateModal(): void {
    this.editingId = null;
    this.form = { username: '', password: '', firstName: '', lastName: '', email: '', role: 'USER', enabled: true };
    this.showModal = true;
  }

  openEditModal(user: User): void {
    this.editingId = user.id;
    this.form = {
      username: user.username,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      role: user.role === 'ROLE_ADMIN' || user.role === 'ADMIN' ? 'ADMIN' : 'USER',
      enabled: user.enabled
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingId = null;
  }

  saveUser(): void {
    if (!this.form.firstName || !this.form.lastName || !this.form.email) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
      return;
    }

    if (this.editingId) {
      // Edit: NEVER send password
      const payload = {
        firstName: this.form.firstName,
        lastName: this.form.lastName,
        email: this.form.email,
        role: this.form.role,
        enabled: this.form.enabled
      };
      this.apiService.updateUser(this.editingId, payload).subscribe({
        next: () => {
          this.successMessage = 'Utilisateur mis à jour avec succès.';
          this.closeModal();
          this.loadUsers();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour.';
        }
      });
    } else {
      // Create: include password
      if (!this.form.username || !this.form.password) {
        this.errorMessage = 'Le nom d\'utilisateur et le mot de passe sont requis.';
        return;
      }
      this.apiService.createUser(this.form).subscribe({
        next: () => {
          this.successMessage = 'Utilisateur créé avec succès.';
          this.closeModal();
          this.loadUsers();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la création.';
        }
      });
    }
  }

  getRoleLabel(role: string): string {
    if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'Administrateur';
    return 'Utilisateur';
  }

  getRoleBadge(role: string): string {
    if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'badge badge-confirmed';
    return 'badge badge-pending';
  }
}