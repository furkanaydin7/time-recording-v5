package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.Absence;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository für Absence Entitäten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    /**
     * Liefert alle Abwesenheiten eines Benutzers zurück.
     * @param user
     * @return Liste mit Abwesenheiten eines Benutzers.
     */
    List<Absence> findByUser(User user);

    /**
     * Liefert alle Abwesenheiten eines bestimmten Typs zurück.
     * @param type
     * @return Liste mit Abwesenheiten eines bestimmten Typs.
     */
    List<Absence> findByType(AbsenceType type);

    /**
     * Liefert alle Abwesenheiten eines Benutzers eines bestimmten Typs zurück.
     * @param user
     * @param type
     * @return Liste mit Abwesenheiten eines Benutzers eines bestimmten Typs.
     */
    List<Absence> findByUserAndType(User user, AbsenceType type);

    /**
     * Liefert alle Abwesenheiten mit einem bestimmten Status zurück.
     * @param status Der gewünschte Abwesenheitsstatus (PENDING, APPROVED, REJECTED)
     * @return Liste mit Abwesenheiten des angegebenen Status.
     */
    List<Absence> findByStatus(AbsenceStatus status);

    /**
     * Liefert alle Abwesenheiten eines Benutzers mit einem bestimmten Status zurück.
     * @param user Der Benutzer.
     * @param status Der gewünschte Abwesenheitsstatus.
     * @return Liste mit Abwesenheiten des Benutzers mit dem angegebenen Status.
     */
    List<Absence> findByUserAndStatus(User user, AbsenceStatus status);

    /**
     * Liefert alle Abwesenheiten zurück, die nicht genehmigt sind (PENDING oder REJECTED).
     * @return Liste der nicht genehmigten Abwesenheiten.
     */
    @Query("SELECT a FROM Absence a WHERE a.status = ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus.PENDING OR a.status = ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus.REJECTED")
    List<Absence> findByStatusNotApproved();

    /**
     * Liefert alle Abwesenheiten eines Benutzers zurück, die nicht genehmigt sind (PENDING oder REJECTED).
     * @param user Der Benutzer.
     * @return Liste der nicht genehmigten Abwesenheiten des Benutzers.
     */
    @Query("SELECT a FROM Absence a WHERE a.user = :user AND (a.status = ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus.PENDING OR a.status = ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus.REJECTED)")
    List<Absence> findByUserAndStatusNotApproved(@Param("user") User user);

    /**
     * Überprüft ob ein Benutzer einen bestimmten Tag abwesend war.
     * @param userId
     * @param date
     * @return true, wenn der Benutzer einen bestimmten Tag abwesend war, sonst false
     * Quelle: ChatGPT.com
     */
    @Query("SELECT COUNT(a) > 0 FROM Absence a WHERE a.user.id = :userId AND :date BETWEEN a.startDate AND a.endDate AND a.status = ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus.APPROVED")
    boolean hasApprovedAbsenceOnDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Summe der genehmigten Abwesenheiten eines Benutzers für einen bestimmten Zeitraum zurück.
     * @param userId
     * @param type
     * @param startDate
     * @param endDate
     * @return Long mit der Summe der genehmigten Abwesenheiten eines Benutzers für einen bestimmten Zeitraum.
     * @author FA
     */
    @Query(
            value = "SELECT SUM(DATEDIFF('DAY', a.start_date, a.end_date)) " +
                    "FROM absence a " +
                    "WHERE a.user_id   = :userId " +
                    "AND a.type      = :type " +
                    "AND a.start_date >= :from " +
                    "AND a.end_date   <= :to",
            nativeQuery = true
    )
    Long sumAbsenceDaysByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type")   AbsenceType type,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to
    );

    /**
     * Liefert alle aktuellen und zukünftigen Abwesenheiten eines Benutzers zurück.
     * @param userId
     * @param today
     * @return Liste mit aktuellen und zukünftigen Abwesenheiten eines Benutzers.
     * Quelle: ChatGPT.com
     */
    @Query("SELECT a FROM Absence a WHERE a.user.id = :userId AND a.endDate >= :today ORDER BY a.startDate ASC")
    List<Absence> findCurrentAndFutureAbsencesByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * Findet alle Abwesenheiten für eine Liste von Benutzern mit einem bestimmten Status.
     * @param users Eine Liste von Benutzern.
     * @param status Der gewünschte Abwesenheitsstatus.
     * @return Eine Liste von Abwesenheiten.
     */
    List<Absence> findByUserInAndStatus(List<User> users, AbsenceStatus status);

}
