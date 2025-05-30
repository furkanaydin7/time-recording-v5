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

/**
 * REST-Controller für Authentifizierungsoperationen (Login & Logout).
 * Behandelt Anfragen an /api/auth.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Führt eine Login-Authentifizierung durch.
     * Erwartet E-Mail und Passwort als JSON (LoginRequest), validiert die Daten,
     * und liefert bei Erfolg ein JWT mit Benutzerdaten zurück.
     */
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

    /**
     * Führt einen Logout durch, indem das übermittelte JWT-Token ungültig gemacht wird (Blacklist).
     * Erwartet das Token im Request-Body als String.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String token) {
        authService.logout(token);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Erfolgreich ausgeloggt"));
    }
}