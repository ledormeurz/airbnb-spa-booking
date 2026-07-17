import { Booking } from './booking.model';

export interface Dashboard {
  pendingBookings: number;
  confirmedBookings: number;
  estimatedRevenue: number;
  occupancyRate: number;
  upcomingArrivals: Booking[];
  upcomingDepartures: Booking[];
}