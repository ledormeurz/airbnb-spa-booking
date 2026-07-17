import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking, BookingRequest } from '../models/booking.model';
import { PriceRule } from '../models/price-rule.model';
import { Equipment } from '../models/equipment.model';
import { AvailabilityBlock } from '../models/availability.model';
import { Dashboard } from '../models/dashboard.model';
import { PropertyInfo } from '../models/property-info.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  // ==================== PUBLIC ====================

  getProperty(): Observable<PropertyInfo> {
    return this.http.get<PropertyInfo>(`${this.apiUrl}/public/property`);
  }

  getEquipment(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/public/equipment`);
  }

  getPrices(): Observable<PriceRule[]> {
    return this.http.get<PriceRule[]>(`${this.apiUrl}/public/prices`);
  }

  getAvailability(startDate: string, endDate: string): Observable<any> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<any>(`${this.apiUrl}/public/availability`, { params });
  }

  createBooking(request: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/public/booking-requests`, request);
  }

  // ==================== USER ====================

  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/user/profile`);
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/user/profile`, data);
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/user/bookings`);
  }

  getBooking(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/user/bookings/${id}`);
  }

  updateBooking(id: number, data: Partial<Booking>): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/user/bookings/${id}`, data);
  }

  cancelBooking(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/user/bookings/${id}`);
  }

  // ==================== ADMIN ====================

  getDashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.apiUrl}/admin/dashboard`);
  }

  getAllBookings(filters?: { status?: string; bookingType?: string; startDate?: string; endDate?: string; clientName?: string }): Observable<Booking[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.status) params = params.set('status', filters.status);
      if (filters.bookingType) params = params.set('bookingType', filters.bookingType);
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
      if (filters.clientName) params = params.set('clientName', filters.clientName);
    }
    return this.http.get<Booking[]>(`${this.apiUrl}/admin/bookings`, { params });
  }

  getBookingDetail(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/admin/bookings/${id}`);
  }

  updateBookingStatus(id: number, status: string, reason?: string): Observable<Booking> {
    const body: any = { status };
    if (reason) body.rejectionReason = reason;
    return this.http.put<Booking>(`${this.apiUrl}/admin/bookings/${id}/status`, body);
  }

  getCalendar(startDate?: string, endDate?: string): Observable<any> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<any>(`${this.apiUrl}/admin/calendar`, { params });
  }

  getAvailabilityBlocks(): Observable<AvailabilityBlock[]> {
    return this.http.get<AvailabilityBlock[]>(`${this.apiUrl}/admin/availability-blocks`);
  }

  createBlock(block: Partial<AvailabilityBlock>): Observable<AvailabilityBlock> {
    return this.http.post<AvailabilityBlock>(`${this.apiUrl}/admin/availability-blocks`, block);
  }

  deleteBlock(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/availability-blocks/${id}`);
  }

  updatePrice(id: number, data: Partial<PriceRule>): Observable<PriceRule> {
    return this.http.put<PriceRule>(`${this.apiUrl}/admin/prices/${id}`, data);
  }

  getAllPrices(): Observable<PriceRule[]> {
    return this.http.get<PriceRule[]>(`${this.apiUrl}/admin/prices`);
  }

  getAllEquipment(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/admin/equipment`);
  }

  createEquipment(data: Partial<Equipment>): Observable<Equipment> {
    return this.http.post<Equipment>(`${this.apiUrl}/admin/equipment`, data);
  }

  updateEquipment(id: number, data: Partial<Equipment>): Observable<Equipment> {
    return this.http.put<Equipment>(`${this.apiUrl}/admin/equipment/${id}`, data);
  }

  deleteEquipment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/equipment/${id}`);
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users`);
  }

  createUser(data: Partial<User>): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/admin/users`, data);
  }

  updateUser(id: number, data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/admin/users/${id}`, data);
  }
}