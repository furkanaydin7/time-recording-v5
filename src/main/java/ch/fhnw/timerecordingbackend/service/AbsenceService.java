package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Absence;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface für Absenzen
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public interface AbsenceService {

    /**
     * Findet alle Abwesenheiten
     * @return Liste aller Abwesenheiten
     */
    List<Absence> findAllAbsences();

    /**
     * Findet eine Abwesenheit nach ID
     * @param id
     * @return Optional mit der gefundenen Abwesenheit oder leer, wenn nicht gefunden
     */
    Optional<Absence> findById(Long id);

    /**
     * Findet alle Abwesenheiten eines Benutzers
     * @param user
     * @return Liste der Abwesenheiten des Benutzers
     */
    List<Absence> findByUser(User user);

    /**
     * Findet alle Abwesenheiten eines Typen
     * @param type
     * @return Liste der Abwesenheiten des angegebenen Typs
     */
    List<Absence> findByType(AbsenceType type);

    /**
     * Findet alle Abwesenheiten eines bestimmten Benutzers und Typs
     * @param user
     * @param type
     * @return Liste der Abwesenheiten des Benutzers und Typs
     */
    List<Absence> findByUserAndType(User user, AbsenceType type);

    /**
     * Abwesenheit erstellen
     * @param absence
     * @return Die erstellte Abwesenheit mit gesetzter ID
     */
    Absence createAbsence(Absence absence);

    /**
     * Aktualisiert Daten einer Abwesenheit
     * @param id
     * @param updatedAbsence
     * @return Die aktualisierte Abwesenheit
     */
    Absence updateAbsence(Long id, Absence updatedAbsence);

    /**
     * Abesenheit löschen
     * @param id
     */
    void deleteAbsence(Long id);

    /**
     * Abwesenheit genehmigen
     * @param id
     * @param approverId
     * @return Die genehmigte Abwesenheit
     */
    Absence approveAbsence(Long id, Long approverId);

    /**
     * Abwesenheit ablehnen
     * @param id
     * @return Die abgelehnte Abwesenheit
     */
    Absence rejectAbsence(Long id, Long rejecterId);

    /**
     * Ruft genehmigte Abwesenheiten für die Übersichtsseite ab,
     * gefiltert nach der Rolle des aktuellen Benutzers (Admin oder Manager).
     * @param currentUser Der aktuell angemeldete Benutzer.
     * @return Liste der relevanten genehmigten Abwesenheiten.
     */
    List<Absence> getApprovedAbsencesForUserView(User currentUser);

    /**
     * Findet alle genehmigten Abwesenheiten
     * @return Liste aller genehmigten Abwesenheiten
     */
    List<Absence> findApprovedAbsences();

    /**
     * Findet alle nicht genehmigten Abwesenheiten, gefiltert nach der Rolle des aktuellen Benutzers.
     * Admins sehen alle. Manager sehen die ihrer direkten Mitarbeiter.
     * @param currentUser Der aktuell angemeldete Benutzer.
     * @return Liste aller nicht genehmigten Abwesenheiten.
     */
    List<Absence> findPendingAbsences(User currentUser);

    List<Absence> findRejectedAbsencesByUser(User user);

    /**
     * Findet alle genehmigten Abwesenheiten eines Benutzers
     * @param user
     * @return Liste der genehmigten Abwesenheiten des Benutzers
     */
    List<Absence> findApprovedAbsencesByUser(User user);

    /**
     * Findet alle nicht genehmigten Abwesenheiten eines Benutzers
     * @param user
     * @return Liste der nicht genehmigten Abwesenheiten des Benutzers
     */
    List<Absence> findPendingAbsencesByUser(User user);

    /**
     * Prüft ob Benutzer an einem bestimmten Datum eine genehmigte Abwesenheit hat
     * @param userId
     * @param date
     * @return true wenn eine genehmigte Abwesenheit existiert, sonst false
     */
    boolean hasApprovedAbsenceOnDate(Long userId, LocalDate date);

    /**
     * Berechnet die Gesamtzahl der Abwesenheitstage eines Benutzers in einem bestimmten Zeitraum und von einem bestimmten Typ
     * @param userId
     * @param type
     * @param startDate
     * @param endDate
     * @return Anzahl der Abwesenheitstage
     */
    Long sumAbsenceDaysByUserIdAndTypeAndDateRange(Long userId, AbsenceType type, LocalDate startDate, LocalDate endDate);

    /**
     * Findet alle aktuellen und zukünftigen Abwesenheiten eines Benutzers
     * @param userId
     * @param today
     * @return Liste der aktuellen und zukünftigen Abwesenheiten
     */
    List<Absence> findCurrentAndFutureAbsencesByUserId(Long userId, LocalDate today);

    /**
     * Validiert ob eine Abwesenheit gültig ist
     * @param absence
     * @return true, wenn die Abwesenheit gültig ist, sonst false
     */
    boolean isValidAbsence(Absence absence);

    /**
     * Prüft ob sich Abwesenheiten überschneiden
     * @param userId
     * @param startDate
     * @param endDate
     * @param excludeId
     * @return true, wenn sich Abwesenheiten überschneiden, sonst false
     */
    boolean hasOverlappingAbsences(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId);
}