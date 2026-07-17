export interface AvailabilityBlock {
  id: number;
  startDate: string;
  endDate: string;
  reason: string;
  createdAt: string;
}

export interface CalendarDay {
  date: string;
  available: boolean;
  booked: boolean;
  blocked: boolean;
  status: 'available' | 'booked' | 'blocked' | 'past';
  price?: number;
}