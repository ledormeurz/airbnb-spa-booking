import { Routes } from '@angular/router';
import { HomeComponent } from './pages/public/home/home.component';
import { PropertyComponent } from './pages/public/property/property.component';
import { SpaComponent } from './pages/public/spa/spa.component';
import { GalleryComponent } from './pages/public/gallery/gallery.component';
import { PricesComponent } from './pages/public/prices/prices.component';
import { AvailabilityComponent } from './pages/public/availability/availability.component';
import { BookingFormComponent } from './pages/public/booking-form/booking-form.component';
import { LoginComponent } from './pages/user/login/login.component';
import { ProfileComponent } from './pages/user/profile/profile.component';
import { MyBookingsComponent } from './pages/user/my-bookings/my-bookings.component';
import { BookingDetailComponent } from './pages/user/my-bookings/booking-detail.component';
import { DashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { AdminBookingsComponent } from './pages/admin/bookings/admin-bookings.component';
import { AdminCalendarComponent } from './pages/admin/calendar/admin-calendar.component';
import { AdminPricesComponent } from './pages/admin/prices/admin-prices.component';
import { AdminEquipmentComponent } from './pages/admin/equipment/admin-equipment.component';
import { AdminUsersComponent } from './pages/admin/users/admin-users.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'property', component: PropertyComponent },
  { path: 'spa', component: SpaComponent },
  { path: 'gallery', component: GalleryComponent },
  { path: 'prices', component: PricesComponent },
  { path: 'availability', component: AvailabilityComponent },
  { path: 'booking', component: BookingFormComponent },
  { path: 'login', component: LoginComponent },
  { path: 'user/profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'user/bookings', component: MyBookingsComponent, canActivate: [AuthGuard] },
  { path: 'user/bookings/:id', component: BookingDetailComponent, canActivate: [AuthGuard] },
  { path: 'admin/dashboard', component: DashboardComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/bookings', component: AdminBookingsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/calendar', component: AdminCalendarComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/prices', component: AdminPricesComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/equipment', component: AdminEquipmentComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: '**', redirectTo: '' }
];