package ch.fhnw.timerecordingbackend.dto.time;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Anfrageobjekt für das Erstellen oder Aktualisieren eines Zeiteintrags
 * Enthält Datum, Start-/Endzeiten, optionale Pausen und Projekt-ID
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public class TimeEntryRequest {

    @NotNull
    private LocalDate date;

    private List<String> startTimes = new ArrayList<>();

    private List<String> endTimes = new ArrayList<>();

    private List<BreakTime> breaks = new ArrayList<>();

    private Long projectId;

    // Getter und Setter
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
     * Innere statische Klasse zur Darstellung einer Pause innerhalb eines Zeiteintrags
     * Jede Pause besteht aus einem Start- und einem Endzeitpunkt (Format: HH:mm)
     */
    public static class BreakTime {
        private String start;
        private String end;

        // Konstruktoren
        public BreakTime() {}

        public BreakTime(String start, String end) {
            this.start = start;
            this.end = end;
        }

        // Getter und Setter
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