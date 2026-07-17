import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../services/api.service';
import { PriceRule } from '../../../models/price-rule.model';

interface GroupedPrices {
  [key: string]: PriceRule[];
}

@Component({
  selector: 'app-prices',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './prices.component.html',
  styleUrl: './prices.component.css'
})
export class PricesComponent implements OnInit {
  private api = inject(ApiService);

  prices: PriceRule[] = [];
  loading = true;
  error = false;

  bookingTypeLabels: Record<string, string> = {
    'NIGHT_STAY': 'Nuitée',
    'EXTENDED_STAY': 'Séjour',
    'SPA_SESSION': 'Séance Spa'
  };

  dayTypeLabels: Record<string, string> = {
    'WEEKDAY': 'Semaine',
    'WEEKEND': 'Week-end',
    'HOLIDAY': 'Jours fériés',
    'ALL': 'Tous les jours'
  };

  ngOnInit(): void {
    this.api.getPrices().subscribe({
      next: (data) => {
        this.prices = data.filter(p => p.active);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = true;
      }
    });
  }

  get groupedPrices(): GroupedPrices {
    const groups: GroupedPrices = {};
    for (const price of this.prices) {
      const key = price.bookingType;
      if (!groups[key]) {
        groups[key] = [];
      }
      groups[key].push(price);
    }
    return groups;
  }

  getGroupKeys(): string[] {
    return Object.keys(this.groupedPrices);
  }

  getBookingTypeLabel(type: string): string {
    return this.bookingTypeLabels[type] || type;
  }

  getDayTypeLabel(type: string): string {
    return this.dayTypeLabels[type] || type;
  }
}