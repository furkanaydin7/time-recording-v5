package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Absence;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import ch.fhnw.timerecordingbackend.repository.AbsenceRepository;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementierung AbsenceService Interface
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Service
public class AbsenceServiceImpl implements AbsenceService{

    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;

    @Autowired
    public AbsenceServiceImpl(
            AbsenceRepository absenceRepository,
            UserRepository userRepository,
            SystemLogRepository systemLogRepository) {
        this.absenceRepository = absenceRepository;
        this.userRepository = userRepository;
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public List<Absence> findAllAbsences() {
        return absenceRepository.findAll();
    }

    @Override
    public Optional<Absence> findById(Long id) {
        return absenceRepository.findById(id);
    }

    @Override
    public List<Absence> findByUser(User user) {
        return absenceRepository.findByUser(user);
    }

    @Override
    public List<Absence> findByType(AbsenceType type) {
        return absenceRepository.findByType(type);
    }

    @Override
    public List<Absence> findByUserAndType(User user, AbsenceType type) {
        return absenceRepository.findByUserAndType(user, type);
    }

    /**
     * Abwesenheit erstellen
     * @param absence
     * @return
     */
    @Override
    @Transactional
    public Absence createAbsence(Absence absence) {
        // Validieren Abwesenheit
        if (!isValidAbsence(absence)) {
            throw new IllegalArgumentException("Ungültige Abwesenheit");
        }

        // Prüfung auf überlappende Abwesenheiten
        if (hasOverlappingAbsences(absence.getUser().getId(), absence.getStartDate(), absence.getEndDate(), null)) {
            throw new IllegalArgumentException("Die Abwesenheit überschneidet sich mit einer bestehenden Abwesenheit");
        }

        // Zeitstempel setzen
        LocalDateTime now = LocalDateTime.now();
        absence.setCreatedAt(now);
        absence.setUpdatedAt(now);

        // Abwesenheit speichern
        Absence savedAbsence = absenceRepository.save(absence);

        // Log erstellen
        createSystemLog("Abwesenheit erstellt für " + absence.getUser().getFullName(),
                "Abwesenheit ID: " + savedAbsence.getId() + ", Typ: " + absence.getType().getDisplayName() +
                        ", Zeitraum: " + absence.getStartDate() + " bis " + absence.getEndDate());

        return savedAbsence;
    }

    /**
     * Abwesenheit aktualisieren
     * @param id
     * @param updatedAbsence
     * @return
     */
    @Override
    @Transactional
    public Absence updateAbsence(Long id, Absence updatedAbsence) {
        // Abwesenheit finden
        Absence existingAbsence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht vorhanden"));

        // Validieren der Abwesenheit
        if (!isValidAbsence(updatedAbsence)) {
            throw new IllegalArgumentException("Ungültige Abwesenheit");
        }

        // Prüfung auf überlappende Abwesenheiten
        if (hasOverlappingAbsences(updatedAbsence.getUser().getId(),
                updatedAbsence.getStartDate(),
                updatedAbsence.getEndDate(), id)) {
            throw new IllegalArgumentException("Die Abwesenheit überschneidet sich mit einer bestehenden Abwesenheit");
        }

        // Abwesenheit aktualisieren
        existingAbsence.setStartDate(updatedAbsence.getStartDate());
        existingAbsence.setEndDate(updatedAbsence.getEndDate());
        existingAbsence.setType(updatedAbsence.getType());
        existingAbsence.setUpdatedAt(LocalDateTime.now());

        Absence savedAbsence = absenceRepository.save(existingAbsence);

        // Log erstellen
        createSystemLog("Abwesenheit aktualisiert für " + existingAbsence.getUser().getFullName(),
                "Abwesenheit ID: " + existingAbsence.getId());

        return savedAbsence;
    }

    /**
     * Abwesenheit löschen
     * @param id
     */
    @Override
    @Transactional
    public void deleteAbsence(Long id) {
        // Abwesenheit finden
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht vorhanden"));


        String userName = absence.getUser().getFullName();
        String absenceDetails = "Typ: " + absence.getType().getDisplayName() +
                ", Zeitraum: " + absence.getStartDate() + " bis " + absence.getEndDate();

        // Abwesenheit löschen
        absenceRepository.delete(absence);

        // Log erstellen
        createSystemLog("Abwesenheit gelöscht für " + userName,
                "Abwesenheit ID: " + id + ", " + absenceDetails);
    }

    /**
     * Abwesenheit genehmigen
     * @param id
     * @param approverId
     * @return
     * @author PD/FA
     */
    @Override
    @Transactional
    public Absence approveAbsence(Long id, Long approverId) {
        // Abwesenheit finden
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht vorhanden"));

        // Genehmiger finden
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("Genehmiger nicht vorhanden"));

        User applicant = absence.getUser();

        boolean isAdmin = approver.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        // Prüft, ob der Antragsteller einen Manager hat UND ob dieser Manager der aktuelle Genehmiger ist
        boolean isDirectManager = applicant.getManager() != null && applicant.getManager().getId().equals(approverId);

        // Nur Admins oder der direkte Vorgesetzte dürfen genehmigen
        if (!isAdmin && !isDirectManager) {
            throw new AccessDeniedException("Nur der direkte Vorgesetzte oder ein Administrator darf diese Abwesenheit genehmigen.");
        }

        // Abwesenheit genehmigen
        absence.approve(approver);
        absence.setUpdatedAt(LocalDateTime.now()); // Sicherstellen, dass das Update-Datum gesetzt wird
        Absence approvedAbsence = absenceRepository.save(absence);

        // Log erstellen
        createSystemLog("Abwesenheit genehmigt für " + applicant.getFullName() +
                        " von " + approver.getFullName(),
                "Abwesenheit ID: " + approvedAbsence.getId() + ", Status: Genehmigt");

        return approvedAbsence;
    }

