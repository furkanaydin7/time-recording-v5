package ch.fhnw.timerecordingbackend.dto.time;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request-DTO für das Erstellen oder Aktualisieren eines Zeiteintrags.
 * Enthält Datum, Start-/Endzeiten, optionale Pausen und eine optionale Projekt-ID.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public class TimeEntryRequest {

    @NotNull  // Datum darf nicht null sein
    private LocalDate date;

    // Liste von Startzeiten (Format: HH:mm); kann leer sein
    private List<String> startTimes = new ArrayList<>();

    // Liste von Endzeiten (Format: HH:mm); kann leer sein
    private List<String> endTimes = new ArrayList<>();

    // Liste von Pausen innerhalb des Zeiteintrags
    private List<BreakTime> breaks = new ArrayList<>();

    // ID des zugeordneten Projekts (optional)
    private Long projectId;

    // ==================== Getter und Setter ====================

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
        // Setze leere Liste, falls null übergeben wurde
        this.startTimes = startTimes != null ? startTimes : new ArrayList<>();
    }

    public List<String> getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(List<String> endTimes) {
        this.endTimes = endTimes != null ? endTimes : new ArrayList<>();
    }

    public List<BreakTime> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<BreakTime> breaks) {
        this.breaks = breaks != null ? breaks : new ArrayList<>();
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Innere Klasse zur Darstellung eines Pausenabschnitts.
     * Jede Pause besitzt Start- und Endzeit (Format: HH:mm).
     */
    public static class BreakTime {
        // Startzeit der Pause (HH:mm)
        private String start;
        // Endzeit der Pause (HH:mm)
        private String end;

        public BreakTime() {}

        public BreakTime(String start, String end) {
            this.start = start;
            this.end = end;
        }

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
}
