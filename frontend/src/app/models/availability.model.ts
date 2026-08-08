export interface AvailabilityBlock {
  id: number;
  startDate: string;
  endDate: string;
  reason: string;
  externalUid?: string;
  source?: string;
  createdAt: string;
}

export interface IcalImportResult {
  totalEvents: number;
  imported: number;
  updated: number;
  skipped: number;
  source: string;
}

export interface CalendarDay {
  date: string;
  available: boolean;
  booked: boolean;
  blocked: boolean;
  status: 'available' | 'booked' | 'blocked' | 'past';
  price?: number;
}