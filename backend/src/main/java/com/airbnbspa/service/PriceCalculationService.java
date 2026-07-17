package com.airbnbspa.service;

import com.airbnbspa.entity.PriceRule;
import com.airbnbspa.enums.BookingType;
import com.airbnbspa.enums.DayType;
import com.airbnbspa.repository.PriceRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class PriceCalculationService {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100.00");

    private final PriceRuleRepository priceRuleRepository;

    public PriceCalculationService(PriceRuleRepository priceRuleRepository) {
        this.priceRuleRepository = priceRuleRepository;
    }

    public List<PriceRule> getAllActive() {
        return priceRuleRepository.findByActiveTrue();
    }

    public BigDecimal calculatePrice(LocalDate startDate, LocalDate endDate, BookingType bookingType) {
        if (startDate == null || endDate == null || bookingType == null) {
            throw new IllegalArgumentException("Start date, end date, and booking type must not be null");
        }

        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // For SPA_SESSION: single price, not per-night
        if (bookingType == BookingType.SPA_SESSION) {
            DayType dayType = classifyDay(startDate);
            Optional<PriceRule> rule = priceRuleRepository.findByBookingTypeAndDayType(bookingType, dayType);
            return rule.map(PriceRule::getPrice).orElse(DEFAULT_PRICE);
        }

        // For NIGHT_STAY and EXTENDED_STAY: per-night pricing
        long numberOfNights = ChronoUnit.DAYS.between(startDate, endDate);
        if (numberOfNights <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < numberOfNights; i++) {
            LocalDate nightDate = startDate.plusDays(i);
            DayType dayType = classifyDay(nightDate);

            Optional<PriceRule> rule = priceRuleRepository.findByBookingTypeAndDayType(bookingType, dayType);
            BigDecimal nightPrice = rule.map(PriceRule::getPrice).orElse(DEFAULT_PRICE);
            totalPrice = totalPrice.add(nightPrice);
        }

        return totalPrice;
    }

    private DayType classifyDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }
}