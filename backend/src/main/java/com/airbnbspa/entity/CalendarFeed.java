package com.airbnbspa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_feeds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000, unique = true)
    private String url;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus;

    @Column(name = "last_sync_message", length = 500)
    private String lastSyncMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
