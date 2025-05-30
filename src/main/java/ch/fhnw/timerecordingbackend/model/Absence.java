package ch.fhnw.timerecordingbackend.model;

import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entität Klasse für Abwesenheiten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
@Entity
@Table(name = "absences")
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceType type;

    // Ersetze 'approved' und 'approver' durch 'status' und 'rejectionReason'
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceStatus status = AbsenceStatus.PENDING; // Standardwert ist PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Konstruktoren
    public Absence() {}

    public Absence(User user, LocalDate startDate, LocalDate endDate, AbsenceType type) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Abwesenheit genehmigen
     * @param approver
     */
    public void approve(User approver) {
        this.status = AbsenceStatus.APPROVED;
        this.approver = approver;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Abwesenheit ablehnen
     */
    public void reject() {
        this.status = AbsenceStatus.REJECTED;
        this.approver = null;
    }

    /**
     * Zeitstempel aktualisieren
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Anzahl der Abwesenheitstage berechnen
     * @return Anzahl der Tage
     */
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public AbsenceType getType() { return type; }
    public void setType(AbsenceType type) { this.type = type; }

    public AbsenceStatus getStatus() { return status; }
    public void setStatus(AbsenceStatus status) { this.status = status; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Absence{" +
                "id=" + id +
                ", user=" + user +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}