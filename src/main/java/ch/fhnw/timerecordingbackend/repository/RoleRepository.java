package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für Rollen Entitäten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Liefert eine Rolle mit dem angegebenen Namen zurück.
     * @param name
     * @return Optional mit Rolle, wenn gefunden, sonst Optional.empty()
     */
    Optional<Role> findByName(String name);

    /**
     * Prüft ob eine Rolle mit dem angegebenen Namen existiert.
     * @param name
     * @return true, wenn Rolle existiert, sonst false
     */
    boolean existsByName(String name);
}
