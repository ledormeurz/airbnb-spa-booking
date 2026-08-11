package com.airbnbspa.config;

import com.airbnbspa.service.CalendarSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CalendarSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(CalendarSyncScheduler.class);

    private final CalendarSyncService calendarSyncService;
    private final boolean enabled;

    public CalendarSyncScheduler(CalendarSyncService calendarSyncService,
                                 @Value("${app.calendar-sync.enabled:true}") boolean enabled) {
        this.calendarSyncService = calendarSyncService;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${app.calendar-sync.fixed-delay-ms:600000}",
            initialDelayString = "${app.calendar-sync.initial-delay-ms:60000}")
    public void syncEnabledFeeds() {
        if (!enabled) {
            return;
        }
        log.debug("Démarrage de la synchronisation planifiée des calendriers");
        calendarSyncService.syncAllEnabled();
    }
}
