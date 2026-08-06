package com.airbnbspa.controller;

import com.airbnbspa.entity.AvailabilityBlock;
import com.airbnbspa.repository.AvailabilityBlockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IcalImportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvailabilityBlockRepository availabilityBlockRepository;

    @Test
    @DisplayName("POST /api/admin/availability-blocks/import-ics without auth returns 401")
    void importWithoutAuthReturns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.ics", "text/calendar", "BEGIN:VCALENDAR\nEND:VCALENDAR".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/admin/availability-blocks/import-ics").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST import-ics with ROLE_USER returns 403")
    @WithMockUser(username = "johndoe", roles = "USER")
    void importWithUserRoleReturns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.ics", "text/calendar", "BEGIN:VCALENDAR\nEND:VCALENDAR".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/admin/availability-blocks/import-ics").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Import Airbnb-like ICS creates blocks with exclusive DTEND converted to inclusive endDate")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void importAirbnbIcsCreatesBlocks() throws Exception {
        byte[] content = new ClassPathResource("fixtures/sample-airbnb.ics").getContentAsByteArray();
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample-airbnb.ics", "text/calendar", content);

        mockMvc.perform(multipart("/api/admin/availability-blocks/import-ics")
                        .file(file)
                        .param("source", "AIRBNB")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(3))
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("AIRBNB"));

        AvailabilityBlock first = availabilityBlockRepository.findByExternalUid("sample-event-1@airbnb.com").orElseThrow();
        assertThat(first.getStartDate()).isEqualTo(LocalDate.of(2025, 7, 15));
        // DTEND 2025-07-22 is exclusive in iCal DATE → inclusive endDate = 2025-07-21
        assertThat(first.getEndDate()).isEqualTo(LocalDate.of(2025, 7, 21));
        assertThat(first.getSource()).isEqualTo("AIRBNB");
        assertThat(first.getReason()).isEqualTo("Reserved (import iCal)");

        AvailabilityBlock second = availabilityBlockRepository.findByExternalUid("sample-event-2@airbnb.com").orElseThrow();
        assertThat(second.getStartDate()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(second.getEndDate()).isEqualTo(LocalDate.of(2025, 8, 4));
    }

    @Test
    @DisplayName("Re-importing same ICS updates existing blocks by UID (idempotent)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void reimportIsIdempotent() throws Exception {
        byte[] content = new ClassPathResource("fixtures/sample-airbnb.ics").getContentAsByteArray();
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample-airbnb.ics", "text/calendar", content);

        mockMvc.perform(multipart("/api/admin/availability-blocks/import-ics")
                        .file(file)
                        .param("source", "AIRBNB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3));

        mockMvc.perform(multipart("/api/admin/availability-blocks/import-ics")
                        .file(file)
                        .param("source", "AIRBNB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(0))
                .andExpect(jsonPath("$.updated").value(3))
                .andExpect(jsonPath("$.totalEvents").value(3));

        assertThat(availabilityBlockRepository.findByExternalUid("sample-event-1@airbnb.com")).isPresent();
    }
}
