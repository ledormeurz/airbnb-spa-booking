package com.airbnbspa.service;

import com.airbnbspa.entity.Booking;
import com.airbnbspa.enums.BookingStatus;
import com.airbnbspa.enums.BookingType;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableStatus;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class ICalendarService {

    private static final String PROD_ID = "-//Le Nid Spa//Booking//FR";
    private static final String UID_DOMAIN = "airbnbspa.com";

    /**
     * Génère un fichier iCalendar (.ics) pour une réservation.
     * Événement journée entière : DTEND = jour de départ (exclusif, standard iCal).
     */
    public String toIcs(Booking booking) {
        LocalDate start = booking.getStartDate();
        LocalDate end = booking.getEndDate();

        // Pour un événement journée entière, DTEND est exclusif.
        // Si start == end (ex. SPA_SESSION), on ajoute 1 jour pour un VEVENT valide.
        LocalDate exclusiveEnd = end.isAfter(start) ? end : start.plusDays(1);

        String summary = buildSummary(booking);
        VEvent event = new VEvent(start, exclusiveEnd, summary);
        event.add(new Uid(buildUid(booking.getId())));
        event.add(new Description(buildDescription(booking)));
        event.add(mapStatus(booking.getStatus()));
        event.add(new DtStamp(Instant.now()));

        Calendar calendar = new Calendar();
        calendar.add(new ProdId(PROD_ID));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);
        calendar.add(event);

        try {
            StringWriter writer = new StringWriter();
            new CalendarOutputter().output(calendar, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de générer le fichier iCalendar", e);
        }
    }

    public String buildUid(Long bookingId) {
        return "booking-" + bookingId + "@" + UID_DOMAIN;
    }

    private String buildSummary(Booking booking) {
        return bookingTypeLabel(booking.getBookingType())
                + " — "
                + nullToEmpty(booking.getFirstName())
                + " "
                + nullToEmpty(booking.getLastName());
    }

    private String buildDescription(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Réservation #").append(booking.getId()).append('\n');
        sb.append("Statut: ").append(booking.getStatus()).append('\n');
        sb.append("Type: ").append(bookingTypeLabel(booking.getBookingType())).append('\n');
        sb.append("Invités: ").append(booking.getNumberOfGuests()).append('\n');
        if (booking.getTotalPrice() != null) {
            sb.append("Prix total: ").append(booking.getTotalPrice()).append(" EUR\n");
        }
        if (booking.getMessage() != null && !booking.getMessage().isBlank()) {
            sb.append("Message: ").append(booking.getMessage()).append('\n');
        }
        return sb.toString().trim();
    }

    private Status mapStatus(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> ImmutableStatus.VEVENT_CONFIRMED;
            case CANCELLED, REJECTED -> ImmutableStatus.VEVENT_CANCELLED;
            case PENDING -> ImmutableStatus.VEVENT_TENTATIVE;
        };
    }

    private String bookingTypeLabel(BookingType type) {
        if (type == null) {
            return "Réservation";
        }
        return switch (type) {
            case NIGHT_STAY -> "Séjour nuitée";
            case EXTENDED_STAY -> "Séjour prolongé";
            case SPA_SESSION -> "Séance spa";
        };
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
