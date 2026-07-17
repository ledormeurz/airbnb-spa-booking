import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { Booking, BookingStatus } from '../../../models/booking.model';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './booking-detail.component.html',
  styleUrl: './booking-detail.component.css'
})
export class BookingDetailComponent implements OnInit {
  private apiService = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  booking: Booking | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadBooking(id);
    }
  }

  loadBooking(id: number): void {
    this.loading = true;
    this.apiService.getBooking(id).subscribe({
      next: (data) => {
        this.booking = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement de la réservation.';
      }
    });
  }

  cancelBooking(): void {
    if (!this.booking) return;
    if (window.confirm(`Êtes-vous sûr de vouloir annuler cette réservation ?`)) {
      this.apiService.cancelBooking(this.booking.id).subscribe({
        next: () => {
          this.successMessage = 'Réservation annulée avec succès.';
          this.loadBooking(this.booking!.id);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de l\'annulation.';
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/user/bookings']);
  }

  editBooking(): void {
    if (this.booking) {
      this.router.navigate(['/booking'], { queryParams: { id: this.booking.id } });
    }
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