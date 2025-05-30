package ch.fhnw.timerecordingbackend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO für Anfragen zum Registrieren eines Benutzers
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Quelle: https://medium.com/paysafe-bulgaria/springboot-dto-validation-good-practices-and-breakdown-fee69277b3b0
 */
public class UserRegistrationRequest {

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

    @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
    private String password;

    @NotBlank(message = "Rolle darf nicht leer sein")
    private String role;

    @NotNull(message = "Geplante Stunden pro Tag darf nicht null sein")
    // Der einfachheit vorerst 8.0 definiert
    private double plannedHoursPerDay = 8.0;

    private Long managerId;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getPlannedHoursPerDay() {
        return plannedHoursPerDay;
    }

    public void setPlannedHoursPerDay(double plannedHoursPerDay) {
        this.plannedHoursPerDay = plannedHoursPerDay;
    }

    public Long getManagerId() { // Getter für managerId
        return managerId;
    }

    public void setManagerId(Long managerId) { // Setter für managerId
        this.managerId = managerId;
    }

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", plannedHoursPerDay=" + plannedHoursPerDay +
                '}';
    }
}
