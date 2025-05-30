package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.absence.AbsenceResponse;
import ch.fhnw.timerecordingbackend.dto.admin.UserResponse;
import ch.fhnw.timerecordingbackend.dto.backup.BackupData;
import ch.fhnw.timerecordingbackend.dto.project.ProjectResponse;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;
import ch.fhnw.timerecordingbackend.model.*;
import ch.fhnw.timerecordingbackend.repository.AbsenceRepository;
import ch.fhnw.timerecordingbackend.repository.ProjectRepository;
import ch.fhnw.timerecordingbackend.repository.TimeEntryRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementierung des BackupService zur Erstellung von JSON-Backups.
 * Sichert Benutzer, Projekte, Zeiteinträge und Abwesenheiten mit Zeitstempel.
 * Nutzt ObjectMapper für Serialisierung und speichert im definierten Verzeichnis.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final AbsenceRepository absenceRepository;
    private final ObjectMapper objectMapper;

    @Value("${backup.storage.path}")
    private String backupStoragePath;

    public BackupServiceImpl(UserRepository userRepository,
                             ProjectRepository projectRepository,
                             TimeEntryRepository timeEntryRepository,
                             AbsenceRepository absenceRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.absenceRepository = absenceRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Wichtig für LocalDateTime Serialisierung
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @Transactional(readOnly = true) // Lesende Transaktion für Konsistenz
    public String createBackup() throws IOException {
        logger.info("Backup-Prozess gestartet...");

        BackupData backupData = new BackupData();
        backupData.setBackupTimestamp(LocalDateTime.now());

        // Benutzerdaten sammeln
        List<User> users = userRepository.findAll();
        backupData.setUsers(users.stream().map(this::convertToUserResponse).collect(Collectors.toList()));
        logger.info("{} Benutzer gefunden.", users.size());

        // Projektdaten sammeln
        List<Project> projects = projectRepository.findAll();
        backupData.setProjects(projects.stream().map(this::convertToProjectResponse).collect(Collectors.toList()));
        logger.info("{} Projekte gefunden.", projects.size());

        // Zeiteintragsdaten sammeln
        List<TimeEntry> timeEntries = timeEntryRepository.findAll();
        backupData.setTimeEntries(timeEntries.stream().map(this::convertToTimeEntryResponse).collect(Collectors.toList()));
        logger.info("{} Zeiteinträge gefunden.", timeEntries.size());

        // Abwesenheitsdaten sammeln
        List<Absence> absences = absenceRepository.findAll();
        backupData.setAbsences(absences.stream().map(this::convertToAbsenceResponse).collect(Collectors.toList()));
        logger.info("{} Abwesenheiten gefunden.", absences.size());

        // Backup-Datei erstellen
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "timerecording_backup_" + timestamp + ".json";
        Path backupDir = Paths.get(backupStoragePath);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            logger.info("Backup-Verzeichnis erstellt: {}", backupDir.toAbsolutePath());
        }
        Path backupFilePath = backupDir.resolve(filename);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFilePath.toFile(), backupData);
            logger.info("Backup erfolgreich erstellt: {}", backupFilePath.toAbsolutePath());
            return backupFilePath.toAbsolutePath().toString();
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben der Backup-Datei: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Konvertierungsmethoden (ähnlich zu den Controllern, hier zentralisiert)
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());
        response.setStatus(user.getStatus());
        response.setPlannedHoursPerDay(user.getPlannedHoursPerDay());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private ProjectResponse convertToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setActive(project.isActive());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        if (project.getManager() != null) {
            ProjectResponse.ProjectStatistics stats = new ProjectResponse.ProjectStatistics();
            // Hier könnten bei Bedarf Manager-Infos oder andere Details hinzugefügt werden
            response.setStatistics(stats); // Dummy-Statistik, um NullPointer zu vermeiden
        }
        return response;
    }

    private TimeEntryResponse convertToTimeEntryResponse(TimeEntry timeEntry) {
        TimeEntryResponse response = new TimeEntryResponse();
        response.setId(timeEntry.getId());
        response.setDate(timeEntry.getDate());
        response.setStartTimes(timeEntry.getStartTimes().stream().map(t -> t.format(DateTimeFormatter.ISO_LOCAL_TIME)).collect(Collectors.toList()));
        response.setEndTimes(timeEntry.getEndTimes().stream().map(t -> t.format(DateTimeFormatter.ISO_LOCAL_TIME)).collect(Collectors.toList()));
        response.setBreaks(timeEntry.getBreaks().stream().map(b -> {
            TimeEntryResponse.BreakTime bt = new TimeEntryResponse.BreakTime();
            bt.setStart(b.getStart().format(DateTimeFormatter.ISO_LOCAL_TIME));
            bt.setEnd(b.getEnd().format(DateTimeFormatter.ISO_LOCAL_TIME));
            return bt;
        }).collect(Collectors.toList()));
        response.setActualHours(timeEntry.getActualHours());
        response.setPlannedHours(timeEntry.getPlannedHours());
        response.setDifference(timeEntry.getDifference());
        response.setUserId(timeEntry.getUser().getId());
        response.setUser(timeEntry.getUser().getFullName());
        if (timeEntry.getProject() != null) {
            response.setProject(new TimeEntryResponse.ProjectDto(timeEntry.getProject().getId(), timeEntry.getProject().getName()));
        }
        return response;
    }

    private AbsenceResponse convertToAbsenceResponse(Absence absence) {
        AbsenceResponse response = new AbsenceResponse();
        response.setId(absence.getId());
        response.setStartDate(absence.getStartDate());
        response.setEndDate(absence.getEndDate());
        response.setType(absence.getType());
        response.setStatus(absence.getStatus());
        response.setCreatedAt(absence.getCreatedAt());
        response.setUpdatedAt(absence.getUpdatedAt());
        response.setUserId(absence.getUser().getId());
        response.setFirstName(absence.getUser().getFirstName());
        response.setLastName(absence.getUser().getLastName());
        response.setEmail(absence.getUser().getEmail());
        if (absence.getApprover() != null) {
            response.setProcessedById(absence.getApprover().getId());
            response.setProcessedByName(absence.getApprover().getFullName());
        }
        return response;
    }
}