    /**
     * Abwesenheit ablehnen
     * @param id
     * @param rejecterId
     * @return
     * @author PD/FA
     */
    @Override
    @Transactional
    public Absence rejectAbsence(Long id, Long rejecterId) {
        // Abwesenheit finden
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abwesenheit nicht vorhanden"));
        User rejecter = userRepository.findById(rejecterId)
                .orElseThrow(() -> new IllegalArgumentException("Ablehnender nicht vorhanden"));

        User applicant = absence.getUser();

        boolean isAdmin = rejecter.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        boolean isDirectManager = applicant.getManager() != null && applicant.getManager().getId().equals(rejecterId);

        if (!isAdmin && !isDirectManager) {
            throw new AccessDeniedException("Nur der direkte Vorgesetzte oder ein Administrator darf diese Abwesenheit ablehnen.");
        }

        // Abwesenheit ablehnen
        absence.reject();
        absence.setUpdatedAt(LocalDateTime.now());
        Absence rejectedAbsence = absenceRepository.save(absence);

        // Log erstellen
        createSystemLog("Abwesenheit abgelehnt für " + applicant.getFullName() +
                        " von " + rejecter.getFullName(),
                "Abwesenheit ID: " + rejectedAbsence.getId() + ", Status: Abgelehnt");

        return rejectedAbsence;
    }

    /**
     * Genehmigte Absenz finden
     * @author FA
     * @return
     */
    @Override
    public List<Absence> findApprovedAbsences() {
        return absenceRepository.findByStatus(AbsenceStatus.APPROVED);
    }

    /**
     * Pending Absenz finden
     * @author FA
     * @return
     */
    @Override
    public List<Absence> findPendingAbsences(User currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if (currentUser.hasRole("ADMIN")) {
            return absenceRepository.findByStatus(AbsenceStatus.PENDING);
        } else if (currentUser.hasRole("MANAGER")) {
            List<User> directReports = userRepository.findByManager(currentUser);
            if (directReports == null || directReports.isEmpty()) {
                return Collections.emptyList();
            }
            return absenceRepository.findByUserInAndStatus(directReports, AbsenceStatus.PENDING);
        }
        return Collections.emptyList();
    }

