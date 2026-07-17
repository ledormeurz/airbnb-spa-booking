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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/public/property should return 200 with property details")
    void getPropertyReturns200() throws Exception {
        mockMvc.perform(get("/api/public/property"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.description").isString());
    }

    @Test
    @DisplayName("GET /api/public/equipment should return 200 with equipment list")
    void getEquipmentReturns200WithList() throws Exception {
        mockMvc.perform(get("/api/public/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$[0].name").isString());
    }

    @Test
    @DisplayName("GET /api/public/prices should return 200 with price rules")
    void getPricesReturns200() throws Exception {
        mockMvc.perform(get("/api/public/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].bookingType").isString())
                .andExpect(jsonPath("$[0].price").isNumber());
    }

    @Test
    @DisplayName("POST /api/public/booking-requests with valid data should return 201")
    void postBookingRequestWithValidDataReturns201() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Jane");
        request.put("lastName", "Doe");
        request.put("email", "jane.doe@example.com");
        request.put("phone", "0611223344");
        request.put("startDate", "2026-08-15");
        request.put("endDate", "2026-08-17");
        request.put("numberOfGuests", 2);
        request.put("bookingType", "NIGHT_STAY");
        request.put("message", "Looking forward to it!");
        request.put("agreedToRules", true);

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

    @Test
    @DisplayName("POST /api/public/booking-requests with invalid data should return 400")
    void postBookingRequestWithInvalidDataReturns400() throws Exception {
        // Missing required fields
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("firstName", "");
        invalidRequest.put("email", "not-an-email");
        invalidRequest.put("startDate", "invalid-date");
        invalidRequest.put("numberOfGuests", -1);

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/public/booking-requests with past dates should return 400")
    void postBookingRequestWithPastDatesReturns400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "John");
        request.put("lastName", "Doe");
        request.put("email", "john@example.com");
        request.put("startDate", "2020-01-01");
        request.put("endDate", "2020-01-03");
        request.put("numberOfGuests", 2);
        request.put("bookingType", "NIGHT_STAY");

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/public/booking-requests with endDate before startDate should return 400")
    void postBookingRequestWithInvalidDateRangeReturns400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "John");
        request.put("lastName", "Doe");
        request.put("email", "john@example.com");
        request.put("startDate", "2026-08-20");
        request.put("endDate", "2026-08-18");
        request.put("numberOfGuests", 2);
        request.put("bookingType", "NIGHT_STAY");

        mockMvc.perform(post("/api/public/booking-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}