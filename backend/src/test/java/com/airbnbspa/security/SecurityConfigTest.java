package com.airbnbspa.security;

import com.airbnbspa.entity.User;
import com.airbnbspa.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoint should be accessible without authentication")
    void publicEndpointAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/public/property"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User endpoint should return 401 without authentication")
    void userEndpointReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/user/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin endpoint should return 401 without authentication")
    void adminEndpointReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin endpoint should return 403 for user with ROLE_USER")
    @WithMockUser(username = "johndoe", roles = "USER")
    void adminEndpointReturns403ForUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CORS should allow requests from allowed origins")
    void corsConfiguration() throws Exception {
        mockMvc.perform(get("/api/public/property")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());

        // Verify CORS headers in the response
        mockMvc.perform(get("/api/public/property")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    @DisplayName("Password should never be returned in user endpoint responses")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void passwordNeverReturned() throws Exception {
        // The user endpoint should not expose password_hash in the response
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Preflight CORS request should be handled correctly")
    void corsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/public/property")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
}