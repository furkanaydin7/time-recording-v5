package ch.fhnw.timerecordingbackend.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO Antwort für Zeitreports
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://techkluster.com/2023/08/21/dto-for-a-java-spring-application/
 */
public class TimeReportResponse {

    private Long reportId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimeReportRequest.ReportType type;
    private TimeReportRequest.ReportFormat format;
    private LocalDateTime generatedAt;
    private ReportSummary summary;
    private List<TimeReportEntry> entries;
    private Map<String, Object> groupedData;
    private ReportMetaData metaData; // Quelle: ChatGPT.com

    /**
     * Konstruktor
     */
    public TimeReportResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public TimeReportResponse (TimeReportRequest.ReportType type, LocalDate startDate, LocalDate endDate) {
        this.generatedAt = LocalDateTime.now();
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Anzahl der Tage im Zeitreport berechnen
     */
    public long getDurationInDays() {
        return startDate.datesUntil(endDate.plusDays(1)).count();
    }

    /**
     * Getter und Setter
     */
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

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

    public TimeReportRequest.ReportType getType() {
        return type;
    }

    public void setType(TimeReportRequest.ReportType type) {
        this.type = type;
    }

    public TimeReportRequest.ReportFormat getFormat() {
        return format;
    }

    public void setFormat(TimeReportRequest.ReportFormat format) {
        this.format = format;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public ReportSummary getSummary() {
        return summary;
    }

    public void setSummary(ReportSummary summary) {
        this.summary = summary;
    }

    public List<TimeReportEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TimeReportEntry> entries) {
        this.entries = entries;
    }

    public Map<String, Object> getGroupedData() {
        return groupedData;
    }

    public void setGroupedData(Map<String, Object> groupedData) {
        this.groupedData = groupedData;
    }

    public ReportMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(ReportMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return "TimeReportResponse{" +
                "reportId=" + reportId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", generatedAt=" + generatedAt +
                ", entriesCount=" + entries.size() +
                '}';
    }

    /**
     * SUMMARY Klasse
     */
    public static class ReportSummary {
        private String totalPlannedHours;
        private String totalActualHours;
        private String totalDifference;
        private long totalEntries;
        private long averageHoursPerDay;
        private double utilizationRate;

        /**
         * Konstruktor
         */
        public ReportSummary() {}

        public ReportSummary(String totalPlannedHours, String totalActualHours, String totalDifference) {
            this.totalPlannedHours = totalPlannedHours;
            this.totalActualHours = totalActualHours;
            this.totalDifference = totalDifference;
        }

        /**
         * Getter und Setter
         */
        public String getTotalPlannedHours() {
            return totalPlannedHours;
        }

        public void setTotalPlannedHours(String totalPlannedHours) {
            this.totalPlannedHours = totalPlannedHours;
        }

        public String getTotalActualHours() {
            return totalActualHours;
        }

        public void setTotalActualHours(String totalActualHours) {
            this.totalActualHours = totalActualHours;
        }

        public String getTotalDifference() {
            return totalDifference;
        }

        public void setTotalDifference(String totalDifference) {
            this.totalDifference = totalDifference;
        }

        public long getTotalEntries() {
            return totalEntries;
        }

        public void setTotalEntries(long totalEntries) {
            this.totalEntries = totalEntries;
        }

        public long getAverageHoursPerDay() {
            return averageHoursPerDay;
        }

        public void setAverageHoursPerDay(long averageHoursPerDay) {
            this.averageHoursPerDay = averageHoursPerDay;
        }

        public double getUtilizationRate() {
            return utilizationRate;
        }

        public void setUtilizationRate(double utilizationRate) {
            this.utilizationRate = utilizationRate;
        }
    }

    /**
     * Klasse fpr Zeiteberichts Einträge
     */
    public static class TimeReportEntry {
        private Long timeEntryId;
        private LocalDate date;
        private Long userId;
        private String userName;
        private Long projectId;
        private String projectName;
        private String actualHours;
        private String plannedHours;
        private String difference;

        /**
         * Konstruktor
         */
        public TimeReportEntry() {}

        public TimeReportEntry(Long timeEntryId, LocalDate date, String userName, String actualHours) {
           this.timeEntryId = timeEntryId;
           this.date = date;
           this.userName = userName;
           this.actualHours = actualHours;
        }

        /**
         * Getter und Setter
         */
        public Long getTimeEntryId() {
            return timeEntryId;
        }

        public void setTimeEntryId(Long timeEntryId) {
            this.timeEntryId = timeEntryId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
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
    }

    /**
     * Klasse für Metadaten des Zeitreports
     * Quelle: ChatGPT.com
     */
    public static class ReportMetaData {
        private long executionTimes;
        private int totalRecordsProcessed;
        private String query;
        private Map<String, String> filters;

        /**
         * Konstruktor
         */
        public ReportMetaData() {}

        public ReportMetaData(long executionTimes, int totalRecordsProcessed) {
            this.executionTimes = executionTimes;
            this.totalRecordsProcessed = totalRecordsProcessed;
        }

        /**
         * Getter und Setter
         */
        public long getExecutionTimes() {
            return executionTimes;
        }

        public void setExecutionTimes(long executionTimes) {
            this.executionTimes = executionTimes;
        }

        public int getTotalRecordsProcessed() {
            return totalRecordsProcessed;
        }

        public void setTotalRecordsProcessed(int totalRecordsProcessed) {
            this.totalRecordsProcessed = totalRecordsProcessed;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, String> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, String> filters) {
            this.filters = filters;
        }
    }
}
