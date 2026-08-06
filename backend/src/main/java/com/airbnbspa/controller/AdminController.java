package com.airbnbspa.controller;

import com.airbnbspa.dto.*;
import com.airbnbspa.entity.PriceRule;
import com.airbnbspa.enums.Role;
import com.airbnbspa.repository.PriceRuleRepository;
import com.airbnbspa.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookingService bookingService;
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final AvailabilityService availabilityService;
    private final PriceCalculationService priceCalculationService;
    private final PriceRuleRepository priceRuleRepository;
    private final IcalImportService icalImportService;

    public AdminController(BookingService bookingService,
                           UserService userService,
                           EquipmentService equipmentService,
                           AvailabilityService availabilityService,
                           PriceCalculationService priceCalculationService,
                           PriceRuleRepository priceRuleRepository,
                           IcalImportService icalImportService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.equipmentService = equipmentService;
        this.availabilityService = availabilityService;
        this.priceCalculationService = priceCalculationService;
        this.priceRuleRepository = priceRuleRepository;
        this.icalImportService = icalImportService;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(bookingService.getDashboardStats());
    }

    // ==================== BOOKINGS ====================

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDTO>> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bookingType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String clientName) {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings(
                status, bookingType, dateFrom, dateTo, clientName);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingDetail(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }

        BookingResponseDTO result;
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                result = bookingService.confirmBooking(id);
                break;
            case "REJECTED":
                String reason = body.get("rejectionReason");
                result = bookingService.rejectBooking(id, reason);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    // ==================== CALENDAR ====================

    @GetMapping("/calendar")
    public ResponseEntity<Map<String, Object>> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(availabilityService.getAvailability(startDate, endDate));
    }

    // ==================== AVAILABILITY BLOCKS ====================

    @PostMapping("/availability-blocks")
    public ResponseEntity<AvailabilityBlockDTO> createAvailabilityBlock(
            @RequestBody AvailabilityBlockDTO blockDTO) {
        AvailabilityBlockDTO created = availabilityService.createBlockDTO(blockDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/availability-blocks/{id}")
    public ResponseEntity<Void> deleteAvailabilityBlock(@PathVariable Long id) {
        availabilityService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability-blocks")
    public ResponseEntity<List<AvailabilityBlockDTO>> getAvailabilityBlocks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        if (dateFrom != null && dateTo != null) {
            return ResponseEntity.ok(availabilityService.getBlocksInRangeDTO(dateFrom, dateTo));
        }
        return ResponseEntity.ok(availabilityService.getAllBlockDTOs());
    }

    @PostMapping(value = "/availability-blocks/import-ics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IcalImportResultDTO> importAvailabilityIcs(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ICAL") String source) {
        IcalImportResultDTO result = icalImportService.importFromFile(file, source);
        return ResponseEntity.ok(result);
    }

    // ==================== PRICE RULES ====================

    @GetMapping("/prices")
    public ResponseEntity<List<PriceRuleDTO>> getPrices() {
        List<PriceRule> rules = priceRuleRepository.findAll();
        List<PriceRuleDTO> dtos = rules.stream()
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

    @PutMapping("/prices/{id}")
    public ResponseEntity<PriceRuleDTO> updatePrice(
            @PathVariable Long id,
            @RequestBody PriceRuleDTO dto) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price rule not found with id: " + id));

        if (dto.getPrice() != null) {
            rule.setPrice(dto.getPrice());
        }
        if (dto.getName() != null) {
            rule.setName(dto.getName());
        }
        rule.setActive(dto.isActive());

        rule = priceRuleRepository.save(rule);

        PriceRuleDTO result = PriceRuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .bookingType(rule.getBookingType())
                .dayType(rule.getDayType())
                .price(rule.getPrice())
                .active(rule.isActive())
                .build();

        return ResponseEntity.ok(result);
    }

    // ==================== EQUIPMENT ====================

    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentDTO>> getEquipment() {
        return ResponseEntity.ok(equipmentService.getAllDTOs());
    }

    @PostMapping("/equipment")
    public ResponseEntity<EquipmentDTO> createEquipment(@RequestBody EquipmentDTO equipmentDTO) {
        EquipmentDTO created = equipmentService.createDTO(equipmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(
            @PathVariable Long id,
            @RequestBody EquipmentDTO equipmentDTO) {
        EquipmentDTO updated = equipmentService.updateDTO(id, equipmentDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String roleStr = body.get("role");
        boolean enabled = body.get("enabled") == null || Boolean.parseBoolean(body.get("enabled"));

        if (username == null || password == null || email == null) {
            return ResponseEntity.badRequest().build();
        }

        Role role = roleStr != null ? Role.valueOf(roleStr) : Role.ROLE_USER;

        UserDTO created = userService.createUser(username, password, firstName, lastName, email, role, enabled);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String role = (String) body.get("role");
        Boolean enabled = body.get("enabled") != null ? Boolean.valueOf(body.get("enabled").toString()) : null;
        String firstName = (String) body.get("firstName");
        String lastName = (String) body.get("lastName");
        String email = (String) body.get("email");

        UserDTO updated = userService.updateUser(id, role, enabled, firstName, lastName, email);
        return ResponseEntity.ok(updated);
    }
}