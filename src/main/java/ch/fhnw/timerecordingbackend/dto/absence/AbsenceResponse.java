
package ch.fhnw.timerecordingbackend.dto.absence;

import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO Antwort für Abwesenheiten Anfragen
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://techkluster.com/2023/08/21/dto-for-a-java-spring-application/
 */
public class AbsenceResponse {

    // Basis-Informationen
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private AbsenceType type;
    private String comment; // NEU!
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Benutzer-Informationen
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;

    // Genehmiger-Informationen (NEU!)
    private Long processedById;
    private String processedByName;
    private LocalDateTime processedDate;

    // Ablehnungs-Informationen (NEU!)
    private String rejectionReason;

    // Status für UI (NEU!)
    private AbsenceStatus status; // Für Backward Compatibility

    public AbsenceResponse() {}

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public AbsenceType getType() { return type; }
    public void setType(AbsenceType type) { this.type = type; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Benutzer-Informationen
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return email;
    }

    // Genehmiger-Informationen
    public Long getProcessedById() { return processedById; }
    public void setProcessedById(Long processedById) { this.processedById = processedById; }

    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }

    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }

    // Ablehnungs-Informationen
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    // Backward Compatibility
    public AbsenceStatus getStatus() { return status; } // Getter für Status
    public void setStatus(AbsenceStatus status) { this.status = status; } // Setter für Status

    public long getDurationInDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "AbsenceResponse{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", type=" + type +
                ", status=" + status +
                ", email='" + email + '\'' +
                '}';
    }
}