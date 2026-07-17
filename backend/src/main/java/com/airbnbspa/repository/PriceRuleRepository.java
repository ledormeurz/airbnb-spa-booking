package com.airbnbspa.repository;

import com.airbnbspa.entity.PriceRule;
import com.airbnbspa.enums.BookingType;
import com.airbnbspa.enums.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {
    List<PriceRule> findByActiveTrue();
    Optional<PriceRule> findByBookingTypeAndDayType(BookingType bookingType, DayType dayType);
    List<PriceRule> findByBookingType(BookingType bookingType);
}