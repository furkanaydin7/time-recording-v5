package ch.fhnw.timerecordingbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Filter zur JWT-Authentifizierung bei jeder Anfrage
 * Extrahiert und validiert das Token, setzt den Security-Kontext
 * Wurde mithilfe der Quelle https://medium.com/@victoronu/implementing-jwt-authentication-in-a-simple-spring-boot-application-with-java-b3135dbdb17b
 * erstellt.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Diese Methode wird bei jeder Anfrage automatisch aufgerufen.
     * Sie prüft, ob ein gültiges JWT vorhanden ist, und setzt ggf. den Benutzerkontext.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // JWT-Token wird aus dem Authorization-Header extrahiert
            String jwt = getJwtFromRequest(request);

            // Wenn ein JWT vorhanden ist und es gültig ist, wird der Benutzer im SecurityContext gesetzt
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Benutzernamen aus dem JWT extrahieren
                String username = tokenProvider.getUsernameFromJwt(jwt);

                // Benutzerrollen aus dem JWT extrahieren
                List<GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromJwt(jwt);

                // Benutzerdetails über das UserDetailsService-Interface laden
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Erstellung eines Authentication-Objekts mit den geladenen Benutzerdaten und Rollen
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);

                // Zusätzliche Details zur Anfrage hinzufügen (IP-Adresse, Session-ID etc.)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Authentifizierten Benutzer in den SecurityContext setzen
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Fehlerbehandlung bei Problemen mit dem Token oder der Authentifizierung
            logger.error("Could not set user authentication in security context", ex);
        }

        // Request weiterführen durch Filter
        filterChain.doFilter(request, response);
    }

    /**
     * Extrahiert das JWT aus dem Authorization-Header, sofern vorhanden.
     * Erwartetes Format: "Authorization: Bearer <token>"
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Prüft, ob der Header gesetzt ist und mit "Bearer " beginnt
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " abschneiden und nur das Token zurückgeben
            return bearerToken.substring(7);
        }
        return null;
    }
}
