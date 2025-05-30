package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für System Log Entitäten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    /**
     * Logs eines bestimmten Benutzer
     * @param userId
     * @return Liste mit Logs des Benutzers
     */
    List<SystemLog> findByUserId(Long userId);

    /**
     * Logs eines Benutzers mit Email finden
     * @param email
     * @return Liste der Logs des Benutzers
     */
    List<SystemLog> findByUserEmail(String email);

    /**
     * Logs mit bestimmten Aktionstypen
     * @param action
     * @return Liste der Logs
     */
    List<SystemLog> findByActionContaining(String action);

    /**
     * Findet Logs in einem bestimmten Zeitraum
     * @param start
     * @param end
     * @return
     */
    List<SystemLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Findet Logs einer bestimmten Entität
     * @param targetEntity
     * @param targetId
     * @return
     */
    List<SystemLog> findByTargetEntityAndTargetId(String targetEntity, Long targetId);

    /**
     * Logs mit Suchbegriff in Aktionen oder Details suchen
     * @param searchTerm
     * @return
     */
    @Query("SELECT l FROM SystemLog l WHERE LOWER(l.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(l.details) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SystemLog> searchLogs(@Param("searchTerm") String searchTerm);

    /**
     * Neuste Logs finden
     * @param pageable
     * @return
     * Quelle: ChatGPT.com
     */
    @Query("SELECT l FROM SystemLog l ORDER BY l.timestamp DESC")
    List<SystemLog> findLatestLogs(Pageable pageable);

    /**
     * Logs Eines bestimmten Benutzers innerhalb eines bestimmten Zeitraums
     * @param userId
     * @param start
     * @param end
     * @return
     */
    List<SystemLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Löscht Logs die älter als ein bestimmter Zeitpunkt sind
     * @param timestamp
     * @return
     */
    long deleteByTimestampBefore(LocalDateTime timestamp);

    /**
     * @author EK
     * @param userId
     * @param action
     * @return
     */
    List<SystemLog> findByUserIdAndActionAndProcessedStatusIsNullOrderByTimestampDesc(Long userId, String action);

    List<SystemLog> findByUserIdAndActionOrderByTimestampDesc(Long userId, String action);
    List<SystemLog> findByUserIdOrderByTimestampDesc(Long userId);
    List<SystemLog> findByUserIdAndActionAndProcessedStatusOrderByTimestampDesc(Long userId, String action, String processedStatus);
}