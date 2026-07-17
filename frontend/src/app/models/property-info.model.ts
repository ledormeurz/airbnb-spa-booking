export interface PropertyInfo {
  name: string;
  description: string;
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;
  amenities: string[];
  rules: string[];
  checkInTime: string;
  checkOutTime: string;
}