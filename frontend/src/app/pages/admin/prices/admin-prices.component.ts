import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { PriceRule } from '../../../models/price-rule.model';

@Component({
  selector: 'app-admin-prices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-prices.component.html',
  styleUrl: './admin-prices.component.css'
})
export class AdminPricesComponent implements OnInit {
  private apiService = inject(ApiService);

  prices: PriceRule[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  editingId: number | null = null;
  editPriceValue: number = 0;

  ngOnInit(): void {
    this.loadPrices();
  }

  loadPrices(): void {
    this.loading = true;
    this.errorMessage = '';
    this.apiService.getAllPrices().subscribe({
      next: (data) => {
        this.prices = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des tarifs.';
      }
    });
  }

  startEdit(price: PriceRule): void {
    this.editingId = price.id;
    this.editPriceValue = price.price;
  }

  cancelEdit(): void {
    this.editingId = null;
    this.editPriceValue = 0;
  }

  savePrice(price: PriceRule): void {
    if (this.editPriceValue <= 0) {
      this.errorMessage = 'Le prix doit être supérieur à 0.';
      return;
    }

    this.apiService.updatePrice(price.id, { price: this.editPriceValue }).subscribe({
      next: () => {
        this.successMessage = 'Prix mis à jour avec succès.';
        this.editingId = null;
        this.loadPrices();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour du prix.';
      }
    });
  }

  toggleActive(price: PriceRule): void {
    this.apiService.updatePrice(price.id, { active: !price.active }).subscribe({
      next: () => {
        this.successMessage = `Tarif ${!price.active ? 'activé' : 'désactivé'} avec succès.`;
        this.loadPrices();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour.';
      }
    });
  }

  getBookingTypeLabel(type: string): string {
    switch (type) {
      case 'NIGHT_STAY': return 'Nuitée';
      case 'EXTENDED_STAY': return 'Séjour prolongé';
      case 'SPA_SESSION': return 'Session spa';
      default: return type;
    }
  }

  getDayTypeLabel(type: string): string {
    switch (type) {
      case 'WEEKDAY': return 'Semaine';
      case 'WEEKEND': return 'Week-end';
      case 'HOLIDAY': return 'Jours fériés';
      default: return type;
    }
  }
}