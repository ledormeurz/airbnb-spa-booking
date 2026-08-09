package com.airbnbspa.service;

import com.airbnbspa.dto.CalendarFeedDTO;
import com.airbnbspa.dto.CalendarFeedRequestDTO;
import com.airbnbspa.dto.CalendarSyncResultDTO;
import com.airbnbspa.dto.IcalImportResultDTO;
import com.airbnbspa.entity.CalendarFeed;
import com.airbnbspa.repository.CalendarFeedRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class CalendarSyncService {

    private static final Logger log = LoggerFactory.getLogger(CalendarSyncService.class);
    private static final Set<String> ALLOWED_HOST_SUFFIXES = Set.of(
            "airbnb.com",
            "airbnb.fr",
            "booking.com"
    );

    private final CalendarFeedRepository calendarFeedRepository;
    private final IcalImportService icalImportService;
    private final RestClient restClient;

    public CalendarSyncService(CalendarFeedRepository calendarFeedRepository,
                               IcalImportService icalImportService,
                               RestClient.Builder restClientBuilder) {
        this.calendarFeedRepository = calendarFeedRepository;
        this.icalImportService = icalImportService;
        this.restClient = restClientBuilder.build();
    }

    @Transactional(readOnly = true)
    public List<CalendarFeedDTO> getAllFeeds() {
        return calendarFeedRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public CalendarFeedDTO createFeed(CalendarFeedRequestDTO request) {
        String url = normalizeUrl(request.getUrl());
        validateUrl(url);
        if (calendarFeedRepository.existsByUrl(url)) {
            throw new IllegalArgumentException("Cette URL iCal est déjà enregistrée");
        }

        CalendarFeed feed = CalendarFeed.builder()
                .name(request.getName().trim())
                .url(url)
                .source(normalizeSource(request.getSource()))
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        return toDTO(calendarFeedRepository.save(feed));
    }

    public CalendarFeedDTO updateFeed(Long id, CalendarFeedRequestDTO request) {
        CalendarFeed feed = getFeedEntity(id);
        String url = normalizeUrl(request.getUrl());
        validateUrl(url);
        if (calendarFeedRepository.existsByUrlAndIdNot(url, id)) {
            throw new IllegalArgumentException("Cette URL iCal est déjà enregistrée");
        }

        feed.setName(request.getName().trim());
        feed.setUrl(url);
        feed.setSource(normalizeSource(request.getSource()));
        if (request.getEnabled() != null) {
            feed.setEnabled(request.getEnabled());
        }
        return toDTO(calendarFeedRepository.save(feed));
    }

    public void deleteFeed(Long id) {
        if (!calendarFeedRepository.existsById(id)) {
            throw new EntityNotFoundException("Flux calendrier introuvable: " + id);
        }
        calendarFeedRepository.deleteById(id);
    }

    public CalendarSyncResultDTO syncFeed(Long id) {
        return syncFeedEntity(getFeedEntity(id));
    }

    public List<CalendarSyncResultDTO> syncAllEnabled() {
        return calendarFeedRepository.findByEnabledTrue().stream()
                .map(this::syncFeedEntity)
                .toList();
    }

    private CalendarSyncResultDTO syncFeedEntity(CalendarFeed feed) {
        try {
            String icsContent = fetchIcalContent(feed.getUrl());
            IcalImportResultDTO importResult = icalImportService.importFromString(icsContent, feed.getSource());

            String message = String.format(
                    Locale.ROOT,
                    "%d créé(s), %d mis à jour, %d ignoré(s)",
                    importResult.getImported(),
                    importResult.getUpdated(),
                    importResult.getSkipped()
            );

            feed.setLastSyncedAt(LocalDateTime.now());
            feed.setLastSyncStatus("SUCCESS");
            feed.setLastSyncMessage(truncate(message, 500));
            calendarFeedRepository.save(feed);

            log.info("Sync calendrier id={} source={} : {}", feed.getId(), feed.getSource(), message);

            return CalendarSyncResultDTO.builder()
                    .feedId(feed.getId())
                    .feedName(feed.getName())
                    .source(feed.getSource())
                    .status("SUCCESS")
                    .message(message)
                    .importResult(importResult)
                    .build();
        } catch (Exception ex) {
            String errorMessage = truncate(ex.getMessage() != null ? ex.getMessage() : "Erreur de synchronisation", 500);
            feed.setLastSyncedAt(LocalDateTime.now());
            feed.setLastSyncStatus("ERROR");
            feed.setLastSyncMessage(errorMessage);
            calendarFeedRepository.save(feed);

            log.warn("Échec sync calendrier id={} : {}", feed.getId(), errorMessage);

            return CalendarSyncResultDTO.builder()
                    .feedId(feed.getId())
                    .feedName(feed.getName())
                    .source(feed.getSource())
                    .status("ERROR")
                    .message(errorMessage)
                    .build();
        }
    }

    public String fetchIcalContent(String url) {
        try {
            byte[] body = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(byte[].class);

            if (body == null || body.length == 0) {
                throw new IllegalArgumentException("Le calendrier distant est vide");
            }

            String content = new String(body, StandardCharsets.UTF_8);
            if (!content.contains("BEGIN:VCALENDAR")) {
                throw new IllegalArgumentException("La réponse distante n'est pas un calendrier iCal valide");
            }
            return content;
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Impossible de télécharger le calendrier: " + ex.getMessage(), ex);
        }
    }

    void validateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("URL iCal invalide");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http"))) {
            throw new IllegalArgumentException("L'URL iCal doit commencer par http:// ou https://");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("L'URL iCal doit contenir un hôte valide");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        boolean allowed = ALLOWED_HOST_SUFFIXES.stream()
                .anyMatch(suffix -> normalizedHost.equals(suffix) || normalizedHost.endsWith("." + suffix));
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Hôte non autorisé. Utilisez une URL Airbnb ou Booking.com");
        }
    }

    private CalendarFeed getFeedEntity(Long id) {
        return calendarFeedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flux calendrier introuvable: " + id));
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        return url.trim();
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "ICAL";
        }
        return source.trim().toUpperCase(Locale.ROOT);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private CalendarFeedDTO toDTO(CalendarFeed feed) {
        return CalendarFeedDTO.builder()
                .id(feed.getId())
                .name(feed.getName())
                .url(feed.getUrl())
                .source(feed.getSource())
                .enabled(feed.isEnabled())
                .lastSyncedAt(feed.getLastSyncedAt())
                .lastSyncStatus(feed.getLastSyncStatus())
                .lastSyncMessage(feed.getLastSyncMessage())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .build();
    }
}
