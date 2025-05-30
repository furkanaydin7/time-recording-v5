package ch.fhnw.timerecordingbackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entität Klasse für Zeiteinträge
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @ElementCollection
    @CollectionTable(name = "time_entry_start_times", joinColumns = @JoinColumn(name = "time_entry_id"))
    @Column(name = "start_time")
    private Set<LocalTime> startTimes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "time_entry_end_times", joinColumns = @JoinColumn(name = "time_entry_id"))
    @Column(name = "end_time")
    private Set<LocalTime> endTimes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "time_entry_breaks", joinColumns = @JoinColumn(name = "time_entry_id"))
    private Set<Break> breaks = new HashSet<>();

    @Column(name = "actual_hours", nullable = false)
    private String actualHours;

    @Column(name = "planned_hours", nullable = false)
    private String plannedHours;

    @Column(nullable = false)
    private String difference;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Konstruktoren
     */
    public TimeEntry() {
        this.actualHours = "00:00";
        this.plannedHours = "00:00";
        this.difference = "00:00";
    }

    public TimeEntry(User user, LocalDate date, String actualHours, String plannedHours, String difference) {
        this.user = user;
        this.date = date;
        this.actualHours = actualHours;
        this.plannedHours = plannedHours;
        this.difference = difference;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Start, Pausen- und Endzeit hinzufügen
     */
    public void addStartTime(LocalTime startTime) {
        startTimes.add(startTime);
    }

    public void addEndTime(LocalTime endTime) {
        endTimes.add(endTime);
    }

    public void addBreak(LocalTime start, LocalTime end) {
        breaks.add(new Break(start, end));
    }

    /**
     * Zeiteintrag aktiv prüfen
     */
    public boolean isActive() {
        return startTimes.size() > endTimes.size();
    }

    /**
     * Zeitstempel bei Änderungen aktualisieren
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Set<LocalTime> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(Set<LocalTime> startTimes) {
        this.startTimes = startTimes;
    }

    public Set<LocalTime> getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(Set<LocalTime> endTimes) {
        this.endTimes = endTimes;
    }

    public Set<Break> getBreaks() {
        return breaks;
    }

    public void setBreaks(Set<Break> breaks) {
        this.breaks = breaks;
    }

    public String getActualHours() {
        return actualHours;
    }

    public void setActualHours(String actualHours) {
        this.actualHours = actualHours;
    }

    public String getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(String plannedHours) {
        this.plannedHours = plannedHours;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * String-Repräsentation des Zeiteintrags
     * @return String-Repräsentation des Zeiteintrags
     */
    @Override
    public String toString() {
        return "TimeEntry{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", date=" + date +
                ", actualHours='" + actualHours + '\'' +
                ", plannedHours='" + plannedHours + '\'' +
                ", difference='" + difference + '\'' +
                ", project=" + (project != null ? project.getName() : null) +
                '}';
    }

    /**
     * Embedded-Klasse für Pausen
     */
    @Embeddable
    public static class Break {
        @Column(name = "break_start")
        private LocalTime start;

        @Column(name = "break_end")
        private LocalTime end;

        // Konstruktor
        public Break() {
        }

        public Break(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        // Getter und Setter
        public LocalTime getEnd() {
            return end;
        }

        public void setEnd(LocalTime end) {
            this.end = end;
        }
        public LocalTime getStart() {
            return start;
        }
        public void setStart(LocalTime start) {
            this.start = start;
        }

        @Override
        public String toString() {
            return "Break{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

}
