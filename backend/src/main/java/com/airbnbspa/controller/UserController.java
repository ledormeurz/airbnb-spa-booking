package com.airbnbspa.controller;

import com.airbnbspa.dto.BookingRequestDTO;
import com.airbnbspa.dto.BookingResponseDTO;
import com.airbnbspa.dto.UserDTO;
import com.airbnbspa.entity.User;
import com.airbnbspa.service.BookingService;
import com.airbnbspa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            Authentication authentication,
            @RequestBody UserDTO userDTO) {
        User user = getCurrentUser(authentication);
        UserDTO updated = userService.updateUser(user.getId(), userDTO, false);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        BookingResponseDTO booking = bookingService.getBookingById(id);

        // Verify ownership
        if (booking.getUserId() != null && !booking.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // If booking has no user (anonymous), check email match
        if (booking.getUserId() == null) {
            if (!booking.getEmail().equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(booking);
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequestDTO requestDTO,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        BookingResponseDTO updated = bookingService.updateBooking(id, requestDTO, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        bookingService.cancelBooking(id, user);
        return ResponseEntity.noContent().build();
    }
}