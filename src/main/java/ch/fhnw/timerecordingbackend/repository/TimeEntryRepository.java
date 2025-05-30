package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.TimeEntry;
import ch.fhnw.timerecordingbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository für Zeiteinträge Entitäten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    /**
     * Liefert alle Zeiteinträge eines Benutzers zurück.
     * @param user
     * @return Liste mit Zeiteinträgen eines Benutzers.
     */
    List<TimeEntry> findByUser(User user);

    /**
     * Liefert einen Zeiteintrag zurück mit bestimmtem Benutzer und Datum.
     * @param user
     * @param date
     * @return Optional mit Zeiteintrag, wenn gefunden, sonst Optional.empty()
     */
    Optional<TimeEntry> findByUserAndDate(User user, LocalDate date);

    /**
     * Liefert alle Zeiteinträge eines Projekts zurück.
     * @param projectId
     * @return Liste mit Zeiteinträgen eines Projekts.
     */
    List<TimeEntry> findByProjectId(Long projectId);

    /**
     * Liefert alle Zeiteinträge eines Benutzers für ein bestimmtes Projekt zurück.
     * @param user
     * @param projectId
     * @return Liste mit Zeiteinträgen eines Benutzers für ein bestimmtes Projekt.
     */
    List<TimeEntry> findByUserAndProjectId(User user, Long projectId);

    /**
     * Liefert alle Zeiteinträge für ein bestimmtes Datum zurück.
     * @param date
     * @return Liste mit Zeiteinträgen für ein bestimmtes Datum.
     */
    List<TimeEntry> findByDate(LocalDate date);

    /**
     * Prüft ob ein Zeiteintrag für einen bestimmten Benutzer und einem bestimmten Datum existiert.
     * @param user
     * @param date
     * @return true, wenn Zeiteintrag existiert, sonst false
     */
    boolean existsByUserAndDate(User user, LocalDate date);

    /**
     * Liefert alle Zeiteinträge ohne Projekt zurück.
     * @return Liste mit Zeiteinträgen ohne Projekt.
     */
    List<TimeEntry> findByProjectIsNull();

    /**
     * Summe der tatsächlichen Arbeitsstunden eines Benutzers für einen bestimmten Zeitraum zurück.
     * @param userId
     * @param startDate
     * @param endDate
     * @return String mit der Summe der tatsächlichen Arbeitsstunden eines Benutzers für einen bestimmten Zeitraum.
     * Quelle: ChatGPT.com
     */
    @Query("SELECT SUM(t.actualHours) FROM TimeEntry t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    String sumActualHoursByUserIdAndDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Summe der geplanten Arbeitsstunden eines Benutzers für einen bestimmten Zeitraum zurück.
     * @param userId
     * @param startDate
     * @param endDate
     * @return String mit der Summe der geplanten Arbeitsstunden eines Benutzers für einen bestimmten Zeitraum.
     * Quelle: ChatGPT.com
     */
    @Query("SELECT SUM(t.plannedHours) FROM TimeEntry t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    String sumPlannedHoursByUserIdAndDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Liefert alle aktiven Zeiteinträge eines Benutzers zurück.
     * @param userId
     * @return Liste mit aktiven Zeiteinträgen eines Benutzers.
     * Quelle: ChatGPT.com
     */
    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND SIZE(t.startTimes) > SIZE(t.endTimes)")
    List<TimeEntry> findActiveEntriesByUserId(@Param("userId") Long userId);

    /**
     * Anzahl der Arbeits-Tage eines Benutzers für einen bestimmten Zeitraum zurück.
     * @param userId
     * @param startDate
     * @param endDate
     * @return Anzahl der Arbeits-Tage eines Benutzers für einen bestimmten Zeitraum.
     * Quelle: ChatGPT.com
     */
    @Query("SELECT COUNT(DISTINCT t.date) FROM TimeEntry t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    long countWorkdaysByUserIdAndDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Summe aller tatsächlichen Stunden für ein Projekt berechnen
     * @param projectId
     * @return
     * Quelle: ChatGPT.com
     */
    @Query(value = "SELECT COALESCE(SUM(EXTRACT(HOUR FROM TO_TIMESTAMP(t.actual_hours, 'HH24:MI')) * 60 + EXTRACT(MINUTE FROM TO_TIMESTAMP(t.actual_hours, 'HH24:MI'))), 0) FROM time_entries t WHERE t.project_id = :projectId", nativeQuery = true)
    List<Object[]> sumActualHoursByProjectId(@Param("projectId") Long projectId);

    /**
     * Benutzer anhand Zeiteinträge für ein Projekt finden
     * @param projectId
     * @return
     */
    @Query("SELECT DISTINCT t.user FROM TimeEntry t WHERE t.project.id = :projectId")
    List<User> findDistinctUsersByProjectId(@Param("projectId") Long projectId);

}
