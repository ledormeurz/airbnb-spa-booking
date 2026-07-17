package com.airbnbspa.repository;

import com.airbnbspa.entity.Booking;
import com.airbnbspa.entity.User;
import com.airbnbspa.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStartDateBetween(LocalDate start, LocalDate end);
    List<Booking> findByStatusNotAndStartDateBetween(BookingStatus status, LocalDate start, LocalDate end);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.startDate < :endDate AND b.endDate > :startDate")
    List<Booking> findOverlappingBookings(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByStatus(BookingStatus status);
    long countByStatusAndStartDateAfter(BookingStatus status, LocalDate date);

    List<Booking> findByStatusOrderByStartDateAsc(BookingStatus status);
    List<Booking> findByStatusAndStartDateBetweenOrderByStartDateAsc(BookingStatus status, LocalDate start, LocalDate end);
    List<Booking> findByStatusAndEndDateBetweenOrderByEndDateAsc(BookingStatus status, LocalDate start, LocalDate end);

    // Filter methods for admin
    List<Booking> findByStatusAndBookingType(BookingStatus status, com.airbnbspa.enums.BookingType bookingType);
    List<Booking> findByBookingType(com.airbnbspa.enums.BookingType bookingType);
    List<Booking> findByStartDateGreaterThanEqual(LocalDate startDate);
    List<Booking> findByEndDateLessThanEqual(LocalDate endDate);
    List<Booking> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDate start, LocalDate end);
    List<Booking> findByStatusAndStartDateGreaterThanEqual(BookingStatus status, LocalDate start);
    List<Booking> findByStatusAndEndDateLessThanEqual(BookingStatus status, LocalDate end);
    List<Booking> findByStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(BookingStatus status, LocalDate start, LocalDate end);
    List<Booking> findByStatusAndBookingTypeAndStartDateGreaterThanEqual(BookingStatus status, com.airbnbspa.enums.BookingType bookingType, LocalDate start);
    List<Booking> findByStatusAndBookingTypeAndEndDateLessThanEqual(BookingStatus status, com.airbnbspa.enums.BookingType bookingType, LocalDate end);
    List<Booking> findByStatusAndBookingTypeAndStartDateGreaterThanEqualAndEndDateLessThanEqual(BookingStatus status, com.airbnbspa.enums.BookingType bookingType, LocalDate start, LocalDate end);
    List<Booking> findByBookingTypeAndStartDateGreaterThanEqual(com.airbnbspa.enums.BookingType bookingType, LocalDate start);
    List<Booking> findByBookingTypeAndEndDateLessThanEqual(com.airbnbspa.enums.BookingType bookingType, LocalDate end);
    List<Booking> findByBookingTypeAndStartDateGreaterThanEqualAndEndDateLessThanEqual(com.airbnbspa.enums.BookingType bookingType, LocalDate start, LocalDate end);
}