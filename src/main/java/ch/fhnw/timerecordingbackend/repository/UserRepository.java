package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für User Entitäten und CRUD Operationen
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * User nach E-Mail finden
     * @param email sucht nach E-Mail Adresse
     * @return Optional mit User, wenn gefunden, sonst Optional.empty()
     */
    Optional<User> findByEmail(String email);

    /**
     * Prüft ob User mit E-Mail vorhanden ist
     * @param email zu prüfende E-Mail Adresse
     * @return true, wenn E-Mail existiert, sonst false
     */
    boolean existsByEmail(String email);

    /**
     * Gibt alle aktiven User zurück
     * @return Liste mit aktiven Usern
     */
    List<User> findByActiveTrue();

    /**
     * Nutzer mit Status finden
     * @param status
     * @return Liste mit Nutzern mit gewünschten Status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Nutzer mit bestimmten Rollen finden
     * Query Many To Many Abfrage mit Hilfe von Quelle: chatgpt.com
     * @param roleName Name der Rolle
     * @return Liste mit Nutzern mit gewünschten Rollen
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Sucht User mit Suchbegriff in Name und E-Mail nach
     * @param searchTerm Suchbegriff
     * @return Liste mit gefundenen Usern
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Findet alle Benutzer, die dem angegebenen Manager direkt unterstellt sind.
     * @param manager Der Manager-Benutzer.
     * @return Eine Liste der direkt unterstellten Benutzer.
     */
    List<User> findByManager(User manager);

}
