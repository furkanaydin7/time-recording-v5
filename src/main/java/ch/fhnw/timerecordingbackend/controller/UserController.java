package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.dto.authentication.ChangePasswordRequest;
import ch.fhnw.timerecordingbackend.dto.authentication.ResetPasswordRequest;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * REST-Controller für Benutzer-Operationen (Passwort ändern & Passwort zurücksetzen).
 * Behandelt Anfragen an /api/users.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 * @version 1.1 - sendpasswortresetlink entfernt - PD
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemLogRepository systemLogRepository;

    /**
     * Ändert das Passwort des aktuell angemeldeten Nutzers.
     * @param request DTO mit aktuellem und neuem Passwort
     * @return ResponseEntity mit einer Erfolgsmeldung
     */
    @PutMapping( "/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // Validierung der Anfrage durch @Valid und Delegation an den Service
        userService.changePassword(request);
        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Passwort geändert")
        );
    }

    /**
     * Fordert das Zurücksetzen des Passworts an.
     * Wenn ein Benutzer mit der angegebenen E-Mail existiert,
     * wird ein Log-Eintrag erstellt und die Anfrage an den Administrator weitergeleitet.
     * @param request DTO mit E-Mail-Adresse des Nutzers
     * @return ResponseEntity mit einer Hinweisnachricht
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        // Bei unbekannter E-Mail bleibt die Antwort unverfänglich
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok().body(Map.of("message", "Wenn ein Konto mit dieser E-Mail-Adresse existiert, wurde Ihre Anfrage verarbeitet."));
        }

        // Benutzer existiert: Erzeuge neuen System-Log-Eintrag
        User user = userOptional.get();
        SystemLog log = new SystemLog();
        log.setAction("Passwort Reset angefordert");
        log.setTimestamp(LocalDateTime.now());
        log.setUserEmail(user.getEmail());
        log.setUserId(user.getId());
        log.setDetails("Passwort-Reset-Anfrage von Login-Seite für Benutzer: " + user.getEmail());
        log.setProcessedStatus("PENDING");

        SystemLog savedLog = systemLogRepository.save(log);

        // Debug-Ausgaben zur Kontrolle des gespeicherten Log-Status
        System.out.println("DEBUG UserController: SystemLog nach save(): ID=" + savedLog.getId() +
                ", Action=\"" + savedLog.getAction() + "\"" +
                ", ProcessedStatus=\"" + savedLog.getProcessedStatus() + "\"" +
                ", UserID=" + savedLog.getUserId());

        // Warnung, falls der Status nicht wie erwartet gesetzt wurde
        if (savedLog.getProcessedStatus() == null) {
            System.out.println("WARNUNG UserController: ProcessedStatus ist NULL direkt nach dem Speichern!");
        } else if (!savedLog.getProcessedStatus().equals("PENDING")) {
            System.out.println("WARNUNG UserController: ProcessedStatus ist NICHT 'PENDING' direkt nach dem Speichern, sondern: '" + savedLog.getProcessedStatus() + "'");
        }

        // Aktualisiere den Log-Eintrag erneut (falls Änderungen erforderlich)
        systemLogRepository.save(log);

        // Bestätigungsnachricht an den Client
        return ResponseEntity.ok().body(Map.of("message", "Ihre Anfrage zum Zurücksetzen des Passworts wurde an den Administrator weitergeleitet."));
    }
}