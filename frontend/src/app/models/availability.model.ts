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

export interface CalendarFeed {
  id: number;
  name: string;
  url: string;
  source: string;
  enabled: boolean;
  lastSyncedAt?: string;
  lastSyncStatus?: string;
  lastSyncMessage?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CalendarFeedRequest {
  name: string;
  url: string;
  source: string;
  enabled?: boolean;
}

export interface CalendarSyncResult {
  feedId: number;
  feedName: string;
  source: string;
  status: string;
  message: string;
  importResult?: IcalImportResult;
}

export interface CalendarDay {
  date: string;
  available: boolean;
  booked: boolean;
  blocked: boolean;
  status: 'available' | 'booked' | 'blocked' | 'past';
  price?: number;
}