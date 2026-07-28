package com.airbnbspa.service;

import com.airbnbspa.config.LoginAttemptService;
import com.airbnbspa.dto.AuthResponseDTO;
import com.airbnbspa.entity.User;
import com.airbnbspa.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserService userService,
                       JwtService jwtService,
                       LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthResponseDTO login(String email, String password) {
        if (loginAttemptService.isBlocked(email)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Compte temporairement bloqué suite à trop de tentatives. Réessayez dans 15 minutes.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            loginAttemptService.loginSucceeded(email);
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }

        User user = userService.findByLogin(email);
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte désactivé");
        }

        return AuthResponseDTO.builder()
                .accessToken(jwtService.generateToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(userService.toDTO(user))
                .build();
    }
}
