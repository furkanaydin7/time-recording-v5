package ch.fhnw.timerecordingbackend.dto.authentication;

import jakarta.validation.constraints.*;

/**
 * Anfrage-DTO für Benutzer-Login mit E-Mail und Passwort.
 * Verwendet in /api/auth/login.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public class LoginRequest {
    // Feld darf nicht leer sein und muss ein gültiges E-Mail-Format haben
    @NotBlank
    @Email
    private String email;

    // Passwort darf nicht leer sein
    @NotBlank
    private String password;

    // Getter und Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
