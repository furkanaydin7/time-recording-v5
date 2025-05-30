package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.dto.absence.AbsenceRequest;
import ch.fhnw.timerecordingbackend.dto.absence.AbsenceResponse;
import ch.fhnw.timerecordingbackend.model.Absence;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.security.SecurityUtils;
import ch.fhnw.timerecordingbackend.service.AbsenceService;
import ch.fhnw.timerecordingbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/absences")
public class AbsenceController {

    private final AbsenceService absenceService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @Autowired
    public AbsenceController(AbsenceService absenceService, UserService userService, SecurityUtils securityUtils) {
        this.absenceService = absenceService;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    /**
     * Neue Abwesenheit erstellen
     * POST /api/absences
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAbsence(@Valid @RequestBody AbsenceRequest request) {
        // Aktuellen Benutzer ermitteln
        User currentUser = getCurrentUser();

        // Abwesenheit erstellen
        Absence absence = new Absence();
        absence.setUser(currentUser);
        absence.setStartDate(request.getStartDate());
        absence.setEndDate(request.getEndDate());
        absence.setType(request.getType());

        Absence createdAbsence = absenceService.createAbsence(absence);

        return new ResponseEntity<>(
                Map.of(
                        "id", createdAbsence.getId(),
                        "message", "Abwesenheit eingetragen"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * Abwesenheit aktualisieren
     * PUT /api/absences/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @absenceController.isAbsenceOwner(#id)")
    public ResponseEntity<Map<String, String>> updateAbsence(
            @PathVariable Long id,
            @Valid @RequestBody AbsenceRequest request) {

        // Bestehende Abwesenheit finden
        Absence existingAbsence = absenceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht gefunden mit ID: " + id));

        // Nur nicht genehmigte Abwesenheiten können bearbeitet werden
        if (existingAbsence.getStatus() == AbsenceStatus.APPROVED || existingAbsence.getStatus() == AbsenceStatus.REJECTED) {
            throw new IllegalArgumentException("Genehmigte oder abgelehnte Abwesenheiten können nicht direkt bearbeitet werden. Erstellen Sie ggf. einen neuen Antrag.");
        }

        // Aktualisierte Abwesenheit erstellen
        Absence updatedAbsence = new Absence();
        updatedAbsence.setUser(existingAbsence.getUser());
        updatedAbsence.setStartDate(request.getStartDate());
        updatedAbsence.setEndDate(request.getEndDate());
        updatedAbsence.setType(request.getType());

        absenceService.updateAbsence(id, updatedAbsence);

        return ResponseEntity.ok(Map.of("message", "Abwesenheit aktualisiert"));
    }

    /**
     * Abwesenheit löschen (nur Admin oder Besitzer unter Bedingungen)
     * DELETE /api/absences/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @absenceController.isAbsenceOwner(#id)")
    public ResponseEntity<Map<String, String>> deleteAbsence(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Absence absenceToDelete = absenceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht gefunden mit ID: " + id));

        // Wenn der Benutzer kein Admin ist, prüfen, ob die Abwesenheit stornierbar ist
        if (!currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            if (absenceToDelete.getStatus() == AbsenceStatus.APPROVED || absenceToDelete.getStatus() == AbsenceStatus.REJECTED) {
                throw new IllegalArgumentException("Genehmigte oder abgelehnte Abwesenheiten können nicht vom Mitarbeiter storniert werden.");
            }
        }

        absenceService.deleteAbsence(id);
        return ResponseEntity.ok(Map.of("message", "Abwesenheit gelöscht"));
    }

    /**
     * Eigene Abwesenheiten anzeigen
     * GET /api/absences
     */
    @GetMapping
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getCurrentUserAbsences() {
        User currentUser = getCurrentUser();
        List<Absence> absences = absenceService.findByUser(currentUser);

        List<AbsenceResponse> responses = absences.stream()
                .map(this::convertToAbsenceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Abwesenheiten eines bestimmten Benutzers anzeigen (nur Admin)
     * GET /api/users/{userId}/absences
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getUserAbsences(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden mit ID: " + userId));

        List<Absence> absences = absenceService.findByUser(user);

        List<AbsenceResponse> responses = absences.stream()
                .map(this::convertToAbsenceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Alle ausstehenden Abwesenheiten anzeigen (nur Admin/Manager)
     * GET /api/absences/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getPendingAbsences() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("absences", Collections.emptyList()));
        }
        List<Absence> pendingAbsences = absenceService.findPendingAbsences(currentUser);

        List<AbsenceResponse> responses = pendingAbsences.stream()
                .map(this::convertToAbsenceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Alle genehmigten Abwesenheiten anzeigen (nur Admin)
     * GET /api/absences/approved
     */
    @GetMapping("/approved")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getApprovedAbsences() {
        List<Absence> approvedAbsences = absenceService.findApprovedAbsences();

        List<AbsenceResponse> responses = approvedAbsences.stream()
                .map(this::convertToAbsenceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Abwesenheit genehmigen (nur Admin/Manager)
     * PATCH /api/absences/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN') or hasAnyAuthority('MANAGER')")
    public ResponseEntity<Map<String, Object>> approveAbsence(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        Absence approvedAbsence = absenceService.approveAbsence(id, currentUser.getId());

        return ResponseEntity.ok(Map.of(
                "approved", true,
                "message", "Abwesenheit genehmigt"
        ));
    }

    /**
     * Abwesenheit ablehnen (nur Admin/Manager)
     * PATCH /api/absences/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, String>> rejectAbsence(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        absenceService.rejectAbsence(id, currentUser.getId());

        return ResponseEntity.ok(Map.of("message", "Abwesenheit abgelehnt"));
    }

    /**
     * Aktuelle und zukünftige Abwesenheiten eines Benutzers
     * GET /api/absences/user/{userId}/upcoming
     */
    @GetMapping("/user/{userId}/upcoming")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getUpcomingAbsences(@PathVariable Long userId) {
        List<Absence> upcomingAbsences = absenceService.findCurrentAndFutureAbsencesByUserId(userId, LocalDate.now());

        List<AbsenceResponse> responses = upcomingAbsences.stream()
                .map(this::convertToAbsenceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Abwesenheiten nach Typ filtern (nur Admin)
     * GET /api/absences/type/{type}
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getAbsencesByType(@PathVariable String type) {
        try {
            ch.fhnw.timerecordingbackend.model.enums.AbsenceType absenceType =
                    ch.fhnw.timerecordingbackend.model.enums.AbsenceType.valueOf(type.toUpperCase());

            List<Absence> absences = absenceService.findByType(absenceType);

            List<AbsenceResponse> responses = absences.stream()
                    .map(this::convertToAbsenceResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("absences", responses));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültiger Abwesenheitstyp: " + type);
        }
    }

    /**
     * Prüft ob ein Benutzer an einem bestimmten Datum abwesend ist
     * GET /api/absences/check?userId={userId}&date={date}
     */
    @GetMapping("/check")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkAbsenceOnDate(
            @RequestParam Long userId,
            @RequestParam String date) {

        LocalDate checkDate = LocalDate.parse(date);
        boolean hasAbsence = absenceService.hasApprovedAbsenceOnDate(userId, checkDate);

        return ResponseEntity.ok(Map.of("hasAbsence", hasAbsence));
    }

    /**
     * Hilfsmethode zur Überprüfung ob aktueller Benutzer Besitzer der Abwesenheit ist
     * @param absenceId Abwesenheits-ID
     * @return true wenn Benutzer Besitzer der Abwesenheit ist
     */
    public boolean isAbsenceOwner(Long absenceId) {
        User currentUser = getCurrentUser();

        return absenceService.findById(absenceId)
                .map(absence -> absence.getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    /**
     * Alle genehmigten Abwesenheiten für Admins oder die des eigenen Teams für Manager anzeigen.
     * GET /api/absences/view/approved
     */
    @GetMapping("/view/approved")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, List<AbsenceResponse>>> getAllApprovedAbsencesForView() {
        User currentUser = getCurrentUser(); // Diese Methode nutzt SecurityContextHolder
        List<Absence> absences = absenceService.getApprovedAbsencesForUserView(currentUser);

        List<AbsenceResponse> responses = absences.stream()
                .map(this::convertToAbsenceResponse) // Ihre bestehende Konvertierungsmethode
                .collect(Collectors.toList());

        String title = currentUser.hasRole("ADMIN") ? "Alle genehmigten Abwesenheiten" : "Genehmigte Abwesenheiten (Team)";

        return ResponseEntity.ok(Map.of("absences", responses));
    }

    /**
     * Hilfsmethode zum Ermitteln des aktuellen Benutzers
     * @return Aktueller Benutzer
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        return userService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Aktueller Benutzer nicht gefunden"));
    }

    /**
     * Konvertiert ein Absence-Objekt zu einem AbsenceResponse-DTO
     * @param absence Das zu konvertierende Absence-Objekt
     * @return AbsenceResponse-DTO
     */
    private AbsenceResponse convertToAbsenceResponse(Absence absence) {
        AbsenceResponse response = new AbsenceResponse();

        // Basis-Informationen
        response.setId(absence.getId());
        response.setStartDate(absence.getStartDate());
        response.setEndDate(absence.getEndDate());
        response.setType(absence.getType());
        response.setStatus(absence.getStatus());
        response.setCreatedAt(absence.getCreatedAt());
        response.setUpdatedAt(absence.getUpdatedAt());

        // Benutzer-Informationen
        if (absence.getUser() != null) {
            response.setUserId(absence.getUser().getId());
            response.setFirstName(absence.getUser().getFirstName());
            response.setLastName(absence.getUser().getLastName());
            response.setEmail(absence.getUser().getEmail());
        }

        return response;
    }
}
