package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.authentication.ChangePasswordRequest;
import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * UserService Interface
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
public interface UserService {

    /**
     * Liefert alle Benutzer zurück
     * @return Liste mit allen Benutzern
     */
    List<User> findAllUsers();

    /**
     * Liefert alle aktiven Benutzer zurück
     * @return Liste mit allen aktiven Benutzern
     */
    List<User> findActiveUsers();

    /**
     * User anhand der ID finden
     * @param id
     * @return Optional mit User, wenn gefunden, sonst Optional.empty()
     */
    Optional<User> findById(Long id);

    /**
     * User anhand der E-Mail finden
     * @param email
     * @return Optional mit User, wenn gefunden, sonst Optional.empty()
     */
    Optional<User> findByEmail(String email);

    /**
     * Neuen User erstellen und mit Rolle verknüpfen
     * @param user
     * @param roleName
     * @param managerId (Optional) ID des zuzuweisenden Managers
     * @return neuer User mit Rolle und ID
     */
    User createUser(User user, String roleName, Long managerId);

    /**
     * User aktualisieren
     * @param id
     * @param updatedUser neuer User mit allen Feldern
     * @return aktualisierten User
     */
    User updateUser(Long id, User updatedUser);

    /**
     * Passwort des Users aktualisieren
     * @param id
     * @param oldPassword
     * @param newPassword
     * @return true, wenn Passwort aktualisiert wurde, sonst false
     */
    boolean updatePassword (Long id, String oldPassword, String newPassword);

    /**
     * Ändert das Passwort für den angemeldeten User.
     * @param request enthält userId, altes und neues Passwort
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Passwort zurücksetzen
     * @param userId
     * @return neues Passwort
     */
    String resetPasswordToTemporary(Long userId);

    /**
     * User deaktivieren
     * @param id
     */
    User deactivateUser(Long id);

    /**
     * User aktivieren
     * @param id
     */
    User activateUser(Long id);

    /**
     * User Status aktualisieren
     * @param id
     * @param status
     * @return aktualisierten User
     */
    User updateUserStatus(Long id, UserStatus status);

    /**
     * Rolle zu User hinzufügen
     * @param id
     * @param roleName
     * @return neuen User mit Rolle und ID
     */
    User addRoleToUser(Long id, String roleName);

    /**
     * Rolle von User entfernen
     * @param id
     * @param roleName
     * @return entfernter User
     */
    User removeRoleFromUser(Long id, String roleName);

    /**
     * Sucht User mit Suchbegriff
     * @param searchTerm Suchbegriff
     * @return Liste mit gefundenen Usern
     */
    List<User> searchUsers(String searchTerm);

    /**
     * Prüft ob User mit ID Admin ist
     * @param id
     * @return true, wenn Admin ist, sonst false
     */
    boolean isAdmin (Long id);

    /**
     * Prüft ob User mit ID Project Manager ist
     * @param id
     * @return true, wenn Project Manager ist, sonst false
     */
    boolean isProjectManager(Long id);

    /**
     * Liefert alle Rollen zurück
     * @return Liste mit allen Rollen
     */
    List<Role> getAllRoles();
    boolean requestPasswordReset(String email); // <-- Stelle sicher, dass diese Zeile genau so existiert

}
