package ch.fhnw.timerecordingbackend.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Anfrageobjekt für das Zurücksetzen des Passworts anhand der E-Mail-Adresse
 * Validierung erfolgt über @NotBlank und @Email
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public class ResetPasswordRequest {

    // E-Mail-Adresse muss angegeben werden, darf nicht leer sein
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
