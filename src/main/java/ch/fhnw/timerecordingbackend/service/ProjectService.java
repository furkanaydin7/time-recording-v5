package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Project;
import ch.fhnw.timerecordingbackend.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface für Projekte
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public interface ProjectService {

    /**
     * Findet alle Projekte
     * @return Liste aller Projekte
     */
    List<Project> findAllProjects();

    /**
     * Findet alle aktiven Projekte
     * @return Liste aller aktiven Projekte
     */
    List<Project> findActiveProjects();

    /**
     * Findet ein Projekt anhand ID
     * @param id
     * @return Optional mit dem gefundenen Projekt oder leer, wenn nicht gefunden
     */
    Optional<Project> findById(Long id);

    /**
     * Findet ein Projekt anhand des Namens
     * @param name
     * @return Optional mit dem gefundenen Projekt oder leer, wenn nicht gefunden
     */
    Optional<Project> findByName(String name);

    /**
     * Erstellt ein neues Projekt
     * @param project
     * @return Das erstellte Projekt mit ID
     */
    Project createProject(Project project);

    /**
     * Aktualisiert die Daten eines Projekts
     * @param id
     * @param updatedProject
     * @return Das aktualisierte Projekt
     */
    Project updateProject(Long id, Project updatedProject);

    /**
     * Deaktiviert ein Projekt
     * @param id
     * @return Das deaktivierte Projekt
     */
    Project deactivateProject(Long id);

    /**
     * Aktiviert ein Projekt
     * @param id
     * @return Das aktivierte Projekt
     */
    Project activateProject(Long id);

    /**
     * Weist einem Projekt einen Manager zu
     * @param projectId
     * @param managerId
     * @return Das aktualisierte Projekt
     */
    Project assignManager(Long projectId, Long managerId);

    /**
     * Entfernt den Manager von einem Projekt
     * @param projectId
     * @return Das aktualisierte Projekt
     */
    Project removeManager(Long projectId);

    /**
     * Findet aktive Projekte
     * @return
     */
    List<Project> findByActiveTrue();

    /**
     * Findet alle Projekte eines bestimmten Managers
     * @param managerId
     * @return Liste aller Projekte des Managers
     */
    List<Project> findByManagerId(Long managerId);

    /**
     * Findet alle aktiven Projekte eines bestimmten Managers
     * @param managerId
     * @return Liste aller aktiven Projekte des Managers
     */
    List<Project> findByManagerIdAndActiveTrue(Long managerId);

    /**
     * Sucht Projekte nach Name oder Beschreibung
     * @param searchTerm
     * @return Liste der passenden Projekte
     */
    List<Project> searchProjects(String searchTerm);

    /**
     * Findet alle Projekte an denen ein bestimmter Benutzer gearbeitet hat
     * @param userId
     * @return Liste der Projekte des Benutzers
     */
    List<Project> findProjectsByUserId(Long userId);

    /**
     * Findet alle aktiven Projekte an denen ein bestimmter Benutzer gearbeitet hat
     * @param userId
     * @return Liste der aktiven Projekte des Benutzers
     */
    List<Project> findActiveProjectsByUserId(Long userId);

    /**
     * Findet alle Projekte von einem bestimmten Manager
     * @param managerId
     * @return Liste der verwalteten Projekte
     */
    List<Project> findProjectsByManagerId(Long managerId);

    /**
     * Findet alle aktiven Projekte von einem bestimmten Manager
     * @param managerId Die ID des Managers
     * @return Liste der aktiven verwalteten Projekte
     */
    List<Project> findActiveProjectsByManagerId(Long managerId);


    /**
     * Prüft ob Projektname bereits existiert
     * @param name
     * @return true, wenn der Name bereits existiert, sonst false
     */
    boolean existsByName(String name);

    /**
     * Zählt die Anzahl der Benutzer, die an einem Projekt gearbeitet haben
     * @param projectId
     * @return Anzahl der Benutzer
     */
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM TimeEntry t WHERE t.project.id = :projectId")
    long countUsersByProjectId(@Param("projectId") Long projectId);

    /**
     * ActualHours für Projekt berechnen
     * @param projectId
     * @return
     */
    String calculateTotalActualHoursForProject(Long projectId);

    /**
     * Mitarbeiter für Projekt finden
     * @param projectId
     * @return
     */
    List<User> findUsersByProjectId(Long projectId);

}
