import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AvailabilityService } from '../../../services/availability.service';
import { CalendarDay } from '../../../models/availability.model';

const MONTHS = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
const DAYS_HEADER = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

@Component({
  selector: 'app-availability',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './availability.component.html',
  styleUrl: './availability.component.css'
})
export class AvailabilityComponent implements OnInit {
  private availabilityService = inject(AvailabilityService);
  private router = inject(Router);

  currentMonth: number;
  currentYear: number;
  days: CalendarDay[] = [];
  daysHeader = DAYS_HEADER;
  loading = false;

  startDate: string | null = null;
  endDate: string | null = null;
  hoverDate: string | null = null;

  legendItems = [
    { color: 'rgba(91, 140, 90, 0.3)', label: 'Disponible' },
    { color: 'rgba(184, 84, 80, 0.3)', label: 'Réservé' },
    { color: 'rgba(107, 107, 107, 0.3)', label: 'Bloqué' },
    { color: '#eee', label: 'Passé' }
  ];

  constructor() {
    const now = new Date();
    this.currentMonth = now.getMonth() + 1;
    this.currentYear = now.getFullYear();
  }

  get monthLabel(): string {
    return `${MONTHS[this.currentMonth - 1]} ${this.currentYear}`;
  }

  get firstDayOfMonth(): number {
    return new Date(this.currentYear, this.currentMonth - 1, 1).getDay();
  }

  get daysInMonth(): number {
    return new Date(this.currentYear, this.currentMonth, 0).getDate();
  }

  get emptyDays(): number[] {
    const firstDay = this.firstDayOfMonth;
    const mondayOffset = firstDay === 0 ? 6 : firstDay - 1;
    return Array(mondayOffset).fill(0);
  }

  get selectedRangeInfo(): { start: string | null; end: string | null; nights: number; valid: boolean } {
    if (!this.startDate) {
      return { start: null, end: null, nights: 0, valid: false };
    }
    const start = new Date(this.startDate);
    if (this.endDate) {
      const end = new Date(this.endDate);
      const nights = Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
      return { start: this.formatDateDisplay(this.startDate), end: this.formatDateDisplay(this.endDate), nights, valid: nights > 0 };
    }
    return { start: this.formatDateDisplay(this.startDate), end: null, nights: 0, valid: false };
  }

  ngOnInit(): void {
    this.loadDays();
  }

  loadDays(): void {
    this.loading = true;
    this.availabilityService.getAvailableDates(this.currentMonth, this.currentYear).subscribe({
      next: (data) => {
        this.days = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.days = [];
      }
    });
  }

  getDayForDate(day: number): CalendarDay | undefined {
    return this.days.find(d => {
      const parts = d.date.split('-');
      return parseInt(parts[2], 10) === day;
    });
  }

  isDaySelectable(day: CalendarDay | undefined): boolean {
    return day !== undefined && (day.status === 'available' || day.status === 'booked');
  }

  isInRange(dayNum: number): boolean {
    if (!this.startDate || !this.endDate) return false;
    const current = new Date(this.currentYear, this.currentMonth - 1, dayNum);
    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    return current > start && current < end;
  }

  isRangeStart(dayNum: number): boolean {
    if (!this.startDate) return false;
    const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;
    return dateStr === this.startDate;
  }

  isRangeEnd(dayNum: number): boolean {
    if (!this.endDate) return false;
    const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;
    return dateStr === this.endDate;
  }

  isSelected(dayNum: number): boolean {
    return this.isRangeStart(dayNum) || this.isRangeEnd(dayNum);
  }

  selectDay(day: CalendarDay, dayNum: number): void {
    if (!this.isDaySelectable(day)) return;

    const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;

    if (!this.startDate || (this.startDate && this.endDate)) {
      this.startDate = dateStr;
      this.endDate = null;
    } else {
      const start = new Date(this.startDate);
      const clickDate = new Date(dateStr);
      if (clickDate <= start) {
        this.startDate = dateStr;
        this.endDate = null;
      } else {
        this.endDate = dateStr;
      }
    }
  }

  prevMonth(): void {
    if (this.currentMonth === 1) {
      this.currentMonth = 12;
      this.currentYear--;
    } else {
      this.currentMonth--;
    }
    this.startDate = null;
    this.endDate = null;
    this.loadDays();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) {
      this.currentMonth = 1;
      this.currentYear++;
    } else {
      this.currentMonth++;
    }
    this.startDate = null;
    this.endDate = null;
    this.loadDays();
  }

  goToBooking(): void {
    if (this.startDate && this.endDate) {
      this.router.navigate(['/booking'], {
        queryParams: { startDate: this.startDate, endDate: this.endDate }
      });
    }
  }

  getDaysArray(): number[] {
    return Array.from({ length: this.daysInMonth }, (_, i) => i + 1);
  }

  getEmptyDaysArray(): number[] {
    return this.emptyDays;
  }

  onDayClick(dayNum: number): void {
    const day = this.getDayForDate(dayNum);
    if (day) {
      this.selectDay(day, dayNum);
    }
  }

  private formatDateDisplay(dateStr: string): string {
    const parts = dateStr.split('-');
    return `${parseInt(parts[2], 10)} ${MONTHS[parseInt(parts[1], 10) - 1]} ${parts[0]}`;
  }
}