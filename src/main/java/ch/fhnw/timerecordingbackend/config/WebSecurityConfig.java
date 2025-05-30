package ch.fhnw.timerecordingbackend.config;

import ch.fhnw.timerecordingbackend.security.JwtAuthenticationFilter;
import ch.fhnw.timerecordingbackend.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Zentrale Sicherheitskonfiguration für die Webanwendung.
 * Stellt die Zugriffskontrolle, den JWT-Filter und die URL-Autorisierungen bereit.
 * Verwendet Stateless-Authentifizierung (kein HTTP-Session-State).
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Passwort-Verschlüsselung mit BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Hauptkonfiguration der HTTP-Security.
     * Definiert, welche Endpunkte öffentlich, gesichert oder rollenbasiert erreichbar sind.
     * Bindet den JWT-Filter ein.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Konfiguriere URL-basierte Zugriffskontrolle
                .authorizeHttpRequests(auth -> auth
                        // Öffentliche Seiten und statische Ressourcen
                        .requestMatchers(
                                "/", "/index.html", "/login.html", "/dashboard.html",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico"
                        ).permitAll()

                        // Öffentliche API-Endpunkte (z. B. Login, Registrierung, Passwort-Reset)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/registration-requests",
                                "/api/public/managers",
                                "/api/users/request-password-reset"
                        ).permitAll()

                        // Nur eingeloggte User dürfen Passwort ändern
                        .requestMatchers(HttpMethod.PUT, "/api/users/change-password").authenticated()

                        // Nur Admins & Manager dürfen die Benutzerliste sehen
                        .requestMatchers("/api/admin/users").hasAnyAuthority("ADMIN", "MANAGER")

                        // Nur Admins dürfen auf alle Admin-Endpunkte zugreifen
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // Projektverwaltung (PUT /api/projects/{id}) nur für Admin & Manager
                        .requestMatchers(HttpMethod.PUT, "/api/projects/{id}").hasAnyAuthority("ADMIN", "MANAGER")

                        // Weitere projektbezogene Verwaltung nur für Admin & Manager
                        .requestMatchers("/api/projects/manage/**").hasAnyAuthority("ADMIN", "MANAGER")

                        // Zugriff auf Kernfunktionen (Zeiterfassung, Projekte, Reports, Absenzen)
                        .requestMatchers(
                                "/api/time-entries/**",
                                "/api/projects/**",
                                "/api/reports/**",
                                "/api/absences/**"
                        ).hasAnyAuthority("ADMIN", "MANAGER", "EMPLOYEE")

                        // Alle anderen Anfragen erfordern Authentifizierung
                        .anyRequest().authenticated()
                )

                // Deaktiviere Standard-Login-Formular (da JWT verwendet wird)
                .formLogin(formLogin -> formLogin.disable())

                // Deaktiviere HTTP Basic Auth (ebenfalls wegen JWT)
                .httpBasic(httpBasic -> httpBasic.disable())

                // Füge den JWT-Filter vor dem UsernamePasswordAuthenticationFilter ein
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}