    /**
     * Genehmigte Absenz abrufen
     * @author FA
     * @param currentUser Der aktuell angemeldete Benutzer.
     * @return
     */
    @Override
    public List<Absence> getApprovedAbsencesForUserView(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Benutzer nicht authentifiziert.");
        }

        if (currentUser.hasRole("ADMIN")) {
            // Admins sehen alle genehmigten Abwesenheiten aller Benutzer
            return absenceRepository.findByStatus(AbsenceStatus.APPROVED);
        } else if (currentUser.hasRole("MANAGER")) {
            // Manager sehen genehmigte Abwesenheiten ihrer direkten Mitarbeiter
            List<User> directReports = userRepository.findByManager(currentUser);
            if (directReports == null || directReports.isEmpty()) {
                return Collections.emptyList();
            }
            return absenceRepository.findByUserInAndStatus(directReports, AbsenceStatus.APPROVED);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Absence> findApprovedAbsencesByUser(User user) {
        return absenceRepository.findByUserAndStatus(user, AbsenceStatus.APPROVED);
    }

    @Override
    public List<Absence> findPendingAbsencesByUser(User user) {
        return absenceRepository.findByUserAndStatus(user, AbsenceStatus.PENDING);
    }

    public List<Absence> findRejectedAbsencesByUser(User user) {
        return absenceRepository.findByUserAndStatus(user, AbsenceStatus.REJECTED);
    }

    @Override
    public boolean hasApprovedAbsenceOnDate(Long userId, LocalDate date) {
        return absenceRepository.hasApprovedAbsenceOnDate(userId, date);
    }

    @Override
    public Long sumAbsenceDaysByUserIdAndTypeAndDateRange(Long userId, AbsenceType type,
                                                          LocalDate startDate, LocalDate endDate) {
        return absenceRepository.sumAbsenceDaysByUserIdAndTypeAndDateRange(userId, type, startDate, endDate);
    }

    @Override
    public List<Absence> findCurrentAndFutureAbsencesByUserId(Long userId, LocalDate today) {
        return absenceRepository.findCurrentAndFutureAbsencesByUserId(userId, today);
    }

    /**
     * Abwesenheit validieren
     * @param absence
     * @return
     */
    @Override
    public boolean isValidAbsence(Absence absence) {
        // Validierungen
        if (absence == null || absence.getUser() == null ||
                absence.getStartDate() == null || absence.getEndDate() == null ||
                absence.getType() == null) {
            return false;
        }

        // Enddatum muss nach oder gleich Startdatum sein
        if (absence.getEndDate().isBefore(absence.getStartDate())) {
            return false;
        }

        // Abwesenheit darf nicht in der Vergangenheit liegen
        if (absence.getId() == null && absence.getStartDate().isBefore(LocalDate.now())) {
            return false;
        }

        // Maximale Abwesenheitsdauer 60 Tage
        if (absence.getDurationInDays() > 60) {
            return false;
        }

        return true;
    }

    /**
     * Überlappung bei Abwesenheiten
     * @param userId
     * @param startDate
     * @param endDate
     * @param excludeId
     * @return
     */
    @Override
    public boolean hasOverlappingAbsences(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId) {
        // Alle Abwesenheiten eines Benutzers abrufen
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht vorhanden"));

        List<Absence> existingAbsences = absenceRepository.findByUser(user);

        // Prüfung auf Überschneidung
        for (Absence existing : existingAbsences) {
            // Aktuelle Abwesenheit ausschliessen
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }

            // Prüfung auf Überschneidung
            if (!(endDate.isBefore(existing.getStartDate()) ||
                    startDate.isAfter(existing.getEndDate()))) {
                return true; // Überschneidung gefunden
            }
        }

        return false; // Keine Überschneidung
    }

    /**
     * Erstellen von Systemlogs
     * @param action
     * @param details
     */
    private void createSystemLog(String action, String details) {
        SystemLog log = new SystemLog();
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        systemLogRepository.save(log);
    }
}
