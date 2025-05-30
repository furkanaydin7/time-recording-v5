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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/dashboard.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/auth/**",
                                "/api/public/registration-requests",
                                "/api/public/managers",
                                "/favicon.ico",
                                "/api/users/request-password-reset"
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/change-password").authenticated() // Passwort ändern nur für authentifizierte User
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN") // Alle /api/admin/** Endpunkte nur für Admins
                        // .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/projects/manage/**").hasAnyAuthority("ADMIN", "MANAGER") // Beispiel für Manager-Rechte
                        .requestMatchers(
                                "/api/time-entries/**",
                                "/api/projects/**",
                                "/api/reports/**",
                                "/api/absences/**"
                        ).hasAnyAuthority("ADMIN", "MANAGER", "EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin.disable()) // Standard-Form-Login deaktivieren, da JWT verwendet wird
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic Auth deaktivieren
                .addFilterBefore( // JWT-Filter vor dem Standard-Username/Password-Filter einfügen
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}