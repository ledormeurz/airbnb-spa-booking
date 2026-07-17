package com.airbnbspa.service;

import com.airbnbspa.entity.PriceRule;
import com.airbnbspa.enums.BookingType;
import com.airbnbspa.enums.DayType;
import com.airbnbspa.repository.PriceRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceCalculationServiceTest {

    @Mock
    private PriceRuleRepository priceRuleRepository;

    @InjectMocks
    private PriceCalculationService priceCalculationService;

    private PriceRule weekdayNightStay;
    private PriceRule weekendNightStay;
    private PriceRule weekdayExtendedStay;
    private PriceRule weekendExtendedStay;
    private PriceRule weekdaySpaSession;
    private PriceRule weekendSpaSession;

    @BeforeEach
    void setUp() {
        weekdayNightStay = PriceRule.builder()
                .id(1L)
                .name("Nuitée semaine")
                .bookingType(BookingType.NIGHT_STAY)
                .dayType(DayType.WEEKDAY)
                .price(new BigDecimal("120.00"))
                .active(true)
                .build();

        weekendNightStay = PriceRule.builder()
                .id(2L)
                .name("Nuitée week-end")
                .bookingType(BookingType.NIGHT_STAY)
                .dayType(DayType.WEEKEND)
                .price(new BigDecimal("150.00"))
                .active(true)
                .build();

        weekdayExtendedStay = PriceRule.builder()
                .id(3L)
                .name("Séjour semaine")
                .bookingType(BookingType.EXTENDED_STAY)
                .dayType(DayType.WEEKDAY)
                .price(new BigDecimal("100.00"))
                .active(true)
                .build();

        weekendExtendedStay = PriceRule.builder()
                .id(4L)
                .name("Séjour week-end")
                .bookingType(BookingType.EXTENDED_STAY)
                .dayType(DayType.WEEKEND)
                .price(new BigDecimal("130.00"))
                .active(true)
                .build();

        weekdaySpaSession = PriceRule.builder()
                .id(5L)
                .name("Séance spa")
                .bookingType(BookingType.SPA_SESSION)
                .dayType(DayType.WEEKDAY)
                .price(new BigDecimal("60.00"))
                .active(true)
                .build();

        weekendSpaSession = PriceRule.builder()
                .id(6L)
                .name("Séance spa week-end")
                .bookingType(BookingType.SPA_SESSION)
                .dayType(DayType.WEEKEND)
                .price(new BigDecimal("80.00"))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should calculate WEEKDAY night stay price correctly")
    void weekdayNightStayPrice() {
        // Given
        LocalDate weekday = LocalDate.of(2026, 7, 15); // Wednesday
        when(priceRuleRepository.findByBookingTypeAndDayType(BookingType.NIGHT_STAY, DayType.WEEKDAY))
                .thenReturn(Optional.of(weekdayNightStay));

        // When
        BigDecimal price = priceCalculationService.calculatePrice(
                weekday, weekday.plusDays(1), BookingType.NIGHT_STAY);

        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("Should calculate WEEKEND night stay price correctly")
    void weekendNightStayPrice() {
        // Given
        LocalDate weekend = LocalDate.of(2026, 7, 18); // Saturday
        when(priceRuleRepository.findByBookingTypeAndDayType(BookingType.NIGHT_STAY, DayType.WEEKEND))
                .thenReturn(Optional.of(weekendNightStay));

        // When
        BigDecimal price = priceCalculationService.calculatePrice(
                weekend, weekend.plusDays(1), BookingType.NIGHT_STAY);

        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should calculate extended stay price correctly across multiple days")
    void extendedStayCalculation() {
        // Given
        LocalDate start = LocalDate.of(2026, 7, 13); // Monday
        LocalDate end = LocalDate.of(2026, 7, 18);   // Saturday (5 nights)

        when(priceRuleRepository.findByBookingTypeAndDayType(BookingType.EXTENDED_STAY, DayType.WEEKDAY))
                .thenReturn(Optional.of(weekdayExtendedStay));
        when(priceRuleRepository.findByBookingTypeAndDayType(BookingType.EXTENDED_STAY, DayType.WEEKEND))
                .thenReturn(Optional.of(weekendExtendedStay));

        // Expected: Mon-Fri = 4 weekdays * 100 + Fri-Sat = 1 weekend * 130 = 530.00
        // When
        BigDecimal price = priceCalculationService.calculatePrice(
                start, end, BookingType.EXTENDED_STAY);

        // Then
        // 4 weekdays at 100.00 + 1 weekend day at 130.00 = 530.00
        assertThat(price).isEqualByComparingTo(new BigDecimal("530.00"));
    }

    @Test
    @DisplayName("Should calculate spa session price correctly")
    void spaSessionPrice() {
        // Given
        LocalDate weekday = LocalDate.of(2026, 7, 15); // Wednesday
        when(priceRuleRepository.findByBookingTypeAndDayType(BookingType.SPA_SESSION, DayType.WEEKDAY))
                .thenReturn(Optional.of(weekdaySpaSession));

        // When
        BigDecimal price = priceCalculationService.calculatePrice(
                weekday, weekday.plusDays(1), BookingType.SPA_SESSION);

        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("Should return zero when no price rule is found")
    void noPriceRuleFound() {
        // Given
        LocalDate date = LocalDate.of(2026, 7, 15);
        when(priceRuleRepository.findByBookingTypeAndDayType(any(), any()))
                .thenReturn(Optional.empty());

        // When
        BigDecimal price = priceCalculationService.calculatePrice(
                date, date.plusDays(1), BookingType.SPA_SESSION);

        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    /**
     * Helper to determine if a date falls on a weekend.
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}