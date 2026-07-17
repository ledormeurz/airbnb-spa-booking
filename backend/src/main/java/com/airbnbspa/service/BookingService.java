package com.airbnbspa.service;

import com.airbnbspa.dto.BookingRequestDTO;
import com.airbnbspa.dto.BookingResponseDTO;
import com.airbnbspa.dto.DashboardDTO;
import com.airbnbspa.entity.AvailabilityBlock;
import com.airbnbspa.entity.Booking;
import com.airbnbspa.entity.User;
import com.airbnbspa.enums.BookingStatus;
import com.airbnbspa.enums.BookingType;
import com.airbnbspa.exception.BookingConflictException;
import com.airbnbspa.repository.AvailabilityBlockRepository;
import com.airbnbspa.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final PriceCalculationService priceCalculationService;
    private final UserService userService;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilityBlockRepository availabilityBlockRepository,
                          PriceCalculationService priceCalculationService,
                          UserService userService) {
        this.bookingRepository = bookingRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.priceCalculationService = priceCalculationService;
        this.userService = userService;
    }

    /**
     * Create a new booking with PENDING status.
     */
    public BookingResponseDTO createBooking(BookingRequestDTO dto, User user) {
        // Validate dates
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check for conflicts
        checkBookingConflicts(dto.getStartDate(), dto.getEndDate());

        // Calculate price
        BigDecimal totalPrice = priceCalculationService.calculatePrice(
                dto.getStartDate(), dto.getEndDate(), dto.getBookingType());

        Booking booking = Booking.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .numberOfGuests(dto.getNumberOfGuests())
                .bookingType(dto.getBookingType())
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .message(dto.getMessage())
                .build();

        booking = bookingRepository.save(booking);
        return toDTO(booking);
    }

    /**
     * Get all bookings for a specific user.
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getUserBookings(Long userId) {
        User user = userService.findById(userId);
        return bookingRepository.findByUser(user).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a single booking by ID.
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = findBookingById(id);
        return toDTO(booking);
    }

    /**
     * Update a PENDING booking (only the owner can update).
     */
    public BookingResponseDTO updateBooking(Long id, BookingRequestDTO dto, User currentUser) {
        Booking booking = findBookingById(id);

        // Verify ownership
        if (!isOwner(booking, currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only update your own bookings");
        }

        // Only if status is PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be updated");
        }

        // Re-check conflicts with updated dates
        if (!dto.getStartDate().equals(booking.getStartDate()) || !dto.getEndDate().equals(booking.getEndDate())) {
            checkBookingConflicts(dto.getStartDate(), dto.getEndDate());
        }

        // Recalculate price
        BigDecimal totalPrice = priceCalculationService.calculatePrice(
                dto.getStartDate(), dto.getEndDate(), dto.getBookingType());

        booking.setFirstName(dto.getFirstName());
        booking.setLastName(dto.getLastName());
        booking.setEmail(dto.getEmail());
        booking.setPhone(dto.getPhone());
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setBookingType(dto.getBookingType());
        booking.setTotalPrice(totalPrice);
        booking.setMessage(dto.getMessage());

        booking = bookingRepository.save(booking);
        return toDTO(booking);
    }

    /**
     * Cancel a PENDING booking (only the owner can cancel).
     */
    public void cancelBooking(Long id, User currentUser) {
        Booking booking = findBookingById(id);

        // Verify ownership
        if (!isOwner(booking, currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only cancel your own bookings");
        }

        // Only if PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be cancelled");
        }

        // Set status to CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    /**
     * Admin: Get all bookings with filters.
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings(String status, String bookingType,
                                                    LocalDate dateFrom, LocalDate dateTo,
                                                    String clientName) {
        return getFilteredBookings(status, bookingType, dateFrom, dateTo, clientName);
    }

    /**
     * Admin: Get filtered bookings.
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getFilteredBookings(String status, String bookingType,
                                                          LocalDate dateFrom, LocalDate dateTo,
                                                          String clientName) {
        List<Booking> bookings;

        BookingStatus statusEnum = (status != null && !status.isEmpty())
                ? BookingStatus.valueOf(status.toUpperCase()) : null;
        BookingType typeEnum = (bookingType != null && !bookingType.isEmpty())
                ? BookingType.valueOf(bookingType.toUpperCase()) : null;

        if (statusEnum != null && typeEnum != null && dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByStatusAndBookingTypeAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    statusEnum, typeEnum, dateFrom, dateTo);
        } else if (statusEnum != null && typeEnum != null && dateFrom != null) {
            bookings = bookingRepository.findByStatusAndBookingTypeAndStartDateGreaterThanEqual(
                    statusEnum, typeEnum, dateFrom);
        } else if (statusEnum != null && typeEnum != null && dateTo != null) {
            bookings = bookingRepository.findByStatusAndBookingTypeAndEndDateLessThanEqual(
                    statusEnum, typeEnum, dateTo);
        } else if (statusEnum != null && dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    statusEnum, dateFrom, dateTo);
        } else if (statusEnum != null && dateFrom != null) {
            bookings = bookingRepository.findByStatusAndStartDateGreaterThanEqual(statusEnum, dateFrom);
        } else if (statusEnum != null && dateTo != null) {
            bookings = bookingRepository.findByStatusAndEndDateLessThanEqual(statusEnum, dateTo);
        } else if (typeEnum != null && dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByBookingTypeAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    typeEnum, dateFrom, dateTo);
        } else if (typeEnum != null && dateFrom != null) {
            bookings = bookingRepository.findByBookingTypeAndStartDateGreaterThanEqual(typeEnum, dateFrom);
        } else if (typeEnum != null && dateTo != null) {
            bookings = bookingRepository.findByBookingTypeAndEndDateLessThanEqual(typeEnum, dateTo);
        } else if (statusEnum != null && typeEnum != null) {
            bookings = bookingRepository.findByStatusAndBookingType(statusEnum, typeEnum);
        } else if (statusEnum != null) {
            bookings = bookingRepository.findByStatus(statusEnum);
        } else if (typeEnum != null) {
            bookings = bookingRepository.findByBookingType(typeEnum);
        } else if (dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(dateFrom, dateTo);
        } else if (dateFrom != null) {
            bookings = bookingRepository.findByStartDateGreaterThanEqual(dateFrom);
        } else if (dateTo != null) {
            bookings = bookingRepository.findByEndDateLessThanEqual(dateTo);
        } else {
            bookings = bookingRepository.findAll();
        }

        // Filter by client name if provided
        if (clientName != null && !clientName.trim().isEmpty()) {
            String search = clientName.trim().toLowerCase();
            bookings = bookings.stream()
                    .filter(b -> (b.getFirstName() != null && b.getFirstName().toLowerCase().contains(search))
                            || (b.getLastName() != null && b.getLastName().toLowerCase().contains(search))
                            || (b.getEmail() != null && b.getEmail().toLowerCase().contains(search)))
                    .collect(Collectors.toList());
        }

        return bookings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Confirm a booking.
     */
    public BookingResponseDTO confirmBooking(Long id) {
        Booking booking = findBookingById(id);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        return toDTO(booking);
    }

    /**
     * Admin: Reject a booking.
     */
    public BookingResponseDTO rejectBooking(Long id, String reason) {
        Booking booking = findBookingById(id);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);
        booking = bookingRepository.save(booking);
        return toDTO(booking);
    }

    /**
     * Admin: Get upcoming arrivals (CONFIRMED, startDate >= today, ordered by startDate).
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getUpcomingArrivals() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        List<Booking> bookings = bookingRepository.findByStatusAndStartDateBetweenOrderByStartDateAsc(
                BookingStatus.CONFIRMED, today, weekFromNow);
        return bookings.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Admin: Get upcoming departures (CONFIRMED, endDate >= today, ordered by endDate).
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getUpcomingDepartures() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        List<Booking> bookings = bookingRepository.findByStatusAndEndDateBetweenOrderByEndDateAsc(
                BookingStatus.CONFIRMED, today, weekFromNow);
        return bookings.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Admin: Get dashboard statistics.
     */
    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        long pendingCount = bookingRepository.countByStatus(BookingStatus.PENDING);
        long confirmedCount = bookingRepository.countByStatus(BookingStatus.CONFIRMED);

        // Calculate estimated revenue from CONFIRMED bookings
        List<Booking> confirmedBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED);
        BigDecimal estimatedRevenue = confirmedBookings.stream()
                .map(b -> b.getTotalPrice() != null ? b.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate occupancy rate (percentage of days booked in next 30 days)
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        long totalDays = ChronoUnit.DAYS.between(today, thirtyDaysLater);
        List<Booking> upcomingBookings = bookingRepository.findByStatusAndStartDateBetweenOrderByStartDateAsc(
                BookingStatus.CONFIRMED, today, thirtyDaysLater);

        long bookedDays = 0;
        for (Booking b : upcomingBookings) {
            LocalDate effStart = b.getStartDate().isBefore(today) ? today : b.getStartDate();
            LocalDate effEnd = b.getEndDate().isAfter(thirtyDaysLater) ? thirtyDaysLater : b.getEndDate();
            if (effEnd.isAfter(effStart)) {
                bookedDays += ChronoUnit.DAYS.between(effStart, effEnd);
            }
        }
        double occupancyRate = totalDays > 0 ? (double) bookedDays / totalDays * 100.0 : 0.0;
        occupancyRate = Math.round(occupancyRate * 100.0) / 100.0;

        List<BookingResponseDTO> upcomingArrivals = getUpcomingArrivals();
        List<BookingResponseDTO> upcomingDepartures = getUpcomingDepartures();

        return DashboardDTO.builder()
                .pendingBookings(pendingCount)
                .confirmedBookings(confirmedCount)
                .estimatedRevenue(estimatedRevenue.setScale(2, RoundingMode.HALF_UP))
                .occupancyRate(occupancyRate)
                .upcomingArrivals(upcomingArrivals)
                .upcomingDepartures(upcomingDepartures)
                .build();
    }

    /**
     * Check for conflicts with existing CONFIRMED bookings and availability blocks.
     */
    private void checkBookingConflicts(LocalDate startDate, LocalDate endDate) {
        // Check overlapping CONFIRMED bookings
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(
                    "The selected dates conflict with an existing booking. Please choose different dates.");
        }

        // Check overlapping availability blocks
        List<AvailabilityBlock> blocks = availabilityBlockRepository.findOverlapping(startDate, endDate);
        if (!blocks.isEmpty()) {
            String reasons = blocks.stream()
                    .map(AvailabilityBlock::getReason)
                    .filter(r -> r != null && !r.isEmpty())
                    .collect(Collectors.joining(", "));
            throw new BookingConflictException(
                    "The selected dates are not available: "
                    + (reasons.isEmpty() ? "Blocked dates" : reasons));
        }
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }

    private boolean isOwner(Booking booking, User user) {
        if (booking.getUser() == null || user == null) {
            return false;
        }
        return booking.getUser().getId().equals(user.getId());
    }

    private BookingResponseDTO toDTO(Booking booking) {
        BookingResponseDTO.BookingResponseDTOBuilder builder = BookingResponseDTO.builder()
                .id(booking.getId())
                .firstName(booking.getFirstName())
                .lastName(booking.getLastName())
                .email(booking.getEmail())
                .phone(booking.getPhone())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .numberOfGuests(booking.getNumberOfGuests())
                .bookingType(booking.getBookingType())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .message(booking.getMessage())
                .rejectionReason(booking.getRejectionReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        if (booking.getUser() != null) {
            builder.userId(booking.getUser().getId());
            builder.userName(booking.getUser().getUsername());
        }

        return builder.build();
    }
}