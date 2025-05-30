package ch.fhnw.timerecordingbackend.dto.backup;

import ch.fhnw.timerecordingbackend.dto.absence.AbsenceResponse;
import ch.fhnw.timerecordingbackend.dto.admin.UserResponse;
import ch.fhnw.timerecordingbackend.dto.project.ProjectResponse;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datenstruktur für Backup-Inhalte.
 * Enthält Benutzer, Projekte, Zeiteinträge und Abwesenheiten mit Zeitstempel.
 * Wird für Export und Wiederherstellung verwendet.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
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
