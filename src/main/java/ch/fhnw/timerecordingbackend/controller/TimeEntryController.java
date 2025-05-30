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
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    @Autowired
    private TimeEntryService timeEntryService;

    @PostMapping
    public ResponseEntity<TimeEntryResponse> createTimeEntry(@Valid @RequestBody TimeEntryRequest timeEntryRequest) {
        return ResponseEntity.ok(timeEntryService.createTimeEntry(timeEntryRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeEntry(
            @PathVariable Long id,
            @Valid @RequestBody TimeEntryRequest timeEntryRequest) {
        timeEntryService.updateTimeEntry(id, timeEntryRequest);
        return ResponseEntity.ok().body(Map.of("message", "Eintrag aktualisiert"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@timeEntryServiceImpl.isOwnerOfTimeEntry(#id) or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.ok().body(Map.of("message", "Eintrag gelöscht"));
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getCurrentUserTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getCurrentUserTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    /**
     * Zeiteinträge eines beliebigen Users abrufen
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TimeEntryResponse>> getUserTimeEntries(@PathVariable Long userId) {
        List<TimeEntryResponse> entries = timeEntryService.getUserTimeEntries(userId);
        return ResponseEntity.ok(entries);
    }

    /**
     * Zeiteinträge des Teams eines Managers abrufen (Nur Manager)
     */
    @GetMapping("/team")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getTeamTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getTeamTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    /**
     * Alle Zeiteinträge abrufen (Nur Admin)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, List<TimeEntryResponse>>> getAllTimeEntries() {
        List<TimeEntryResponse> entries = timeEntryService.getAllTimeEntries();
        return ResponseEntity.ok(Map.of("entries", entries));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startTimeTracking(@RequestBody(required = false) Map<String, Long> body) {
        Long projectId = body != null ? body.get("projectId") : null;
        return ResponseEntity.ok(timeEntryService.startTimeTracking(projectId));
    }
    @PostMapping("/{entryId}/stop")
    public ResponseEntity<?> stopTimeTracking(@PathVariable Long entryId) {
        return ResponseEntity.ok(timeEntryService.stopTimeTracking(entryId));
    }

    @PostMapping("/{id}/assign-project")
    public ResponseEntity<?> assignProject(
            @PathVariable Long id,
            @RequestBody Map<String, Long> requestBody) {
        timeEntryService.assignProject(id, requestBody.get("projectId"));
        return ResponseEntity.ok().body(Map.of(
                "message", "Zeiteintrag zu Projekt zugewiesen"));
    }
}