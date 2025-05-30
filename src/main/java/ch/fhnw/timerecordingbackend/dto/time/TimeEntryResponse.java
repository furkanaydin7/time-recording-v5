package ch.fhnw.timerecordingbackend.dto.time;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

/**
 * Antwortobjekt für Zeiteinträge mit Arbeitszeiten, Pausen, Projekt- und Benutzerdaten.
 * Optionale Felder werden nur bei Bedarf serialisiert (@JsonInclude)
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeEntryResponse {

    private Long id;
    private LocalDate date;
    private List<String> startTimes;
    private List<String> endTimes;
    private List<BreakTime> breaks;
    private String actualHours;
    private String plannedHours;
    private String difference;
    private ProjectDto project;
    private Long userId;
    private String user;

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<String> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(List<String> startTimes) {
        this.startTimes = startTimes;
    }

    public List<String> getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(List<String> endTimes) {
        this.endTimes = endTimes;
    }

    public List<BreakTime> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<BreakTime> breaks) {
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

    public ProjectDto getProject() {
        return project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Innere Klasse zur Darstellung einer einzelnen Pause mit Start- und Endzeit
     */
    public static class BreakTime {
        private String start;
        private String end;

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    /**
     * Innere Klasse zur kompakten Darstellung von Projektdetails in der Antwort
     */
    public static class ProjectDto {
        private Long id;
        private String name;

        public ProjectDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

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
    }
}
