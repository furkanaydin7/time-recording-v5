package ch.fhnw.timerecordingbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; // Diese Zeile hinzufügen
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // Diese Zeile hinzufügen
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bereitstellung und Validierung von JWTs für Authentifizierung und Autorisierung
 * Beinhaltet Token-Erstellung, Rollenextraktion und Blacklisting
 * Wurde mithilfe der Quelle https://medium.com/@victoronu/implementing-jwt-authentication-in-a-simple-spring-boot-application-with-java-b3135dbdb17b
 * erstellt.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
@Component
public class JwtTokenProvider {

    // Geheimschlüssel für die Signatur des Tokens, wird aus application.properties geladen
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // Token-Ablaufzeit in Millisekunden, wird auch aus application.properties geladen
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // Set zur Speicherung von invalidierten (z. B. ausgeloggten) Tokens
    private Set<String> blacklistedTokens = new HashSet<>();

    // Methode zur Generierung eines sicheren Schlüssels für HS512
    private SecretKey getSigningKey() {
        // Wenn jwtSecret leer ist, erstellen wir einen zufälligen Schlüssel
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }

        // Ansonsten verwenden wir den konfigurierten Schlüssel, stellen aber sicher,
        // dass er lang genug ist
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Erstellt ein JWT-Token auf Basis der übergebenen Authentifizierungsdaten.
     * Das Token enthält Username, Rollen und Ablaufzeit.
     */
    public String generateToken(Authentication authentication) {
        // Extrahiert UserDetails aus dem Authentication-Objekt
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Rollen des Benutzers als Strings extrahieren
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // z. B. "ROLE_USER", "ROLE_ADMIN"
                .collect(Collectors.toList());

        // Erstellt und signiert das JWT Token mit einem sicheren Schlüssel
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Benutzername als Subject
                .claim("roles", roles) // Rollen als Claim hinzufügen
                .setIssuedAt(new Date()) // Zeitpunkt der Ausstellung
                .setExpiration(expiryDate) // Ablaufdatum
                .signWith(getSigningKey()) // Verwenden Sie die neue Methode für den Schlüssel
                .compact(); // Finales Token-String erstellen
    }

    /**
     * Extrahiert den Benutzernamen (Subject) aus einem gültigen JWT
     */
    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Benutzername
    }

    /**
     * Extrahiert die Rollen aus dem Token und wandelt sie in Spring Security Authorities um
     */
    public List<GrantedAuthority> getAuthoritiesFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Holt Rolen als Liste aus dem "roles"-Claim
        List<String> roles = claims.get("roles", List.class);

        // Wandelt Rollen in SimpleGrantedAuthority-Objekte um
        return roles.stream()
                .map(SimpleGrantedAuthority::new) // Für jede Rolle
                .collect(Collectors.toList());
    }

    // Prüft die Gültigkeit des Tokens
    public boolean validateToken(String authToken) {
        // Prüft, ob Token auf der Blacklist steht
        if (blacklistedTokens.contains(authToken)) {
            return false; // Deaktiviert das Token, z. B. durch Logout
        }

        try {
            // Versucht das Token zu parsen und zu verifizieren
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true; // Token gültig
        } catch (SecurityException e) {
            // JWT Signatur ist ungültig
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            // Token-Format ist ungültig
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            // Token ist abgelaufen
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            // Token-Typ wird nicht unterstützt
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token-String ist leer oder null
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false; // Token ungültig
    }

    // Fügt Token zur Blacklist hinzu, z. B. bei Logout
    // Bei validateToken() gelten diese Tokens als ungültig
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
}