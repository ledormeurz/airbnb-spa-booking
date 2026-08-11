package com.airbnbspa.controller;

import com.airbnbspa.repository.AvailabilityBlockRepository;
import com.airbnbspa.service.CalendarSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CalendarFeedSyncTest {

    private static final String SAMPLE_ICS = """
            BEGIN:VCALENDAR
            PRODID:-//Test//EN
            VERSION:2.0
            BEGIN:VEVENT
            DTSTART;VALUE=DATE:20250715
            DTEND;VALUE=DATE:20250722
            SUMMARY:Reserved
            UID:sync-url-event-1@airbnb.com
            END:VEVENT
            END:VCALENDAR
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvailabilityBlockRepository availabilityBlockRepository;

    @MockitoSpyBean
    private CalendarSyncService calendarSyncService;

    @Test
    @DisplayName("POST /api/admin/calendar-feeds without auth returns 401")
    void createFeedWithoutAuthReturns401() throws Exception {
        mockMvc.perform(post("/api/admin/calendar-feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Airbnb",
                                  "url": "https://www.airbnb.com/calendar/ical/123.ics",
                                  "source": "AIRBNB"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST calendar-feeds rejects non Airbnb/Booking host")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createFeedRejectsUnauthorizedHost() throws Exception {
        mockMvc.perform(post("/api/admin/calendar-feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Evil",
                                  "url": "https://example.com/calendar.ics",
                                  "source": "AIRBNB"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST calendar-feeds accepts Airbnb national domains (ex. airbnb.co.uk)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createFeedAcceptsAirbnbCoUk() throws Exception {
        mockMvc.perform(post("/api/admin/calendar-feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Airbnb UK",
                                  "url": "https://www.airbnb.co.uk/calendar/ical/listing-test.ics",
                                  "source": "AIRBNB",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("AIRBNB"));
    }

    @Test
    @DisplayName("Create feed then sync URL imports AvailabilityBlocks")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createFeedAndSyncImportsBlocks() throws Exception {
        doReturn(SAMPLE_ICS).when(calendarSyncService).fetchIcalContent(anyString());

        MvcResult created = mockMvc.perform(post("/api/admin/calendar-feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Airbnb test",
                                  "url": "https://www.airbnb.com/calendar/ical/listing-test.ics",
                                  "source": "AIRBNB",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.source").value("AIRBNB"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andReturn();

        String body = created.getResponse().getContentAsString();
        Long feedId = Long.valueOf(body.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1"));

        mockMvc.perform(post("/api/admin/calendar-feeds/" + feedId + "/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.importResult.imported").value(1))
                .andExpect(jsonPath("$.importResult.totalEvents").value(1));

        assertThat(availabilityBlockRepository.findByExternalUid("sync-url-event-1@airbnb.com")).isPresent();

        mockMvc.perform(get("/api/admin/calendar-feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastSyncStatus").value("SUCCESS"));

        mockMvc.perform(delete("/api/admin/calendar-feeds/" + feedId))
                .andExpect(status().isNoContent());
    }
}
