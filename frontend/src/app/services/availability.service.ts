import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { CalendarDay } from '../models/availability.model';

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {

  constructor(private api: ApiService) {}

  getAvailableDates(month: number, year: number): Observable<CalendarDay[]> {
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const endDate = `${year}-${String(month).padStart(2, '0')}-31`;
    return this.api.getAvailability(startDate, endDate).pipe(
      map((data: any) => this.transformToCalendarDays(data, month, year))
    );
  }

  isDateAvailable(date: string): Observable<boolean> {
    return this.api.getAvailability(date, date).pipe(
      map((data: any) => {
        if (!data) return true;
        const bookedDates: string[] = data.bookedDates || [];
        const blockedDates: string[] = data.blockedDates || [];
        return !bookedDates.includes(date) && !blockedDates.includes(date);
      })
    );
  }

  private transformToCalendarDays(data: any, month: number, year: number): CalendarDay[] {
    const days: CalendarDay[] = [];
    const daysInMonth = new Date(year, month, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Backend returns Map with bookedDates and blockedDates arrays
    const bookedDates: string[] = data?.bookedDates || [];
    const blockedDates: string[] = data?.blockedDates || [];
    const bookedSet = new Set(bookedDates);
    const blockedSet = new Set(blockedDates);

    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dateObj = new Date(year, month - 1, day);
      const isPast = dateObj < today;
      const isBooked = bookedSet.has(dateStr);
      const isBlocked = blockedSet.has(dateStr);

      let status: 'available' | 'booked' | 'blocked' | 'past';
      if (isPast) {
        status = 'past';
      } else if (isBlocked) {
        status = 'blocked';
      } else if (isBooked) {
        status = 'booked';
      } else {
        status = 'available';
      }

      days.push({
        date: dateStr,
        available: status === 'available',
        booked: isBooked,
        blocked: isBlocked,
        status
      });
    }

    return days;
  }
}