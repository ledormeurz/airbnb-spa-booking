package com.airbnbspa.controller;

import com.airbnbspa.config.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.booking.max-requests=2",
        "app.rate-limit.booking.window-ms=600000",
        "app.rate-limit.login.max-requests=2",
        "app.rate-limit.login.window-ms=600000"
})
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitService rateLimitService;

    @BeforeEach
    void resetLimits() {
        rateLimitService.clearAll();
    }

    @Test
    @DisplayName("POST /api/public/booking-requests returns 429 after exceeding IP limit")
    void bookingRateLimitReturns429() throws Exception {
        String body = """
                {
                  "firstName": "Alice",
                  "lastName": "Test",
                  "email": "alice-rate@example.com",
                  "phone": "0600000000",
                  "startDate": "2099-09-01",
                  "endDate": "2099-09-03",
                  "numberOfGuests": 2,
                  "bookingType": "NIGHT_STAY",
                  "agreedToRules": true
                }
                """;

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isCreated());

        String body2 = body.replace("alice-rate@example.com", "alice-rate-2@example.com");
        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isCreated());

        String body3 = body.replace("alice-rate@example.com", "alice-rate-3@example.com");
        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body3)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "600"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.limit").value("booking"));
    }

    @Test
    @DisplayName("Different IPs have independent booking rate limits")
    void differentIpsAreIndependent() throws Exception {
        String body = """
                {
                  "firstName": "Bob",
                  "lastName": "Test",
                  "email": "bob-rate@example.com",
                  "phone": "0600000001",
                  "startDate": "2099-10-01",
                  "endDate": "2099-10-03",
                  "numberOfGuests": 2,
                  "bookingType": "NIGHT_STAY",
                  "agreedToRules": true
                }
                """;

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.20");
                            return request;
                        }))
                .andExpect(status().isCreated());

        String bodyOther = body.replace("bob-rate@example.com", "bob-rate-other@example.com");
        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyOther)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.21");
                            return request;
                        }))
                .andExpect(status().isCreated());
    }
}
