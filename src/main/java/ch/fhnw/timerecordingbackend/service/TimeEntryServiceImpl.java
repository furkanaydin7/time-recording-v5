package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;
import ch.fhnw.timerecordingbackend.model.Project;
import ch.fhnw.timerecordingbackend.model.TimeEntry;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.ProjectRepository;
import ch.fhnw.timerecordingbackend.repository.TimeEntryRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository; // Import UserRepository
import ch.fhnw.timerecordingbackend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-Implementierung für Zeiterfassung und Zeiteintragsverwaltung.
 * Bietet CRUD-Operationen, Start/Stopp-Tracking und Projektzuordnung für Zeiteinträge.
 * Implementiert Geschäftslogik, Validierung und Ausnahmebehandlung.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 * Quelle: ChatGPT
 */
@Service
@Transactional
public class TimeEntryServiceImpl implements TimeEntryService {

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired // UserRepository hinzugefügt für getCurrentUserOrThrow und andere Benutzeroperationen
    private UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Prüft, ob der aktuell angemeldete Nutzer Eigentümer eines bestimmten Zeiteintrags ist.
     * @param timeEntryId ID des Zeiteintrags
     * @return true, wenn der Nutzer Eigentümer ist, sonst false
     */
    public boolean isOwnerOfTimeEntry(Long timeEntryId) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return timeEntryRepository.findById(timeEntryId)
                .map(timeEntry -> timeEntry.getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    @Override
    public TimeEntryResponse createTimeEntry(TimeEntryRequest request) {
        User currentUser = getCurrentUserOrThrow();

        // Verhindere doppelte Einträge am selben Datum
        Optional<TimeEntry> existingEntry = timeEntryRepository.findByUserAndDate(currentUser, request.getDate());
        if (existingEntry.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Für dieses Datum existiert bereits ein Zeiteintrag");
        }

        validateTimeData(request); // Validierung von Start-/Endzeiten und Pausen

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setUser(currentUser);
        timeEntry.setDate(request.getDate());

        setTimesFromRequest(timeEntry, request); // Zeiten und Pausen initial setzen

        // Falls Pausen angegeben, diese parsen und hinzufügen
        if (request.getBreaks() != null) {
            for (TimeEntryRequest.BreakTime breakTime : request.getBreaks()) {
                try {
                    LocalTime start = LocalTime.parse(breakTime.getStart(), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(breakTime.getEnd(), TIME_FORMATTER);
                    timeEntry.addBreak(start, end);
                } catch (DateTimeParseException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ungültiges Pausenzeit-Format: " + e.getMessage());
                }
            }
        }

        // Arbeitszeiten und Differenz berechnen
        calculateWorkingHours(timeEntry, currentUser);

        // Projektzuordnung, falls ID übergeben
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Projekt nicht gefunden"));
            timeEntry.setProject(project);
        }

        // Timestamps setzen
        timeEntry.setCreatedAt(LocalDateTime.now());
        timeEntry.setUpdatedAt(LocalDateTime.now());

        // Persistiere den Eintrag und konvertiere zu DTO
        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        return convertToResponse(savedEntry);
    }

    @Override
    public void updateTimeEntry(Long id, TimeEntryRequest request) {
        User currentUser = getCurrentUserOrThrow();
        TimeEntry timeEntry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zeiteintrag nicht gefunden"));

        // Berechtigung prüfen
        // Ein Admin kann jeden Eintrag bearbeiten.
        // Ein normaler Benutzer kann nur seine eigenen Einträge bearbeiten.
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN")); //

        if (!timeEntry.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sie können nur Ihre eigenen Zeiteinträge bearbeiten oder benötigen Admin-Rechte.");
        }


        // Validierung der neuen Zeitangaben
        validateTimeData(request);

        // Zeiten aktualisieren
        timeEntry.getStartTimes().clear();
        timeEntry.getEndTimes().clear();
        timeEntry.getBreaks().clear();

        setTimesFromRequest(timeEntry, request);

        // Pausen hinzufügen
        if (request.getBreaks() != null) {
            for (TimeEntryRequest.BreakTime breakTime : request.getBreaks()) {
                try {
                    LocalTime start = LocalTime.parse(breakTime.getStart(), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(breakTime.getEnd(), TIME_FORMATTER);
                    timeEntry.addBreak(start, end);
                } catch (DateTimeParseException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ungültiges Pausenzeit-Format: " + e.getMessage());
                }
            }
        }
        // Arbeitszeiten neu berechnen
        calculateWorkingHours(timeEntry, timeEntry.getUser());

        // Projekt aktualisieren
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Projekt nicht gefunden"));
            timeEntry.setProject(project);
        } else {
            timeEntry.setProject(null);
        }

        // UpdatedAt anpassen und speichern
        timeEntry.setUpdatedAt(LocalDateTime.now());
        timeEntryRepository.save(timeEntry);
    }

    @Override
    public void deleteTimeEntry(Long id) {
        // Existenz prüfen und löschen
        TimeEntry timeEntry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zeiteintrag nicht gefunden"));
        timeEntryRepository.delete(timeEntry);
    }

    @Override
    public List<TimeEntryResponse> getCurrentUserTimeEntries() {
        // Alle Zeiteinträge des aktuellen Nutzers laden
        User currentUser = getCurrentUserOrThrow();
        List<TimeEntry> entries = timeEntryRepository.findByUser(currentUser);
        return entries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntryResponse> getUserTimeEntries(Long userId) {
        // Benutzer anhand ID laden
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden: " + userId));
        List<TimeEntry> entries = timeEntryRepository.findByUser(user); //
        return entries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntryResponse> getTeamTimeEntries() {
        // Alle Teammitglieder des Managers laden
        User currentManager = getCurrentUserOrThrow();
        List<User> teamMembers = userRepository.findByManager(currentManager); //
        if (teamMembers.isEmpty()) {
            return Collections.emptyList();
        }
        // Sammle alle Einträge der Teammitglieder
        List<TimeEntry> teamEntries = new ArrayList<>();
        for (User member : teamMembers) {
            teamEntries.addAll(timeEntryRepository.findByUser(member)); //
        }
        // Sortierung nach Datum und Benutzer
        return teamEntries.stream()
                .map(this::convertToResponse)
                .sorted(Comparator.comparing(TimeEntryResponse::getDate).reversed().thenComparing(TimeEntryResponse::getUser))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntryResponse> getAllTimeEntries() {
        // Liefert alle Einträge (Admin-Funktion)
        List<TimeEntry> allEntries = timeEntryRepository.findAll();
        return allEntries.stream()
                .map(this::convertToResponse)
                .sorted(Comparator.comparing(TimeEntryResponse::getDate).reversed().thenComparing(TimeEntryResponse::getUser))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> startTimeTracking(Long projectId) {
        // Startet die Zeiterfassung für heute
        User currentUser = getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<TimeEntry> existingEntryOpt = timeEntryRepository.findByUserAndDate(currentUser, today);
        TimeEntry timeEntry;

        if (existingEntryOpt.isPresent()) {
            timeEntry = existingEntryOpt.get();
            if (timeEntry.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Zeiterfassung läuft bereits für diesen Eintrag");
            }
        } else {
            // Neuer Eintrag für heute
            timeEntry = new TimeEntry();
            timeEntry.setUser(currentUser);
            timeEntry.setDate(today);
            timeEntry.setCreatedAt(LocalDateTime.now());
            calculateWorkingHours(timeEntry, currentUser); // Initialwerte setzen
        }

        // Projektzuordnung optional
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Projekt nicht gefunden"));
            timeEntry.setProject(project);
        } else if (existingEntryOpt.isEmpty()) {
            timeEntry.setProject(null);
        }

        // Aktuelle Zeit als Startzeit hinzufügen
        timeEntry.addStartTime(now);
        timeEntry.setUpdatedAt(LocalDateTime.now());

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

        // Antwort mit Entry-Details
        Map<String, Object> response = new HashMap<>();
        response.put("entryId", savedEntry.getId());
        response.put("startTime", LocalDateTime.of(today, now).toString());
        response.put("message", "Zeiterfassung gestartet");
        if (savedEntry.getProject() != null) {
            response.put("projectId", savedEntry.getProject().getId());
            response.put("projectName", savedEntry.getProject().getName());
        } else {
            response.put("projectId", null);
            response.put("projectName", null);
        }
        return response;
    }

    @Override
    public Map<String, Object> stopTimeTracking(Long entryId) {
        User currentUser = getCurrentUserOrThrow();
        TimeEntry timeEntry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zeiteintrag nicht gefunden"));

        if (!timeEntry.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sie können nur Ihre eigenen Zeiteinträge stoppen");
        }

        if (!timeEntry.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Keine aktive Zeiterfassung für diesen Eintrag");
        }

        LocalTime now = LocalTime.now();
        timeEntry.addEndTime(now);

        // Arbeitszeiten neu berechnen und speichern
        calculateWorkingHours(timeEntry, currentUser);
        timeEntry.setUpdatedAt(LocalDateTime.now());

        timeEntryRepository.save(timeEntry);

        // Antwort mit aktualisierten Zeiten
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Zeiterfassung gestoppt");
        response.put("endTime", LocalDateTime.of(timeEntry.getDate(), now).toString());
        response.put("actualHours", timeEntry.getActualHours());
        response.put("difference", timeEntry.getDifference());
        if (timeEntry.getProject() != null) {
            response.put("projectId", timeEntry.getProject().getId());
            response.put("projectName", timeEntry.getProject().getName());
        }
        return response;
    }

    @Override
    public void assignProject(Long timeEntryId, Long projectId) {
        // Weist einem bestehenden Eintrag ein Projekt zu oder entfernt es
        TimeEntry timeEntry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zeiteintrag nicht gefunden"));

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Projekt nicht gefunden"));
            timeEntry.setProject(project);
        } else {
            timeEntry.setProject(null);
        }

        timeEntry.setUpdatedAt(LocalDateTime.now());
        timeEntryRepository.save(timeEntry);
    }

    /**
     * Liefert den aktuellen Nutzer oder wirft UNAUTHORIZED.
     */
    private User getCurrentUserOrThrow() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht authentifiziert");
        }
        // Optional: Benutzer neu aus der DB laden, um sicherzustellen, dass er aktuell ist
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht in DB gefunden"));
    }

    /**
     * Validiert Start- und Endzeiten bezüglich Format und Konsistenz.
     */
    private void validateTimeData(TimeEntryRequest request) {
        if (request.getStartTimes() == null || request.getStartTimes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Mindestens eine Startzeit ist erforderlich");
        }
        if (request.getEndTimes() != null && request.getEndTimes().size() > request.getStartTimes().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ungültige Anzahl von Endzeiten im Verhältnis zu Startzeiten");
        }
        // Prüfe jedes Startzeit-Format
        for (String timeStr : request.getStartTimes()) {
            try {
                LocalTime.parse(timeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ungültiges Startzeit-Format: " + timeStr + ". Erwartet HH:mm");
            }
        }
        // Prüfe jedes Endzeit-Format
        if (request.getEndTimes() != null) {
            for (String timeStr : request.getEndTimes()) {
                if (timeStr != null && !timeStr.isEmpty()) {
                    try {
                        LocalTime.parse(timeStr, TIME_FORMATTER);
                    } catch (DateTimeParseException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ungültiges Endzeit-Format: " + timeStr + ". Erwartet HH:mm");
                    }
                }
            }
        }
    }

    /**
     * Überträgt String-Zeitlisten in LocalTime-Sets im Model.
     */
    private void setTimesFromRequest(TimeEntry timeEntry, TimeEntryRequest request) {
        if (request.getStartTimes() != null) {
            timeEntry.setStartTimes(request.getStartTimes().stream()
                    .map(timeStr -> LocalTime.parse(timeStr, TIME_FORMATTER))
                    .collect(Collectors.toSet()));
        } else {
            timeEntry.setStartTimes(new HashSet<>());
        }

        if (request.getEndTimes() != null) {
            timeEntry.setEndTimes(request.getEndTimes().stream()
                    .filter(s -> s != null && !s.isEmpty()) // Nur nicht-leere Strings parsen
                    .map(timeStr -> LocalTime.parse(timeStr, TIME_FORMATTER))
                    .collect(Collectors.toSet()));
        } else {
            timeEntry.setEndTimes(new HashSet<>());
        }
    }

    /**
     * Berechnet tatsächliche Arbeitszeit, geplante Stunden und Differenz.
     */
    private void calculateWorkingHours(TimeEntry timeEntry, User user) {
        double plannedHoursPerDay = user.getPlannedHoursPerDay();
        double totalMinutes = 0;

        // Sortiere Zeiten und gleiche Paare ab
        List<LocalTime> startTimes = (timeEntry.getStartTimes() != null)
                ? new ArrayList<>(timeEntry.getStartTimes())
                : new ArrayList<>();
        List<LocalTime> endTimes = (timeEntry.getEndTimes() != null)
                ? new ArrayList<>(timeEntry.getEndTimes())
                : new ArrayList<>();

        startTimes.sort(LocalTime::compareTo);
        endTimes.sort(LocalTime::compareTo);

        int pairs = Math.min(startTimes.size(), endTimes.size());
        for (int i = 0; i < pairs; i++) {
            LocalTime start = startTimes.get(i);
            LocalTime end = endTimes.get(i);
            if (end.isAfter(start)) {
                totalMinutes += java.time.Duration.between(start, end).toMinutes();
            }
        }

        // Pausen abziehen
        if (timeEntry.getBreaks() != null) {
            for (TimeEntry.Break breakTime : timeEntry.getBreaks()) {
                if (breakTime.getStart() != null && breakTime.getEnd() != null && breakTime.getEnd().isAfter(breakTime.getStart())) {
                    totalMinutes -= java.time.Duration.between(breakTime.getStart(), breakTime.getEnd()).toMinutes();
                }
            }
        }

        // Formatiere Stunden:Minuten
        long hours = (long) (totalMinutes / 60);
        long minutes = (long) (Math.round(totalMinutes) % 60);
        String actualHours = String.format("%02d:%02d", hours, minutes);

        // Berechne geplante Stunden als String
        long plannedHours = (long) plannedHoursPerDay;
        long plannedMinutes = (long) Math.round((plannedHoursPerDay - plannedHours) * 60);
        String plannedHoursStr = String.format("%02d:%02d", plannedHours, plannedMinutes);

        // Differenz berechnen und formatieren
        double differenceInMinutes = totalMinutes - (plannedHoursPerDay * 60);
        long diffAbsMinutes = (long) Math.abs(Math.round(differenceInMinutes));
        long diffH = diffAbsMinutes / 60;
        long diffM = diffAbsMinutes % 60;
        String differenceStr = String.format("%s%02d:%02d",
                differenceInMinutes >= 0 ? "+" : "-", diffH, diffM);

        timeEntry.setActualHours(actualHours);
        timeEntry.setPlannedHours(plannedHoursStr);
        timeEntry.setDifference(differenceStr);
    }

    /**
     * Konvertiert Entity in Response-DTO inklusive Formatierungen.
     */
    private TimeEntryResponse convertToResponse(TimeEntry timeEntry) {
        TimeEntryResponse response = new TimeEntryResponse();
        response.setId(timeEntry.getId());
        response.setDate(timeEntry.getDate());
        response.setActualHours(timeEntry.getActualHours());
        response.setPlannedHours(timeEntry.getPlannedHours());
        response.setDifference(timeEntry.getDifference());
        response.setUserId(timeEntry.getUser().getId());
        response.setUser(timeEntry.getUser().getFirstName() + " " + timeEntry.getUser().getLastName());

        // Start- und Endzeiten als sortierte Listen
        if (timeEntry.getStartTimes() != null) {
            response.setStartTimes(timeEntry.getStartTimes().stream()
                    .sorted()
                    .map(time -> time.format(TIME_FORMATTER))
                    .collect(Collectors.toList()));
        } else {
            response.setStartTimes(Collections.emptyList());
        }

        if (timeEntry.getEndTimes() != null) {
            response.setEndTimes(timeEntry.getEndTimes().stream()
                    .sorted()
                    .map(time -> time.format(TIME_FORMATTER))
                    .collect(Collectors.toList()));
        } else {
            response.setEndTimes(Collections.emptyList());
        }

        // Pausen transformieren
        if (timeEntry.getBreaks() != null) {
            response.setBreaks(timeEntry.getBreaks().stream()
                    .map(breakTime -> {
                        TimeEntryResponse.BreakTime bt = new TimeEntryResponse.BreakTime();
                        if (breakTime.getStart() != null) {
                            bt.setStart(breakTime.getStart().format(TIME_FORMATTER));
                        }
                        if (breakTime.getEnd() != null) {
                            bt.setEnd(breakTime.getEnd().format(TIME_FORMATTER));
                        }
                        return bt;
                    })
                    .collect(Collectors.toList()));
        } else {
            response.setBreaks(Collections.emptyList());
        }

        // Projektdaten hinzufügen, falls vorhanden
        if (timeEntry.getProject() != null) {
            response.setProject(new TimeEntryResponse.ProjectDto(
                    timeEntry.getProject().getId(),
                    timeEntry.getProject().getName()
            ));
        }
        return response;
    }
}