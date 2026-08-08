import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { AvailabilityBlock } from '../../../models/availability.model';

@Component({
  selector: 'app-admin-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-calendar.component.html',
  styleUrl: './admin-calendar.component.css'
})
export class AdminCalendarComponent implements OnInit {
  private apiService = inject(ApiService);

  @ViewChild('icsFileInput') icsFileInput?: ElementRef<HTMLInputElement>;

  currentMonth: number;
  currentYear: number;
  monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
  dayHeaders = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  calendarDays: { date: string; day: number; status: string; isCurrentMonth: boolean }[] = [];
  bookedDates: Set<string> = new Set();
  blockedDates: Set<string> = new Set();

  blocks: AvailabilityBlock[] = [];
  loading = false;
  importing = false;
  errorMessage = '';
  successMessage = '';

  blockForm = {
    startDate: '',
    endDate: '',
    reason: ''
  };

  icsSource = 'AIRBNB';
  icsSources = ['AIRBNB', 'BOOKING', 'ICAL'];
  selectedIcsFile: File | null = null;

  constructor() {
    const now = new Date();
    this.currentMonth = now.getMonth() + 1;
    this.currentYear = now.getFullYear();
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.apiService.getCalendar().subscribe({
      next: (data: any) => {
        this.bookedDates = new Set<string>();
        this.blockedDates = new Set<string>();
        if (data) {
          // Backend returns Map with bookedDates and blockedDates as arrays
          if (data.bookedDates) {
            data.bookedDates.forEach((date: string) => this.bookedDates.add(date));
          }
          if (data.blockedDates) {
            data.blockedDates.forEach((date: string) => this.blockedDates.add(date));
          }
        }
        this.buildCalendar();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement du calendrier.';
      }
    });

    this.apiService.getAvailabilityBlocks().subscribe({
      next: (data) => {
        this.blocks = data;
      },
      error: () => {}
    });
  }

  buildCalendar(): void {
    const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1);
    const daysInMonth = new Date(this.currentYear, this.currentMonth, 0).getDate();
    const startDayOfWeek = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1; // Monday = 0

    this.calendarDays = [];

    // Previous month padding
    for (let i = 0; i < startDayOfWeek; i++) {
      this.calendarDays.push({ date: '', day: 0, status: 'empty', isCurrentMonth: false });
    }

    // Current month days
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      let status = 'available';
      if (this.blockedDates.has(dateStr)) status = 'blocked';
      else if (this.bookedDates.has(dateStr)) status = 'booked';

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const cellDate = new Date(this.currentYear, this.currentMonth - 1, day);
      if (cellDate < today) status = 'past';

      this.calendarDays.push({ date: dateStr, day, status, isCurrentMonth: true });
    }
  }

  prevMonth(): void {
    if (this.currentMonth === 1) {
      this.currentMonth = 12;
      this.currentYear--;
    } else {
      this.currentMonth--;
    }
    this.buildCalendar();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) {
      this.currentMonth = 1;
      this.currentYear++;
    } else {
      this.currentMonth++;
    }
    this.buildCalendar();
  }

  createBlock(): void {
    if (!this.blockForm.startDate || !this.blockForm.endDate) {
      this.errorMessage = 'Veuillez remplir les dates de début et de fin.';
      return;
    }

    this.errorMessage = '';
    this.apiService.createBlock({
      startDate: this.blockForm.startDate,
      endDate: this.blockForm.endDate,
      reason: this.blockForm.reason
    }).subscribe({
      next: () => {
        this.successMessage = 'Blocage ajouté avec succès.';
        this.blockForm = { startDate: '', endDate: '', reason: '' };
        this.loadData();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du blocage.';
      }
    });
  }

  onIcsFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedIcsFile = file;
    this.errorMessage = '';
  }

  importIcs(): void {
    if (!this.selectedIcsFile) {
      this.errorMessage = 'Veuillez choisir un fichier .ics.';
      return;
    }

    this.importing = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.apiService.importAvailabilityIcs(this.selectedIcsFile, this.icsSource).subscribe({
      next: (result) => {
        this.importing = false;
        this.successMessage =
          `Import ${result.source} : ${result.imported} créé(s), ` +
          `${result.updated} mis à jour, ${result.skipped} ignoré(s) ` +
          `(${result.totalEvents} événement(s)).`;
        this.selectedIcsFile = null;
        if (this.icsFileInput) {
          this.icsFileInput.nativeElement.value = '';
        }
        this.loadData();
      },
      error: (err) => {
        this.importing = false;
        this.errorMessage =
          err.error?.message || err.error?.error || 'Erreur lors de l\'import du calendrier.';
      }
    });
  }

  deleteBlock(id: number): void {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce blocage ?')) {
      this.apiService.deleteBlock(id).subscribe({
        next: () => {
          this.successMessage = 'Blocage supprimé avec succès.';
          this.loadData();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la suppression.';
        }
      });
    }
  }
}