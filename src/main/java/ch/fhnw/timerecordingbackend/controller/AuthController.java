package ch.fhnw.timerecordingbackend.controller;


import ch.fhnw.timerecordingbackend.dto.authentication.LoginRequest;
import ch.fhnw.timerecordingbackend.dto.authentication.LoginResponse;
import ch.fhnw.timerecordingbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Ungültige E-Mail oder Passwort"));
        } catch (DisabledException e) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of("error", "Benutzerkonto ist deaktiviert"));
        } catch (Exception e) {
            // Detaillierte Fehlerinformation in der Konsole ausgeben
            System.err.println("Login-Fehler: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", "Unbekannter Serverfehler"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String token) {
        authService.logout(token);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Erfolgreich ausgeloggt"));
    }
}