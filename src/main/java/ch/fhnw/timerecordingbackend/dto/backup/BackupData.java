package ch.fhnw.timerecordingbackend.dto.backup;

import ch.fhnw.timerecordingbackend.dto.absence.AbsenceResponse;
import ch.fhnw.timerecordingbackend.dto.admin.UserResponse;
import ch.fhnw.timerecordingbackend.dto.project.ProjectResponse;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für Backup-Daten, enthält alle exportierbaren Entitäten inklusive Zeitstempel.
 * Verwendet für den Export und die Wiederherstellung von Systemzuständen.
 * Beinhaltet Benutzer-, Projekt-, Zeiteintrags- und Abwesenheitsdaten.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public class BackupData {
    private LocalDateTime backupTimestamp;
    private List<UserResponse> users;
    private List<ProjectResponse> projects;
    private List<TimeEntryResponse> timeEntries;
    private List<AbsenceResponse> absences;

    // Getters and Setters
    public LocalDateTime getBackupTimestamp() {
        return backupTimestamp;
    }

    public void setBackupTimestamp(LocalDateTime backupTimestamp) {
        this.backupTimestamp = backupTimestamp;
    }

    public List<UserResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }

    public List<ProjectResponse> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectResponse> projects) {
        this.projects = projects;
    }

    public List<TimeEntryResponse> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(List<TimeEntryResponse> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public List<AbsenceResponse> getAbsences() {
        return absences;
    }

    public void setAbsences(List<AbsenceResponse> absences) {
        this.absences = absences;
    }
}
