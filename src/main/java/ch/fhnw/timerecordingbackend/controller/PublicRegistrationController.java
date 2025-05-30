package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.dto.registration.RegistrationRequest;
import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.service.RegistrationService;
import ch.fhnw.timerecordingbackend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rest Controller Regestrierungsanfragen
 * @author PD
 */
@RestController
@RequestMapping("/api/public")
public class PublicRegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    @Autowired
    public PublicRegistrationController(RegistrationService registrationRequestService, UserService userService) {
        this.registrationService = registrationRequestService;
        this.userService = userService;
    }

    /**
     * Erstellt eine neue Registrierungsanfrage
     * @param request
     * @return
     */
    @PostMapping("/registration-requests")
    public ResponseEntity<Map<String, String>> submitRegistrationRequest(@Valid @RequestBody RegistrationRequest request) {
        try {
            registrationService.submitRegistrationRequest(request);
            return new ResponseEntity<>(Map.of("message", "Registrierungsanfrage erfolgreich eingereicht. Sie wird vom Administrator geprüft."), HttpStatus.ACCEPTED);
        } catch (ValidationException e) {
            String errorMessage;
            if (e.getMessage().contains("bereits") || e.getMessage().contains("exists")) {
                errorMessage = "Eine Registrierungsanfrage mit dieser E-Mail-Adresse existiert bereits oder wurde bereits verarbeitet.";
            } else {
                errorMessage = e.getMessage();
            }
            return new ResponseEntity<>(
                    Map.of("error", errorMessage),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            e.printStackTrace();

            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                errorMessage = "Eine Registrierungsanfrage mit dieser E-Mail-Adresse wurde bereits eingereicht.";
            } else {
                errorMessage = "Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.";
            }

            return new ResponseEntity<>(
                    Map.of("error", errorMessage),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    /**
     * Gibt eine Liste aller verfügbaren Manager zurück
     * @return
     */
    @GetMapping("/managers")
    public ResponseEntity<List<Map<String, Object>>> getPublicManagers() {
        List<User> users = userService.findAllUsers();

        List<Map<String, Object>> managers = users.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("MANAGER") || role.getName().equals("ADMIN")))
                .map(user -> {
                    Map<String, Object> managerInfo = new java.util.HashMap<>();
                    managerInfo.put("id", user.getId());
                    managerInfo.put("firstName", user.getFirstName());
                    managerInfo.put("lastName", user.getLastName());
                    managerInfo.put("email", user.getEmail());
                    managerInfo.put("roles", user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toList()));

                    return managerInfo;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(managers);
    }
}
