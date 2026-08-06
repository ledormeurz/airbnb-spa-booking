package com.airbnbspa.service;

import com.airbnbspa.dto.AvailabilityBlockDTO;
import com.airbnbspa.entity.AvailabilityBlock;
import com.airbnbspa.entity.Booking;
import com.airbnbspa.enums.BookingStatus;
import com.airbnbspa.repository.AvailabilityBlockRepository;
import com.airbnbspa.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(AvailabilityBlockRepository availabilityBlockRepository,
                               BookingRepository bookingRepository) {
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAvailability(LocalDate start, LocalDate end) {
        if (start == null) {
            start = LocalDate.now();
        }
        if (end == null) {
            end = start.plusMonths(3);
        }

        Set<LocalDate> bookedDates = new HashSet<>();
        Set<LocalDate> blockedDates = new HashSet<>();
        List<LocalDate> availableDates = new ArrayList<>();

        // Get availability blocks in range
        List<AvailabilityBlock> blocks = availabilityBlockRepository
                .findOverlapping(start, end);

        // Get confirmed bookings in range
        List<Booking> confirmedBookings = bookingRepository
                .findOverlappingBookings(start, end);

        // Mark blocked dates
        for (AvailabilityBlock block : blocks) {
            LocalDate current = block.getStartDate();
            while (!current.isAfter(block.getEndDate())) {
                if (!current.isBefore(start) && !current.isAfter(end)) {
                    blockedDates.add(current);
                }
                current = current.plusDays(1);
            }
        }

        // Mark booked dates
        for (Booking booking : confirmedBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                LocalDate current = booking.getStartDate();
                while (!current.isAfter(booking.getEndDate())) {
                    if (!current.isBefore(start) && !current.isAfter(end)) {
                        bookedDates.add(current);
                    }
                    current = current.plusDays(1);
                }
            }
        }

        // Build available dates
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (!bookedDates.contains(current) && !blockedDates.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("availableDates", availableDates);
        result.put("bookedDates", new ArrayList<>(bookedDates));
        result.put("blockedDates", new ArrayList<>(blockedDates));
        result.put("totalDays", ChronoUnit.DAYS.between(start, end));
        result.put("bookedDays", bookedDates.size());
        result.put("blockedDays", blockedDates.size());
        result.put("availableDays", availableDates.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlock> getOverlappingBlocks(LocalDate start, LocalDate end) {
        return availabilityBlockRepository.findOverlapping(start, end);
    }

    public AvailabilityBlock createBlock(AvailabilityBlockDTO dto) {
        // Check for overlap with existing blocks
        List<AvailabilityBlock> overlapping = availabilityBlockRepository
                .findOverlapping(dto.getStartDate(), dto.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Date range overlaps with an existing availability block");
        }

        AvailabilityBlock block = AvailabilityBlock.builder()
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .source(dto.getSource() != null && !dto.getSource().isBlank() ? dto.getSource() : "MANUAL")
                .externalUid(dto.getExternalUid())
                .build();

        return availabilityBlockRepository.save(block);
    }

    public void deleteBlock(Long id) {
        if (!availabilityBlockRepository.existsById(id)) {
            throw new EntityNotFoundException("Availability block not found with id: " + id);
        }
        availabilityBlockRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlock> getAllBlocks() {
        return availabilityBlockRepository.findAll();
    }

    // DTO convenience methods for controllers
    public AvailabilityBlockDTO createBlockDTO(AvailabilityBlockDTO dto) {
        AvailabilityBlock block = createBlock(dto);
        return toDTO(block);
    }

    public List<AvailabilityBlockDTO> getAllBlockDTOs() {
        return availabilityBlockRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AvailabilityBlockDTO> getBlocksInRangeDTO(LocalDate start, LocalDate end) {
        return availabilityBlockRepository
                .findOverlapping(start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AvailabilityBlockDTO toDTO(AvailabilityBlock block) {
        return AvailabilityBlockDTO.builder()
                .id(block.getId())
                .startDate(block.getStartDate())
                .endDate(block.getEndDate())
                .reason(block.getReason())
                .externalUid(block.getExternalUid())
                .source(block.getSource())
                .createdAt(block.getCreatedAt())
                .build();
    }
}