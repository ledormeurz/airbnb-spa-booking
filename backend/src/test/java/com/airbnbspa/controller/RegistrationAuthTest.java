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

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Map<String, Object> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("POST /api/public/register crée un compte et renvoie 201")
    void registerCreatesAccount() throws Exception {
        String body = json(Map.of(
                "email", "alice@example.com",
                "password", "secret123",
                "firstName", "Alice",
                "lastName", "Martin"));

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Après inscription, la connexion par email fonctionne sur un endpoint protégé")
    void emailLoginWorksAfterRegistration() throws Exception {
        String body = json(Map.of(
                "email", "bob@example.com",
                "password", "secret123",
                "firstName", "Bob",
                "lastName", "Durand"));

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // L'email + mot de passe permettent d'accéder à l'espace utilisateur
        mockMvc.perform(get("/api/user/profile")
                        .with(httpBasic("bob@example.com", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));

        // Un mauvais mot de passe est rejeté
        mockMvc.perform(get("/api/user/profile")
                        .with(httpBasic("bob@example.com", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Une inscription avec un email déjà utilisé renvoie 400")
    void duplicateEmailIsRejected() throws Exception {
        String body = json(Map.of(
                "email", "carol@example.com",
                "password", "secret123",
                "firstName", "Carol",
                "lastName", "Petit"));

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Une inscription avec un email invalide ou un mot de passe trop court renvoie 400")
    void invalidPayloadIsRejected() throws Exception {
        String badEmail = json(Map.of(
                "email", "not-an-email",
                "password", "secret123",
                "firstName", "Dan",
                "lastName", "Roux"));

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badEmail))
                .andExpect(status().isBadRequest());

        String shortPassword = json(Map.of(
                "email", "dan@example.com",
                "password", "123",
                "firstName", "Dan",
                "lastName", "Roux"));

        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shortPassword))
                .andExpect(status().isBadRequest());
    }
}
