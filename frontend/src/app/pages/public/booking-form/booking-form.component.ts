import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { BookingType, BookingRequest } from '../../../models/booking.model';

function futureDateValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const date = new Date(control.value);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date >= today ? null : { pastDate: 'La date doit être dans le futur' };
}

function endAfterStartValidator(group: AbstractControl): ValidationErrors | null {
  const start = group.get('startDate')?.value;
  const end = group.get('endDate')?.value;
  if (!start || !end) return null;
  return new Date(end) > new Date(start) ? null : { endBeforeStart: 'La date de fin doit être après la date de début' };
}

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './booking-form.component.html',
  styleUrl: './booking-form.component.css'
})
export class BookingFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private apiService = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  bookingForm: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';

  bookingTypes = [
    { value: BookingType.NIGHT_STAY, label: 'Nuitée' },
    { value: BookingType.EXTENDED_STAY, label: 'Séjour' },
    { value: BookingType.SPA_SESSION, label: 'Séance spa' }
  ];

  constructor() {
    this.bookingForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      startDate: ['', [Validators.required, futureDateValidator]],
      endDate: ['', [Validators.required, futureDateValidator]],
      numberOfGuests: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
      bookingType: [BookingType.NIGHT_STAY, Validators.required],
      message: [''],
      agreedToRules: [false, Validators.requiredTrue]
    }, { validators: endAfterStartValidator });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['startDate']) {
        this.bookingForm.patchValue({ startDate: params['startDate'] });
      }
      if (params['endDate']) {
        this.bookingForm.patchValue({ endDate: params['endDate'] });
      }
    });
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const data: BookingRequest = {
      firstName: this.bookingForm.value.firstName,
      lastName: this.bookingForm.value.lastName,
      email: this.bookingForm.value.email,
      phone: this.bookingForm.value.phone,
      startDate: this.bookingForm.value.startDate,
      endDate: this.bookingForm.value.endDate,
      numberOfGuests: this.bookingForm.value.numberOfGuests,
      bookingType: this.bookingForm.value.bookingType,
      message: this.bookingForm.value.message,
      agreedToRules: this.bookingForm.value.agreedToRules
    };

    this.apiService.createBooking(data).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Votre réservation a été envoyée avec succès ! Nous vous contacterons sous peu.';
        this.bookingForm.reset({
          numberOfGuests: 1,
          bookingType: BookingType.NIGHT_STAY,
          agreedToRules: false
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Une erreur est survenue lors de la soumission. Veuillez réessayer.';
      }
    });
  }

  get f() {
    return this.bookingForm.controls;
  }
}