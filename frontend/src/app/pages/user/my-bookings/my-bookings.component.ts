import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { Booking, BookingStatus } from '../../../models/booking.model';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.css'
})
export class MyBookingsComponent implements OnInit {
  private apiService = inject(ApiService);
  private router = inject(Router);

  bookings: Booking[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.loading = true;
    this.errorMessage = '';
    this.apiService.getMyBookings().subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des réservations.';
      }
    });
  }

  cancelBooking(booking: Booking, event: Event): void {
    event.stopPropagation();
    if (window.confirm(`Êtes-vous sûr de vouloir annuler la réservation du ${booking.startDate} au ${booking.endDate} ?`)) {
      this.apiService.cancelBooking(booking.id).subscribe({
        next: () => {
          this.successMessage = 'Réservation annulée avec succès.';
          this.loadBookings();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de l\'annulation.';
        }
      });
    }
  }

  viewBooking(id: number): void {
    this.router.navigate(['/user/bookings', id]);
  }

  getStatusClass(status: BookingStatus): string {
    switch (status) {
      case BookingStatus.PENDING: return 'badge badge-pending';
      case BookingStatus.CONFIRMED: return 'badge badge-confirmed';
      case BookingStatus.REJECTED: return 'badge badge-rejected';
      case BookingStatus.CANCELLED: return 'badge badge-cancelled';
      default: return 'badge';
    }
  }

  getStatusLabel(status: BookingStatus): string {
    switch (status) {
      case BookingStatus.PENDING: return 'En attente';
      case BookingStatus.CONFIRMED: return 'Confirmée';
      case BookingStatus.REJECTED: return 'Refusée';
      case BookingStatus.CANCELLED: return 'Annulée';
      default: return status;
    }
  }

  getBookingTypeLabel(type: string): string {
    switch (type) {
      case 'NIGHT_STAY': return 'Nuitée';
      case 'EXTENDED_STAY': return 'Séjour prolongé';
      case 'SPA_SESSION': return 'Session spa';
      default: return type;
    }
  }
}