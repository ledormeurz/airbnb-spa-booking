package com.airbnbspa.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/admin/dashboard without authentication should return 401")
    void dashboardWithNoAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/dashboard with ROLE_USER should return 403")
    @WithMockUser(username = "johndoe", roles = "USER")
    void dashboardWithUserRoleReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/dashboard with ROLE_ADMIN should return 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void dashboardWithAdminRoleReturns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.pendingBookings").isNumber());
    }

    @Test
    @DisplayName("GET /api/admin/bookings without authentication should return 401")
    void bookingsWithNoAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/bookings with ROLE_USER should return 403")
    @WithMockUser(username = "johndoe", roles = "USER")
    void bookingsWithUserRoleReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/bookings with ROLE_ADMIN should return 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void bookingsWithAdminRoleReturns200() throws Exception {
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/admin/users with ROLE_ADMIN should return 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void usersWithAdminRoleReturns200() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}