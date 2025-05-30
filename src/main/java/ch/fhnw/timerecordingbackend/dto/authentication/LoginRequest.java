package ch.fhnw.timerecordingbackend.dto.authentication;

import jakarta.validation.constraints.*;

/**
 * Datenmodell für Login-Anfragen mit E-Mail und Passwort
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
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
