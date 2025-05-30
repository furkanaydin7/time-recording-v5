package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.registration.RegistrationRequest;
import ch.fhnw.timerecordingbackend.model.Registration;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.RegistrationRepository;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementierung Service Interface Registrierungsanfragen
 * @author PD
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRequestRepository; // REPOSITORY NAME GEÄNDERT
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationServiceImpl(RegistrationRepository registrationRequestRepository, // REPOSITORY NAME GEÄNDERT
                                   UserRepository userRepository,
                                   SystemLogRepository systemLogRepository,
                                   UserService userService,
                                   PasswordEncoder passwordEncoder) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.userRepository = userRepository;
        this.systemLogRepository = systemLogRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Registration submitRegistrationRequest(RegistrationRequest requestDto) { // DTO bleibt, Rückgabe ist Entität
        if (registrationRequestRepository.existsByEmail(requestDto.getEmail())) {
            throw new ValidationException("Eine Registrierungsanfrage mit dieser E-Mail existiert bereits.");
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ValidationException("Ein Benutzer mit dieser E-Mail existiert bereits.");
        }

        List<String> allowedRequestedRoles = Arrays.asList("EMPLOYEE", "MANAGER");
        if (!allowedRequestedRoles.contains(requestDto.getRole().toUpperCase())) {
            throw new ValidationException("Ungültige Rolle angefordert: " + requestDto.getRole() + ". Erlaubte Rollen sind: EMPLOYEE, MANAGER.");
        }

        try {
            Registration newRequest = new Registration();
            newRequest.setFirstName(requestDto.getFirstName().trim());
            newRequest.setLastName(requestDto.getLastName().trim());
            newRequest.setEmail(requestDto.getEmail().trim().toLowerCase());
            newRequest.setRequestedRole(requestDto.getRole().toUpperCase());
            newRequest.setStatus("PENDING");
            newRequest.setCreatedAt(LocalDateTime.now());

            if (requestDto.getManagerId() != null) {
                User manager = userRepository.findById(requestDto.getManagerId())
                        .orElseThrow(() -> new ValidationException("Manager nicht gefunden"));
                newRequest.setManager(manager);
            }

            Registration savedRequest = registrationRequestRepository.save(newRequest);


            createSystemLog("Registrierungsanfrage eingereicht",
                    "Anfrage ID: " + savedRequest.getId() + ", E-Mail: " + savedRequest.getEmail(),
                    null, savedRequest.getEmail(), null, "RegistrationRequest", savedRequest.getId());

            return savedRequest;

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique constraint")) {
                throw new ValidationException("Eine Registrierungsanfrage mit dieser E-Mail-Adresse wurde bereits eingereicht.");
            } else {
                throw new ValidationException("Datenbankfehler beim Speichern der Registrierungsanfrage.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
        }
    }

    @Override
    public List<Registration> getAllPendingRequests() { // ENTITÄTSNAME GEÄNDERT
        return registrationRequestRepository.findByStatus("PENDING");
    }

    @Override
    public List<Registration> getAllRequests() { // ENTITÄTSNAME GEÄNDERT
        return registrationRequestRepository.findAll();
    }

    @Override
    public Optional<Registration> getRequestById(Long id) { // ENTITÄTSNAME GEÄNDERT
        return registrationRequestRepository.findById(id);
    }

    @Override
    @Transactional
    public void approveRegistrationRequest(Long requestId, String adminEmail) {
        Registration request = registrationRequestRepository.findById(requestId) // ENTITÄTSNAME GEÄNDERT
                .orElseThrow(() -> new ValidationException("Registrierungsanfrage nicht gefunden."));

        if (!request.getStatus().equals("PENDING")) {
            throw new ValidationException("Anfrage ist nicht ausstehend und kann nicht genehmigt werden.");
        }

        List<String> allowedAssignedRoles = Arrays.asList("EMPLOYEE", "MANAGER");
        String roleToAssign = request.getRequestedRole().toUpperCase(); // Die angeforderte Rolle
        if (!allowedAssignedRoles.contains(roleToAssign)) {
            throw new ValidationException("Angeforderte Rolle '" + roleToAssign + "' ist für die automatische Zuweisung über die Registrierung nicht erlaubt. Manuelle Zuweisung durch Admin erforderlich.");
        }

        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getLastName().toLowerCase()));
        newUser.setPlannedHoursPerDay(8.0);
        newUser.setManager(request.getManager());

        User createdUser = userService.createUser(newUser, roleToAssign);

        request.setStatus("APPROVED");
        registrationRequestRepository.save(request);

        User adminUser = userRepository.findByEmail(adminEmail).orElse(null);
        createSystemLog("Registrierungsanfrage genehmigt",
                "Anfrage ID: " + requestId + ", E-Mail: " + request.getEmail() + ". Neuer Benutzer ID: " + createdUser.getId(),
                adminUser != null ? adminUser.getId() : null, adminEmail, null, "RegistrationRequest", requestId);
    }

    @Override
    @Transactional
    public void rejectRegistrationRequest(Long requestId, String adminEmail) {
        Registration request = registrationRequestRepository.findById(requestId) // ENTITÄTSNAME GEÄNDERT
                .orElseThrow(() -> new ValidationException("Registrierungsanfrage nicht gefunden."));

        if (!request.getStatus().equals("PENDING")) {
            throw new ValidationException("Anfrage ist nicht ausstehend und kann nicht abgelehnt werden.");
        }

        User adminUser = userRepository.findByEmail(adminEmail).orElse(null);
        createSystemLog("Registrierungsanfrage abgelehnt und gelöscht",
                "Anfrage ID: " + requestId +
                        ", E-Mail: " + request.getEmail() +
                        ", Name: " + request.getFirstName() + " " + request.getLastName() +
                        " wurde abgelehnt und aus der Datenbank entfernt.",
                adminUser != null ? adminUser.getId() : null,
                adminEmail,
                null,
                "RegistrationRequest",
                requestId);

        registrationRequestRepository.delete(request);
    }

    private void createSystemLog(String action, String details, Long userId, String userEmail, String ipAddress, String targetEntity, Long targetId) {
        SystemLog log = new SystemLog();
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        log.setUserId(userId);
        log.setUserEmail(userEmail);
        log.setIpAddress(ipAddress);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        systemLogRepository.save(log);
    }
}
