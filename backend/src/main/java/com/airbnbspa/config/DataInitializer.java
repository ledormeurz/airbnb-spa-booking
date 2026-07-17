package com.airbnbspa.config;

import com.airbnbspa.entity.*;
import com.airbnbspa.enums.*;
import com.airbnbspa.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
public class DataInitializer {

    private final UserRepository userRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final EquipmentRepository equipmentRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-demo-data:false}")
    private boolean initDemoData;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           PriceRuleRepository priceRuleRepository,
                           EquipmentRepository equipmentRepository,
                           AvailabilityBlockRepository availabilityBlockRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.equipmentRepository = equipmentRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!initDemoData) {
            log.info("Demo data initialization is disabled (app.init-demo-data=false)");
            return;
        }

        log.info("Initializing demo data...");

        // Create admin user
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@airbnbspa.com")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: {}", adminUsername);
        }

        // Create demo normal users
        if (!userRepository.existsByUsername("john_doe")) {
            User john = User.builder()
                    .username("john_doe")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build();
            userRepository.save(john);
            log.info("Created demo user: john_doe");
        }

        if (!userRepository.existsByUsername("jane_smith")) {
            User jane = User.builder()
                    .username("jane_smith")
                    .passwordHash(passwordEncoder.encode("password456"))
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build();
            userRepository.save(jane);
            log.info("Created demo user: jane_smith");
        }

        // Seed price rules
        if (priceRuleRepository.count() == 0) {
            priceRuleRepository.save(PriceRule.builder()
                    .name("Night Stay Weekday")
                    .bookingType(BookingType.NIGHT_STAY)
                    .dayType(DayType.WEEKDAY)
                    .price(new BigDecimal("150.00"))
                    .active(true)
                    .build());

            priceRuleRepository.save(PriceRule.builder()
                    .name("Night Stay Weekend")
                    .bookingType(BookingType.NIGHT_STAY)
                    .dayType(DayType.WEEKEND)
                    .price(new BigDecimal("200.00"))
                    .active(true)
                    .build());

            priceRuleRepository.save(PriceRule.builder()
                    .name("Extended Stay Weekday")
                    .bookingType(BookingType.EXTENDED_STAY)
                    .dayType(DayType.WEEKDAY)
                    .price(new BigDecimal("120.00"))
                    .active(true)
                    .build());

            priceRuleRepository.save(PriceRule.builder()
                    .name("Extended Stay Weekend")
                    .bookingType(BookingType.EXTENDED_STAY)
                    .dayType(DayType.WEEKEND)
                    .price(new BigDecimal("170.00"))
                    .active(true)
                    .build());

            priceRuleRepository.save(PriceRule.builder()
                    .name("Spa Session Weekday")
                    .bookingType(BookingType.SPA_SESSION)
                    .dayType(DayType.WEEKDAY)
                    .price(new BigDecimal("80.00"))
                    .active(true)
                    .build());

            priceRuleRepository.save(PriceRule.builder()
                    .name("Spa Session Weekend")
                    .bookingType(BookingType.SPA_SESSION)
                    .dayType(DayType.WEEKEND)
                    .price(new BigDecimal("100.00"))
                    .active(true)
                    .build());

            log.info("Seeded 6 price rules");
        }

        // Seed equipment
        if (equipmentRepository.count() == 0) {
            equipmentRepository.save(Equipment.builder()
                    .name("Sauna")
                    .description("Traditional Finnish sauna with cedar wood interior")
                    .icon("sauna")
                    .active(true)
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .name("Jacuzzi")
                    .description("Outdoor jacuzzi with hydrotherapy jets")
                    .icon("hot-tub")
                    .active(true)
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .name("Massage Table")
                    .description("Professional massage table for in-room treatments")
                    .icon("spa")
                    .active(true)
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .name("Steam Room")
                    .description("Aromatic steam room with eucalyptus infusion")
                    .icon("steam")
                    .active(true)
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .name("Yoga Mats")
                    .description("Premium yoga mats and accessories")
                    .icon("yoga")
                    .active(true)
                    .build());

            log.info("Seeded 5 equipment items");
        }

        // Seed availability blocks (maintenance periods)
        if (availabilityBlockRepository.count() == 0) {
            LocalDate today = LocalDate.now();
            availabilityBlockRepository.save(AvailabilityBlock.builder()
                    .startDate(today.plusMonths(3).withDayOfMonth(1))
                    .endDate(today.plusMonths(3).withDayOfMonth(5))
                    .reason("Annual maintenance")
                    .build());

            log.info("Seeded availability blocks");
        }

        log.info("Demo data initialization completed");
    }
}