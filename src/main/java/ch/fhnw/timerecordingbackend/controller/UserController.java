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
 * REST Controller für Benutzer
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.1 - sendpasswortresetlink entfernt - PD
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired // Injiziere das SystemLogRepository
    private SystemLogRepository systemLogRepository;

    @PutMapping( "/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Passwort geändert")
        );
    }
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok().body(Map.of("message", "Wenn ein Konto mit dieser E-Mail-Adresse existiert, wurde Ihre Anfrage verarbeitet."));
        }
        User user = userOptional.get();
        SystemLog log = new SystemLog();
        log.setAction("Passwort Reset angefordert");
        log.setTimestamp(LocalDateTime.now());
        log.setUserEmail(user.getEmail());
        log.setUserId(user.getId());
        log.setDetails("Passwort-Reset-Anfrage von Login-Seite für Benutzer: " + user.getEmail());
        log.setProcessedStatus("PENDING");
        SystemLog savedLog = systemLogRepository.save(log);

        System.out.println("DEBUG UserController: SystemLog nach save(): ID=" + savedLog.getId() +
                ", Action=\"" + savedLog.getAction() + "\"" +
                ", ProcessedStatus=\"" + savedLog.getProcessedStatus() + "\"" + // Wichtig!
                ", UserID=" + savedLog.getUserId());

        if (savedLog.getProcessedStatus() == null) {
            System.out.println("WARNUNG UserController: ProcessedStatus ist NULL direkt nach dem Speichern!");
        } else if (!savedLog.getProcessedStatus().equals("PENDING")) {
            System.out.println("WARNUNG UserController: ProcessedStatus ist NICHT 'PENDING' direkt nach dem Speichern, sondern: '" + savedLog.getProcessedStatus() + "'");
        }
        systemLogRepository.save(log);
        return ResponseEntity.ok().body(Map.of("message", "Ihre Anfrage zum Zurücksetzen des Passworts wurde an den Administrator weitergeleitet."));
    }
}