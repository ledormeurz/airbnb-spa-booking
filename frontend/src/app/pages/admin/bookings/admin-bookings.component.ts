import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { Booking, BookingStatus, BookingType } from '../../../models/booking.model';

@Component({
  selector: 'app-admin-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-bookings.component.html',
  styleUrl: './admin-bookings.component.css'
})
export class AdminBookingsComponent implements OnInit {
  private apiService = inject(ApiService);

  bookings: Booking[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  filters = {
    status: '',
    bookingType: '',
    startDate: '',
    endDate: '',
    clientName: ''
  };

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.loading = true;
    this.errorMessage = '';
    const activeFilters: any = {};
    if (this.filters.status) activeFilters.status = this.filters.status;
    if (this.filters.bookingType) activeFilters.bookingType = this.filters.bookingType;
    if (this.filters.startDate) activeFilters.startDate = this.filters.startDate;
    if (this.filters.endDate) activeFilters.endDate = this.filters.endDate;
    if (this.filters.clientName) activeFilters.clientName = this.filters.clientName;

    this.apiService.getAllBookings(Object.keys(activeFilters).length > 0 ? activeFilters : undefined).subscribe({
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

  updateStatus(booking: Booking, status: string): void {
    const label = status === 'CONFIRMED' ? 'confirmer' : 'refuser';
    const reason = status === 'REJECTED' ? window.prompt('Motif du refus :') : undefined;

    if (status === 'REJECTED' && !reason) {
      // User cancelled prompt
      return;
    }

    if (window.confirm(`Êtes-vous sûr de vouloir ${label} cette réservation ?`)) {
      this.apiService.updateBookingStatus(booking.id, status, reason || undefined).subscribe({
        next: () => {
          this.successMessage = `Réservation ${label}e avec succès.`;
          this.loadBookings();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || `Erreur lors de la mise à jour du statut.`;
        }
      });
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