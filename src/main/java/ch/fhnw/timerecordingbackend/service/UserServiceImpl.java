package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.authentication.ChangePasswordRequest;
import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.RoleRepository;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException; // Import für ValidationException
import org.slf4j.Logger; // Import für Logger
import org.slf4j.LoggerFactory; // Import für LoggerFactory
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random; // Import für Random

/**
 * Implementierung UserService Interface
 * Geschöftslogik Benutzerverwaltung
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.1 - Passwort zurücksetzten, zufälliges Passwort generieren und PasswordEncoder hinzugefügt
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SystemLogRepository systemLogRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Konstruktor mit Dependency Injection
     * @param userRepository
     * @param roleRepository
     * @param systemLogRepository
     */
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, SystemLogRepository systemLogRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.systemLogRepository = systemLogRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override // <-- Stelle sicher, dass @Override hier steht und keinen Fehler wirft
    @Transactional
    public boolean requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            SystemLog log = new SystemLog();
            log.setAction("Passwort Reset angefordert");
            log.setTimestamp(LocalDateTime.now());
            log.setUserEmail(user.getEmail());
            log.setUserId(user.getId());
            log.setDetails("Benutzer " + user.getEmail() + " hat einen Passwort-Reset angefordert.");
            systemLogRepository.save(log);
            logger.info("Passwort-Reset angefordert für Benutzer: {}", email);
            return true;
        }
        logger.warn("Passwort-Reset für unbekannte E-Mail angefordert: {}", email);
        return false;
    }


    /**
     * Suchmethoden
     */
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Neuen Benutzer erstellen
     * @param user
     * @param roleName
     * @param managerId (Optional) ID des zuzuweisenden Managers
     * @return
     */
    @Override
    @Transactional
    public User createUser(User user, String roleName, Long managerId) { // managerId hinzugefügt
        // Prüfen ob Email bereits existiert
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email existiert bereits");
        }

        //Zeitstempel
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Rolle zuweisen
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Rolle nicht gefunden"));
        user.addRole(role);

        // Manager zuweisen, falls managerId angegeben ist
        if (managerId != null) {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new ValidationException("Manager mit ID " + managerId + " nicht gefunden."));
            // Sicherstellen, dass der zugewiesene Benutzer auch Manager-Rechte hat (optional, aber gute Praxis)
            if (!manager.getRoles().stream().anyMatch(r -> r.getName().equals("MANAGER") || r.getName().equals("ADMIN"))) {
                throw new ValidationException("Der ausgewählte Benutzer mit ID " + managerId + " hat keine Manager- oder Admin-Rolle.");
            }
            user.setManager(manager);
            logger.info("Benutzer {} wird Manager {} zugewiesen.", user.getEmail(), manager.getEmail());
        } else {
            logger.info("Benutzer {} wird ohne direkten Manager erstellt.", user.getEmail());
        }


        // Speichern
        User savedUser = userRepository.save(user);

        // System Log erstellen
        SystemLog log = new SystemLog();
        log.setAction("Benutzer erstellt" + user.getEmail());
        log.setTimestamp(now);
        String managerDetails = managerId != null ? ", Manager ID: " + managerId : "";
        log.setDetails("Benutzer ID: " + savedUser.getId() + ", Rolle: " + roleName + managerDetails);
        systemLogRepository.save(log);

        return savedUser;
    }

    /**
     * Benurter aktualisieren
     * @param id
     * @param user neuer User mit allen Feldern
     * @return
     */
    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        // Benutzer finden
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // Daten Aktualisieren
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPlannedHoursPerDay(user.getPlannedHoursPerDay());
        existingUser.setActive(user.isActive());

        // Zeitstempel
        LocalDateTime now = LocalDateTime.now();
        existingUser.setUpdatedAt(now);

        // System Log erstellen
        SystemLog log = new SystemLog();
        log.setAction("Benutzer aktualisiert" + user.getEmail());
        log.setTimestamp(now);
        log.setDetails("Benutzer ID: " + existingUser.getId());
        systemLogRepository.save(log);

        return userRepository.save(existingUser);
    }

    /**
     * Passwort eines Benutzers ändern
     * @param id
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Override
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        // Benutzer finden
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // altes Passwort überprüfen
        if (!oldPassword.equals(user.getPassword())) {
            return false;
        }

        // Neues Passwort erstellen
        user.setPassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Systemlog erstellen
        SystemLog log = new SystemLog();
        log.setAction("Passwort geändert: " + user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Benutzer ID: " + user.getId());
        systemLogRepository.save(log);

        return true;
    }

    /**
     * Ändert das Passwort für den angemeldeten User.
     * @param request enthält userId, altes und neues Passwort
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Aktuelle Benutzer-ID aus Security Context holen
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ValidationException("Benutzer ist nicht authentifiziert");
        }

        String userEmail = auth.getName(); // E-Mail des eingeloggten Benutzers

        // Benutzer finden
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // Altes Passwort korrekt überprüfen
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ValidationException("Altes Passwort ist falsch");
        }

        // Neues Passwort verschlüsseln und setzen
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Systemlog erstellen
        SystemLog log = new SystemLog();
        log.setAction("Passwort geändert: " + user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Benutzer ID: " + user.getId());
        systemLogRepository.save(log);
    }

    /**
     * Passwort zurücksetzen
     * @param userId
     * @return neues Passwort
     */
    @Override
    @Transactional
    public String resetPasswordToTemporary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Benutzer nicht vorhanden"));

        String tempPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        SystemLog adminActionLog = new SystemLog();
        adminActionLog.setUserId(user.getId());
        adminActionLog.setUserEmail(user.getEmail());
        adminActionLog.setAction("Admin hat Passwort zurückgesetzt");
        adminActionLog.setDetails("Temporäres Passwort generiert für User ID: " + userId + " (" + user.getEmail() + ")");
        adminActionLog.setTimestamp(LocalDateTime.now());
        systemLogRepository.save(adminActionLog);

        System.out.println("DEBUG UserServiceImpl: ----- Start Log-Suche für Passwort-Reset-Anfrage (Workaround) -----");
        System.out.println("DEBUG UserServiceImpl: Suche für userId = " + userId);
        final String targetAction = "Passwort Reset angefordert";
        System.out.println("DEBUG UserServiceImpl: Suche nach action = \"" + targetAction + "\" und Status IS NULL oder PENDING");

        // Zuerst nach Status IS NULL suchen, da deine Logs das zeigen
        List<SystemLog> requestLogs = systemLogRepository.findByUserIdAndActionAndProcessedStatusIsNullOrderByTimestampDesc(userId, targetAction);

        if (requestLogs.isEmpty()) {
            // Falls nichts mit NULL gefunden wurde, sicherheitshalber nochmal nach PENDING suchen
            System.out.println("DEBUG UserServiceImpl: Kein Log mit Status NULL gefunden für Action \"" + targetAction + "\". Suche nach Status PENDING.");
            requestLogs = systemLogRepository.findByUserIdAndActionAndProcessedStatusOrderByTimestampDesc(
                    userId,
                    targetAction,
                    "PENDING"
            );
        }

        if (!requestLogs.isEmpty()) {
            SystemLog requestLogToUpdate = requestLogs.get(0);
            System.out.println("DEBUG UserServiceImpl: GEFUNDEN! Log-Eintrag ID " + requestLogToUpdate.getId() + " mit Status \"" + requestLogToUpdate.getProcessedStatus() + "\". Wird auf COMPLETED gesetzt.");
            requestLogToUpdate.setProcessedStatus("COMPLETED");
            systemLogRepository.save(requestLogToUpdate);
            System.out.println("DEBUG UserServiceImpl: SystemLog ID " + requestLogToUpdate.getId() + " wurde auf PROCESSED_STATUS = COMPLETED gesetzt für User ID: " + userId);
        } else {
            System.out.println("DEBUG UserServiceImpl: FEHLER (Workaround): Kein passender SystemLog (Status NULL oder PENDING) mit Action '" + targetAction + "' für User ID " + userId + " gefunden.");
            List<SystemLog> logsByAction = systemLogRepository.findByUserIdAndActionOrderByTimestampDesc(userId, targetAction);
            if (!logsByAction.isEmpty()) {
                System.out.println("DEBUG UserServiceImpl (Workaround): Logs für userId " + userId + " und Action '" + targetAction + "' (unabhängig vom Status) GEFUNDEN:");
                logsByAction.forEach(log -> System.out.println("  -> Log ID: " + log.getId() + ", Action: \"" + log.getAction() + "\", Status: \"" + log.getProcessedStatus() + "\", Timestamp: " + log.getTimestamp()));
            } else {
                System.out.println("DEBUG UserServiceImpl (Workaround): Auch KEINE Logs für userId " + userId + " und Action '" + targetAction + "' (unabhängig vom Status) gefunden.");
            }
        }
        System.out.println("DEBUG UserServiceImpl: ----- Ende Log-Suche (Workaround) -----");
        return tempPassword;
    }
    /**
     * Generiert ein zufälliges Passwort.
     * @return zufälliges Passwort
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * Benutzer deaktivieren
     * @param id
     * @return
     */
    @Override
    @Transactional
    public User deactivateUser(Long id) {
        // Benutzer finden
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // Benutzer deaktivieren
        user.deactivate();
        User deactivatedUser = userRepository.save(user);

        // Systemlog erstellen
        SystemLog log = new SystemLog();
        log.setAction("Benutzer deaktiviert: " + user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Benutzer ID: " + user.getId());
        systemLogRepository.save(log);

        return deactivatedUser;
    }

    /**
     * Benutzer aktivieren
     * @param id
     * @return
     */
    @Override
    @Transactional
    public User activateUser(Long id) {
        // Benutzer finden
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // Benutzer aktivieren
        user.activate();
        User activatedUser = userRepository.save(user);

        // Systemlog erstellen
        SystemLog log = new SystemLog();
        log.setAction("Benutzer aktiviert: " + user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Benutzer ID: " + user.getId());
        systemLogRepository.save(log);

        return activatedUser;
    }

    /**
     * Benutzer Status ändern
     * @param id
     * @param status
     * @return
     */
    @Override
    @Transactional
    public User updateUserStatus(Long id, UserStatus status) {
        // Benutzer finden
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        // Status aktualisieren
        user.setStatus(status);
        user.setActive(status == UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        // Systemlog erstellen
        SystemLog log = new SystemLog();
        log.setAction("Benutzerstatus geändert: " + user.getEmail() + " " + status);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Benutzer ID: " + user.getId());
        systemLogRepository.save(log);

        return updatedUser;
    }

    /**
     * Rolle zu Benutzer hinzufügen
     * @param userId
     * @param roleName
     * @return
     */
    @Override
    @Transactional
    public User addRoleToUser(Long userId, String roleName) {
        // Benutzer finden
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Benutzer nicht gefunden"));

        // Rolle finden
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Rolle nicht gefunden"));

        // Rolle hinzufügen, wenn der Benutzer sie noch nicht hat
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals(roleName))) {
            user.addRole(role);
            userRepository.save(user);

            // Systemlog erstellen
            SystemLog log = new SystemLog();
            log.setAction("Rolle hinzugefügt: " + roleName + " zu " + user.getEmail());
            log.setTimestamp(LocalDateTime.now());
            log.setDetails("Benutzer ID: " + user.getId());
            systemLogRepository.save(log);
        }

        return user;
    }

    /**
     * Rolle von Benutzer entfernen
     * @param userId
     * @param roleName
     * @return
     */
    @Override
    @Transactional
    public User removeRoleFromUser(Long userId, String roleName) {
        // Benutzer finden
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        // Prüfen ob der Benutzer die Rolle hat
        Role roleToRemove = null;
        for (Role role : user.getRoles()) {
            if (role.getName().equals(roleName)) {
                roleToRemove = role;
                break;
            }
        }

        // Rolle entfernen
        if (roleToRemove != null) {
            user.getRoles().remove(roleToRemove);
            userRepository.save(user);

            // Systemlog erstellen
            SystemLog log = new SystemLog();
            log.setAction("Rolle entfernt: " + roleName + " von " + user.getEmail());
            log.setTimestamp(LocalDateTime.now());
            log.setDetails("Benutzer ID: " + user.getId());
            systemLogRepository.save(log);
        }

        return user;
    }

    @Override
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }

    @Override
    public boolean isAdmin(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(user -> user.hasRole("ADMIN")).orElse(false);
    }

    @Override
    public boolean isProjectManager(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(user -> user.hasRole("MANAGER")).orElse(false);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

}
