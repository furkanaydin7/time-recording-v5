package ch.fhnw.timerecordingbackend.security;

import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility-Klasse zur Ermittlung des aktuell authentifizierten Benutzers
 * Stellt Methoden für Username- und Benutzerobjektzugriff bereit
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Component
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    /**
     * Gibt die E-Mail-Adresse des aktuell authentifizierten Benutzers zurück
     * Falls kein Benutzer authentifiziert ist, wird null zurückgegeben
     */
    public String getCurrentUsername() {
        // Holt das Authentication-Objekt aus dem aktuellen SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Gibt null zurück, wenn keine Authentifizierung vorhanden oder Benutzer nicht eingeloggt
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Der principal enthält den Benutzer
        Object principal = authentication.getPrincipal();

        // Gibt die E-Mail-Adresse zurück, wenn der principal ein UserDetails-Objekt ist
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        // Fallback für andere Principal-Typen
        return principal.toString();
    }

    /**
     * Gibt das vollständige User-Objekt des aktuell angemeldeten Benutzers zurück,
     * oder null, falls kein Benutzer angemeldet ist oder der Benutzer nicht gefunden wurde
     */
    public User getCurrentUser() {
        String email = getCurrentUsername(); // E-Mail abrufen
        return userRepository.findByEmail(email)
                .orElse(null); // Null, falls Benutzer nicht gefunden
    }
}
