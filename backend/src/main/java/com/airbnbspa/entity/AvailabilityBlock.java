package com.airbnbspa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability_blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 255)
    private String reason;

    /**
     * UID iCalendar externe (ex. événement Airbnb). Unique quand présent.
     */
    @Column(name = "external_uid", length = 255, unique = true)
    private String externalUid;

    /**
     * Origine du bloc : MANUAL, AIRBNB, BOOKING, ICAL, etc.
     */
    @Column(length = 50)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
