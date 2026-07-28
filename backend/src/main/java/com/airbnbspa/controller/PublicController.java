package com.airbnbspa.controller;

import com.airbnbspa.dto.*;
import com.airbnbspa.entity.PriceRule;
import com.airbnbspa.service.AvailabilityService;
import com.airbnbspa.service.BookingService;
import com.airbnbspa.service.EquipmentService;
import com.airbnbspa.service.PriceCalculationService;
import com.airbnbspa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final EquipmentService equipmentService;
    private final PriceCalculationService priceCalculationService;
    private final BookingService bookingService;
    private final AvailabilityService availabilityService;
    private final UserService userService;

    public PublicController(EquipmentService equipmentService,
                            PriceCalculationService priceCalculationService,
                            BookingService bookingService,
                            AvailabilityService availabilityService,
                            UserService userService) {
        this.equipmentService = equipmentService;
        this.priceCalculationService = priceCalculationService;
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
        this.userService = userService;
    }

    @GetMapping("/property")
    public ResponseEntity<PropertyInfoDTO> getPropertyInfo() {
        PropertyInfoDTO propertyInfo = PropertyInfoDTO.builder()
                .name("Le Nid Spa")
                .description("A luxurious spa retreat nestled in the heart of nature. " +
                        "Enjoy world-class spa facilities, breathtaking views, and exceptional comfort. " +
                        "Perfect for relaxation, wellness retreats, and romantic getaways.")
                .maxGuests(10)
                .bedrooms(5)
                .beds(8)
                .bathrooms(4)
                .amenities(List.of(
                        "Wi-Fi", "Parking", "Kitchen", "Washer", "Dryer",
                        "Air Conditioning", "Heating", "Pool", "Hot Tub",
                        "Sauna", "Steam Room", "Spa", "Gym", "Garden",
                        "Lake View", "Fireplace", "TV", "BBQ Grill"))
                .rules(List.of(
                        "Check-in: after 3:00 PM",
                        "Check-out: before 11:00 AM",
                        "No smoking inside the property",
                        "No parties or events without prior approval",
                        "Quiet hours from 10:00 PM to 7:00 AM",
                        "Spa facilities available from 8:00 AM to 9:00 PM",
                        "Pets allowed with prior approval and additional fee",
                        "Please respect our neighbors and the environment"))
                .checkInTime("3:00 PM")
                .checkOutTime("11:00 AM")
                .build();

        return ResponseEntity.ok(propertyInfo);
    }

    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentDTO>> getEquipment() {
        List<EquipmentDTO> equipment = equipmentService.getAllActiveDTOs();
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/prices")
    public ResponseEntity<List<PriceRuleDTO>> getPrices() {
        List<PriceRule> activeRules = priceCalculationService.getAllActive();
        List<PriceRuleDTO> dtos = activeRules.stream()
                .map(rule -> PriceRuleDTO.builder()
                        .id(rule.getId())
                        .name(rule.getName())
                        .bookingType(rule.getBookingType())
                        .dayType(rule.getDayType())
                        .price(rule.getPrice())
                        .active(rule.isActive())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> availability = availabilityService.getAvailability(startDate, endDate);
        return ResponseEntity.ok(availability);
    }

    @PostMapping("/booking-requests")
    public ResponseEntity<BookingResponseDTO> createBookingRequest(
            @Valid @RequestBody BookingRequestDTO requestDTO) {
        // Anonymous booking - user is null
        BookingResponseDTO response = bookingService.createBooking(requestDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserDTO created = userService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}