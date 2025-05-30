package ch.fhnw.timerecordingbackend.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Anfrage-DTO zum Zurücksetzen des Passworts mittels E-Mail-Adresse.
 * Validierung der E-Mail erfolgt über @NotBlank und @Email.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public class ResetPasswordRequest {

    // E-Mail-Adresse muss angegeben werden und muss dem E-Mail-Format entsprechen, darf nicht leer sein
    @NotBlank
    @Email
    private String email;

    // Getter und Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
