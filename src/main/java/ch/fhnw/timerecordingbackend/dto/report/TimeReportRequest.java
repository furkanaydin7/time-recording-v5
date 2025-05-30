package ch.fhnw.timerecordingbackend.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO Anfragen zum erstellen und aktualisieren von Zeitreports
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://medium.com/paysafe-bulgaria/springboot-dto-validation-good-practices-and-breakdown-fee69277b3b0
 */
public class TimeReportRequest {

    @NotNull(message = "Startdatum darf nicht null sein")
    private LocalDate startDate;

    @NotNull(message = "Enddatum darf nicht null sein")
    private LocalDate endDate;

    private List<Long> userIds;
    private List<Long> projectId;
    private ReportType reportType; //Quelle: ChatGPT.com
    private ReportFormat reportFormat; //Quelle: ChatGPT.com
    private boolean includeInactive;

    /**
     * Konstruktor
     */
    //Standardkonstruktor als SUMMARY und JSON Format
    public TimeReportRequest() {
        this.reportType = ReportType.SUMMARY;
        this.reportFormat = ReportFormat.JSON;
        this.includeInactive = false;
    }

    public TimeReportRequest(LocalDate startDate, LocalDate endDate, ReportType reportType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportType = reportType;
    }

    /**
     * Prüfen, ob Startdatum vor Enddatum
     */
    public boolean isDateRangeValid() {
        return startDate.isBefore(endDate);
    }

    /**
     * Anzahl der Tage berechnen für Berichtszeitraum
     */
    public long getDurationInDays() {
        return startDate.datesUntil(endDate.plusDays(1)).count();
    }

    /**
     * Prüfen ob spezifischer Benutzer angegeben wurde
     */
    public boolean hasSpecificUser() {
        return userIds != null && !userIds.isEmpty();
    }

    /**
     * Prüfen ob spezifischer Projekt angegeben wurde
     */
    public boolean hasSpecificProject() {
        return projectId != null && !projectId.isEmpty();
    }

    /**
     * Getter und Setter
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getProjectId() {
        return projectId;
    }

    public void setProjectId(List<Long> projectId) {
        this.projectId = projectId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportFormat getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(ReportFormat reportFormat) {
        this.reportFormat = reportFormat;
    }

    public boolean isIncludeInactive() {
        return includeInactive;
    }

    public void setIncludeInactive(boolean includeInactive) {
        this.includeInactive = includeInactive;
    }

    @Override
    public String toString() {
        return "TimeReportRequest{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", userIds=" + userIds +
                ", projectId=" + projectId +
                ", reportType=" + reportType +
                ", reportFormat=" + reportFormat +
                ", includeInactive=" + includeInactive +
                ", durationInDays=" + getDurationInDays() +
                '}';
    }

    /**
     * Enum für Report-Typen
     * Quelle ChatGPT.com
     */
    public enum ReportType {
        SUMMARY("Zusammenfassung"),
        BY_USER("Nutzerbericht"),
        BY_PROJECT("Projektbericht"),
        BY_DATE("Nach Datum"),
        BY_TIME_RANGE("Nach Zeitraum");

        private final String displayName;

        ReportType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum für Report-Formate
     * Quelle ChatGPT.com
     */
    public enum ReportFormat {
        CSV("CSV"),
        JSON("JSON"),
        PDF("PDF");

        private final String displayName;

        ReportFormat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
