package com.airbnbspa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ICalendarExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Map<String, Object> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private long createBookingForEmail(String email, LocalDate start, LocalDate end) throws Exception {
        String body = json(Map.of(
                "firstName", "Ical",
                "lastName", "Tester",
                "email", email,
                "phone", "0600000000",
                "startDate", start.toString(),
                "endDate", end.toString(),
                "numberOfGuests", 2,
                "bookingType", "NIGHT_STAY",
                "message", "Test export iCal",
                "agreedToRules", true));

        String response = mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password,
                                "firstName", "Ical",
                                "lastName", "User"))))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).get("accessToken").asText();
    }

    @Test
    @DisplayName("GET calendar.ics renvoie un fichier iCalendar pour le propriétaire")
    void ownerCanDownloadCalendarIcs() throws Exception {
        String email = "ical.owner@example.com";
        String token = registerAndLogin(email, "secret123");
        long bookingId = createBookingForEmail(email, LocalDate.now().plusDays(40), LocalDate.now().plusDays(42));

        mockMvc.perform(get("/api/user/bookings/" + bookingId + "/calendar.ics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/calendar")))
                .andExpect(header().string("Content-Disposition",
                        containsString("reservation-" + bookingId + ".ics")))
                .andExpect(content().string(containsString("BEGIN:VCALENDAR")))
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("UID:booking-" + bookingId + "@airbnbspa.com")))
                .andExpect(content().string(containsString("END:VCALENDAR")));
    }

    @Test
    @DisplayName("GET calendar.ics sans authentification renvoie 401")
    void unauthenticatedCalendarDownloadIsRejected() throws Exception {
        mockMvc.perform(get("/api/user/bookings/1/calendar.ics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET calendar.ics pour la réservation d'un autre utilisateur renvoie 403")
    void otherUserCannotDownloadCalendarIcs() throws Exception {
        String ownerEmail = "ical.owner2@example.com";
        String otherEmail = "ical.other@example.com";

        registerAndLogin(ownerEmail, "secret123");
        long bookingId = createBookingForEmail(
                ownerEmail, LocalDate.now().plusDays(50), LocalDate.now().plusDays(52));

        String otherToken = registerAndLogin(otherEmail, "secret123");

        mockMvc.perform(get("/api/user/bookings/" + bookingId + "/calendar.ics")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }
}
