package com.airbnbspa.service;

import com.airbnbspa.dto.IcalImportResultDTO;
import com.airbnbspa.entity.AvailabilityBlock;
import com.airbnbspa.repository.AvailabilityBlockRepository;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IcalImportService {

    public static final String DEFAULT_SOURCE = "ICAL";

    private final AvailabilityBlockRepository availabilityBlockRepository;

    public IcalImportService(AvailabilityBlockRepository availabilityBlockRepository) {
        this.availabilityBlockRepository = availabilityBlockRepository;
    }

    public IcalImportResultDTO importFromFile(MultipartFile file, String source) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier .ics est requis");
        }
        try (InputStream in = file.getInputStream()) {
            return importFromStream(in, normalizeSource(source));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Fichier iCalendar invalide: " + ex.getMessage(), ex);
        }
    }

    public IcalImportResultDTO importFromString(String icsContent, String source) {
        if (icsContent == null || icsContent.isBlank()) {
            throw new IllegalArgumentException("Le contenu iCalendar est vide");
        }
        try {
            Calendar calendar = new CalendarBuilder().build(new StringReader(icsContent));
            return importCalendar(calendar, normalizeSource(source));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Fichier iCalendar invalide: " + ex.getMessage(), ex);
        }
    }

    public IcalImportResultDTO importFromStream(InputStream inputStream, String source) throws Exception {
        Calendar calendar = new CalendarBuilder().build(inputStream);
        return importCalendar(calendar, normalizeSource(source));
    }

    private IcalImportResultDTO importCalendar(Calendar calendar, String source) {
        List<VEvent> events = calendar.getComponents(Component.VEVENT);
        int imported = 0;
        int updated = 0;
        int skipped = 0;

        for (VEvent event : events) {
            Optional<ParsedEvent> parsed = parseEvent(event);
            if (parsed.isEmpty()) {
                skipped++;
                continue;
            }
            ParsedEvent pe = parsed.get();
            Optional<AvailabilityBlock> existing = availabilityBlockRepository.findByExternalUid(pe.uid());
            if (existing.isPresent()) {
                AvailabilityBlock block = existing.get();
                block.setStartDate(pe.startDate());
                block.setEndDate(pe.endDateInclusive());
                block.setReason(pe.reason());
                block.setSource(source);
                availabilityBlockRepository.save(block);
                updated++;
            } else {
                AvailabilityBlock block = AvailabilityBlock.builder()
                        .startDate(pe.startDate())
                        .endDate(pe.endDateInclusive())
                        .reason(pe.reason())
                        .externalUid(pe.uid())
                        .source(source)
                        .build();
                availabilityBlockRepository.save(block);
                imported++;
            }
        }

        return IcalImportResultDTO.builder()
                .totalEvents(events.size())
                .imported(imported)
                .updated(updated)
                .skipped(skipped)
                .source(source)
                .build();
    }

    private Optional<ParsedEvent> parseEvent(VEvent event) {
        Optional<DtStart<Temporal>> startOpt = event.getStartDate();
        if (startOpt.isEmpty()) {
            return Optional.empty();
        }

        LocalDate startDate = toLocalDate(startOpt.get().getDate());
        if (startDate == null) {
            return Optional.empty();
        }

        // DTEND iCal (VALUE=DATE) est exclusif. AvailabilityBlock utilise une fin inclusive.
        LocalDate exclusiveEnd = event.getEndDate(true)
                .map(DtEnd::getDate)
                .map(this::toLocalDate)
                .orElse(startDate.plusDays(1));

        if (exclusiveEnd == null || !exclusiveEnd.isAfter(startDate)) {
            return Optional.empty();
        }

        LocalDate inclusiveEnd = exclusiveEnd.minusDays(1);

        String uid = event.getProperty(Property.UID)
                .map(p -> ((Uid) p).getValue())
                .filter(v -> v != null && !v.isBlank())
                .orElse(null);
        if (uid == null) {
            return Optional.empty();
        }

        String summary = event.getProperty(Property.SUMMARY)
                .map(p -> ((Summary) p).getValue())
                .filter(v -> v != null && !v.isBlank())
                .orElse("Reserved");

        String reason = summary + " (import iCal)";
        if (reason.length() > 255) {
            reason = reason.substring(0, 255);
        }

        return Optional.of(new ParsedEvent(uid, startDate, inclusiveEnd, reason));
    }

    private LocalDate toLocalDate(Temporal temporal) {
        if (temporal == null) {
            return null;
        }
        if (temporal instanceof LocalDate localDate) {
            return localDate;
        }
        if (temporal instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (temporal instanceof java.time.ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDate();
        }
        if (temporal instanceof java.time.OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }
        if (temporal instanceof java.time.Instant instant) {
            return instant.atZone(ZoneOffset.UTC).toLocalDate();
        }
        try {
            return LocalDate.from(temporal);
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return DEFAULT_SOURCE;
        }
        return source.trim().toUpperCase();
    }

    private record ParsedEvent(String uid, LocalDate startDate, LocalDate endDateInclusive, String reason) {}
}
