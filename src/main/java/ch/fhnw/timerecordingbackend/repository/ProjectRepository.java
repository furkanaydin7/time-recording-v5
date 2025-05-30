package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Projekt Entität
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Projekt nach Name finden
     * @param name
     * @return Optional mit Projekt, wenn gefunden, sonst Optional.empty()
     */
    Optional<Project> findByName(String name);

    /**
     * Prüft ob Projekt mit Name vorhanden ist
     * @param name
     * @return true, wenn Projekt existiert, sonst false
     */
    boolean existsByName(String name);

    /**
     * Gibt alle aktiven Projekte zurück
     * @return Liste mit aktiven Projekten
     */
    List<Project> findByActiveTrue();

    /**
     * Gibt alle Projekte eines Manager zurück
     * @param id
     * @return Liste mit Projekten eines Managers
     */
    List<Project> findByManagerId(Long id);

    /**
     * Gibt alle aktiven Projekte eines Manager zurück
     * @param id
     * @return Liste mit aktiven Projekten eines Managers
     */
    List<Project> findByManagerIdAndActiveTrue(Long id);

    /**
     * Sucht Projekte mit Suchbegriff
     * @param searchTerm Suchbegriff
     * @return Liste mit gefundenen Projekten
     * Quelle: ChatGPT.com
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Project> searchProjects(@Param("searchTerm") String searchTerm);

    /**
     * Sucht Projekten mit User ID
     * @param userId
     * @return Liste mit gefundenen Projekten
     * Quelle: ChatGPT.com
    */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.timeEntries t WHERE t.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);

    /**
     * Sucht aktive Projekten mit User ID
     * @param userId
     * @return Liste mit gefundenen aktiven Projekten
     * Quelle: ChatGPT.com
    */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.timeEntries t WHERE t.user.id = :userId AND p.active = true")
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

    /**
     * Zählt Benutzer die an einem Projekt gearbeitet haben
     * @param projectId
     * @return
     */
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM TimeEntry t WHERE t.project.id = :projectId")
    long countUsersByProjectId(@Param("projectId") Long projectId);
}
