package com.airbnbspa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long pendingBookings;
    private long confirmedBookings;
    private BigDecimal estimatedRevenue;
    private double occupancyRate;
    private List<BookingResponseDTO> upcomingArrivals;
    private List<BookingResponseDTO> upcomingDepartures;
}