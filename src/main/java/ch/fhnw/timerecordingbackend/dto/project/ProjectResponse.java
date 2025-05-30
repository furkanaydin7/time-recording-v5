package ch.fhnw.timerecordingbackend.dto.project;

import ch.fhnw.timerecordingbackend.dto.admin.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Antwort für Projekte Anfragen
 * Projektinformationen an Client senden
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Quelle: https://techkluster.com/2023/08/21/dto-for-a-java-spring-application/
 */
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectStatistics statistics;
    private List<UserResponse> involvedUsers;
    private Long managerId;
    private String managerName;

    /**
     * Konstruktor
     */
    public ProjectResponse() {}

    public ProjectResponse(Long id, String name, String description, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Getter und Setter
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public ProjectStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ProjectStatistics statistics) {
        this.statistics = statistics;
    }

    public List<UserResponse> getInvolvedUsers() {
        return involvedUsers;
    }

    public void setInvolvedUsers(List<UserResponse> involvedUsers) {
        this.involvedUsers = involvedUsers;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    @Override
    public String toString() {
        return "ProjectResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * Embedded-Klasse für Projektstatistiken
     */
    public static class ProjectStatistics {
        private long totalTimeEntries;
        private long activeEmployees;
        private String totalHoursWorked;

        public ProjectStatistics(){}

        public ProjectStatistics(long totalTimeEntries, long activeEmployees, String totalHoursWorked) {
            this.totalTimeEntries = totalTimeEntries;
            this.activeEmployees = activeEmployees;
            this.totalHoursWorked = totalHoursWorked;
        }

        public long getTotalTimeEntries() {
            return totalTimeEntries;
        }

        public void setTotalTimeEntries(long totalTimeEntries) {
            this.totalTimeEntries = totalTimeEntries;
        }

        public long getActiveEmployees() {
            return activeEmployees;
        }

        public void setActiveEmployees(long activeEmployees) {
            this.activeEmployees = activeEmployees;
        }

        public String getTotalHoursWorked() {
            return totalHoursWorked;
        }

        public void setTotalHoursWorked(String totalHoursWorked) {
            this.totalHoursWorked = totalHoursWorked;
        }

        @Override
        public String toString() {
            return "ProjectStatistics{" +
                    "totalTimeEntries=" + totalTimeEntries +
                    ", activeEmployees=" + activeEmployees +
                    ", totalHoursWorked='" + totalHoursWorked + '\'' +
                    '}';
        }
    }
}
