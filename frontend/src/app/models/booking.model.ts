export interface Booking {
  id: number;
  userId: number;
  userName: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  startDate: string;
  endDate: string;
  numberOfGuests: number;
  bookingType: BookingType;
  status: BookingStatus;
  totalPrice: number;
  message: string;
  createdAt: string;
  updatedAt: string;
}

export enum BookingStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

export enum BookingType {
  NIGHT_STAY = 'NIGHT_STAY',
  EXTENDED_STAY = 'EXTENDED_STAY',
  SPA_SESSION = 'SPA_SESSION'
}

export interface BookingRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  startDate: string;
  endDate: string;
  numberOfGuests: number;
  bookingType: BookingType;
  message: string;
  agreedToRules: boolean;
}