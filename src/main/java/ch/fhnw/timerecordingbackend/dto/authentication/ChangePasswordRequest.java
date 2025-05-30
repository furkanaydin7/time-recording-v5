package ch.fhnw.timerecordingbackend.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Anfrageobjekt zum Ändern des Passworts mit altem und neuem Passwort
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public class ChangePasswordRequest {

    // Aktuelles Passwort, darf nicht leer sein
    @NotBlank
    private String oldPassword;

    // Neues Passwort, mindestens 6 Zeichen lang
    @NotBlank
    @Size(min = 6)
    private String newPassword;

    // Getter und Setter
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
