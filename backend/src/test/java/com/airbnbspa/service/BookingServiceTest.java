package com.airbnbspa.service;

import com.airbnbspa.dto.BookingRequestDTO;
import com.airbnbspa.entity.AvailabilityBlock;
import com.airbnbspa.entity.Booking;
import com.airbnbspa.entity.User;
import com.airbnbspa.enums.BookingStatus;
import com.airbnbspa.enums.BookingType;
import com.airbnbspa.enums.Role;
import com.airbnbspa.exception.BookingConflictException;
import com.airbnbspa.repository.AvailabilityBlockRepository;
import com.airbnbspa.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityBlockRepository availabilityBlockRepository;

    @Mock
    private PriceCalculationService priceCalculationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private BookingRequestDTO validRequest;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        validRequest = BookingRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("0612345678")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .numberOfGuests(2)
                .bookingType(BookingType.NIGHT_STAY)
                .message("Test booking")
                .agreedToRules(true)
                .build();

        testBooking = Booking.builder()
                .id(10L)
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("0612345678")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .numberOfGuests(2)
                .bookingType(BookingType.NIGHT_STAY)
                .status(BookingStatus.PENDING)
                .totalPrice(new BigDecimal("240.00"))
                .build();
    }

    @Test
    @DisplayName("Should create booking successfully")
    void createBookingSuccess() {
        when(bookingRepository.findOverlappingBookings(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(availabilityBlockRepository.findOverlapping(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(priceCalculationService.calculatePrice(any(LocalDate.class), any(LocalDate.class), any(BookingType.class)))
                .thenReturn(new BigDecimal("240.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = bookingService.createBooking(validRequest, user);

        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo(user.getUsername());
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("240.00"));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BookingConflictException when dates overlap existing confirmed booking")
    void createBookingWithDateConflict() {
        when(bookingRepository.findOverlappingBookings(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(testBooking));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, user))
                .isInstanceOf(BookingConflictException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when dates are in the past")
    void createBookingWithPastDates() {
        BookingRequestDTO pastRequest = BookingRequestDTO.builder()
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(3))
                .bookingType(BookingType.NIGHT_STAY)
                .agreedToRules(true)
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(pastRequest, user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should cancel booking when called by the owner")
    void cancelBookingAsOwnerSuccess() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(10L, user);

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when non-owner tries to cancel")
    void cancelBookingByWrongUser() {
        User otherUser = User.builder()
                .id(99L)
                .username("other")
                .role(Role.ROLE_USER)
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, otherUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Should confirm booking when called by admin")
    void confirmBookingAsAdmin() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = bookingService.confirmBooking(10L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should calculate correct booking price")
    void bookingPriceCalculation() {
        when(bookingRepository.findOverlappingBookings(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(availabilityBlockRepository.findOverlapping(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(priceCalculationService.calculatePrice(any(LocalDate.class), any(LocalDate.class), any(BookingType.class)))
                .thenReturn(new BigDecimal("240.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = bookingService.createBooking(validRequest, user);

        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("240.00"));
        verify(priceCalculationService, times(1))
                .calculatePrice(any(LocalDate.class), any(LocalDate.class), any(BookingType.class));
    }
}