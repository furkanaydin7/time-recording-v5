package ch.fhnw.timerecordingbackend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO für Anfragen zum Aktualisieren von Daten eines Benutzers
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://medium.com/paysafe-bulgaria/springboot-dto-validation-good-practices-and-breakdown-fee69277b3b0
 */
public class UserUpdateRequest {

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Size(min = 2, max = 50, message = "Vorname muss zwischen 2 und 50 Zeichen lang sein")
    private String firstName;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(min = 2, max = 50, message = "Nachname muss zwischen 2 und 50 Zeichen lang sein")
    private String lastName;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "E-Mail muss eine gültige Adresse haben")
    @Size(min = 5, max = 255, message = "E-Mail muss zwischen 5 und 255 Zeichen lang sein")
    private String email;

    @NotBlank(message = "Geplante Stunden pro Tag dürfen nicht null sein")
    private double plannedHoursPerDay;

    /**
     * Getter und Setter
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getPlannedHoursPerDay() {
        return plannedHoursPerDay;
    }

    public void setPlannedHoursPerDay(double plannedHoursPerDay) {
        this.plannedHoursPerDay = plannedHoursPerDay;
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", plannedHoursPerDay=" + plannedHoursPerDay +
                '}';
    }
}
