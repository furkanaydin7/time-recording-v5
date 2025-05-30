
package ch.fhnw.timerecordingbackend.dto.absence;

import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO Anfragen zum erstellen und aktualisieren von Abwesenheiten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://medium.com/paysafe-bulgaria/springboot-dto-validation-good-practices-and-breakdown-fee69277b3b0
 */
public class AbsenceRequest {

    @NotNull(message = "Startdatum ist erforderlich")
    private LocalDate startDate;

    @NotNull(message = "Enddatum ist erforderlich")
    private LocalDate endDate;

    @NotNull(message = "Abwesenheitstyp ist erforderlich")
    private AbsenceType type;

    @Size(max = 1000, message = "Kommentar darf maximal 1000 Zeichen lang sein")
    private String comment; // NEU! Optionales Kommentar-Feld

    // Konstruktoren
    public AbsenceRequest() {}

    public AbsenceRequest(LocalDate startDate, LocalDate endDate, AbsenceType type) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    public AbsenceRequest(LocalDate startDate, LocalDate endDate, AbsenceType type, String comment) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.comment = comment;
    }

    // Getter und Setter
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public AbsenceType getType() { return type; }
    public void setType(AbsenceType type) { this.type = type; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    // Validierungsmethoden
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !endDate.isBefore(startDate);
    }

    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "AbsenceRequest{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", type=" + type +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50)) + "..." : "null") + '\'' +
                ", duration=" + getDurationInDays() + " days" +
                '}';
    }
}