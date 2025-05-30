package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;
import ch.fhnw.timerecordingbackend.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller für Zeiteinträge
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 * @version 1.0
 */
@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    @Autowired
    private TimeEntryService timeEntryService;

    /**
     * Erzeugt einen neuen Zeiteintrag.
     * @param timeEntryRequest DTO mit den Daten des neuen Eintrags
     * @return neu erstellter Eintrag als Response
     */
    @PostMapping
    public ResponseEntity<TimeEntryResponse> createTimeEntry(
            @Valid @RequestBody TimeEntryRequest timeEntryRequest) {
        return ResponseEntity.ok(timeEntryService.createTimeEntry(timeEntryRequest));
    }

    /**
     * Aktualisiert einen bestehenden Zeiteintrag.
     * @param id ID des zu aktualisierenden Eintrags
     * @param timeEntryRequest DTO mit den neuen Daten
     * @return Bestätigung, dass der Eintrag aktualisiert wurde
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeEntry(
            @PathVariable Long id,
            @Valid @RequestBody TimeEntryRequest timeEntryRequest) {
        timeEntryService.updateTimeEntry(id, timeEntryRequest);
        return ResponseEntity.ok().body(Map.of("message", "Eintrag aktualisiert"));
    }

    /**
     * Löscht einen Zeiteintrag.
     * Nur der Besitzer des Eintrags oder ein Admin darf löschen.
     * @param id ID des zu löschenden Eintrags
     * @return Bestätigung, dass der Eintrag gelöscht wurde
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@timeEntryServiceImpl.isOwnerOfTimeEntry(#id) or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.ok().body(Map.of("message", "Eintrag gelöscht"));
    }

    /**
     * Holt alle Zeiteinträge des aktuell angemeldeten Nutzers.
     * @return Liste der Einträge im JSON-Format unter dem Schlüssel 'entries'
     */
    @GetMapping
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getCurrentUserTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getCurrentUserTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    /**
     * Holt Zeiteinträge eines beliebigen Nutzers.
     * Nur Admins haben Zugriff auf diese Operation.
     * @param userId ID des Nutzers
     * @return Liste der Zeiteinträge
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TimeEntryResponse>> getUserTimeEntries(@PathVariable Long userId) {
        List<TimeEntryResponse> entries = timeEntryService.getUserTimeEntries(userId);
        return ResponseEntity.ok(entries);
    }

    /**
     * Holt Zeiteinträge des Teams des angemeldeten Managers.
     * Zugriff nur für Nutzer mit der Rolle MANAGER.
     * @return Liste der Einträge im JSON-Format unter dem Schlüssel 'entries'
     */
    @GetMapping("/team")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getTeamTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getTeamTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    /**
     * Holt alle Zeiteinträge aller Nutzer.
     * Zugriff nur für Admins.
     * @return Liste aller Zeiteinträge im JSON-Format unter dem Schlüssel 'entries'
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getAllTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getAllTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    /**
     * Startet die Zeiterfassung.
     * Optional kann eine Projekt-ID angegeben werden.
     * @param body JSON-Body mit optionaler projectId
     * @return gestartete Zeiterfassung als Response
     */
    @PostMapping("/start")
    public ResponseEntity<?> startTimeTracking(@RequestBody(required = false) Map<String, Long> body) {
        Long projectId = body != null ? body.get("projectId") : null;
        return ResponseEntity.ok(timeEntryService.startTimeTracking(projectId));
    }

    /**
     * Beendet die Zeiterfassung eines vorhandenen Eintrags.
     * @param entryId ID des zu stoppenden Eintrags
     * @return Beendete Zeiterfassung als Response
     */
    @PostMapping("/{entryId}/stop")
    public ResponseEntity<?> stopTimeTracking(@PathVariable Long entryId) {
        return ResponseEntity.ok(timeEntryService.stopTimeTracking(entryId));
    }

    /**
     * Weist einen Zeiteintrag einem Projekt zu.
     * @param id ID des Zeiteintrags
     * @param requestBody JSON-Body mit projectId
     * @return Bestätigung der Zuweisung
     */
    @PostMapping("/{id}/assign-project")
    public ResponseEntity<?> assignProject(
            @PathVariable Long id,
            @RequestBody Map<String, Long> requestBody) {
        timeEntryService.assignProject(id, requestBody.get("projectId"));
        return ResponseEntity.ok().body(Map.of(
                "message", "Zeiteintrag zu Projekt zugewiesen"));
    }
}