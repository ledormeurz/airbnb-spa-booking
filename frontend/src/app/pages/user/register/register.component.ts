import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor() {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordsMatchValidator });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { firstName, lastName, email, password } = this.registerForm.value;

    this.api.register({ firstName, lastName, email, password }).subscribe({
      next: () => {
        // Connexion automatique après une inscription réussie
        this.authService.login(email, password).subscribe({
          next: () => {
            this.loading = false;
            this.router.navigate(['/']);
          },
          error: () => {
            this.loading = false;
            this.router.navigate(['/login']);
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = this.extractError(err);
      }
    });
  }

  private extractError(err: any): string {
    const message = err?.error?.message;
    if (typeof message === 'string') {
      return message;
    }
    if (message && typeof message === 'object') {
      const first = Object.values(message)[0];
      if (typeof first === 'string') return first;
    }
    return "Une erreur est survenue lors de l'inscription. Veuillez réessayer.";
  }

  get f() {
    return this.registerForm.controls;
  }
}

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (password && confirmPassword && password !== confirmPassword) {
    return { passwordsMismatch: true };
  }
  return null;
}
