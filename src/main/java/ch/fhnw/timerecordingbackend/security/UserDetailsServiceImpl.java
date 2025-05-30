package ch.fhnw.timerecordingbackend.security;


import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementierung von UserDetailsService zur Benutzerladung via E-Mail
 * Wandelt Benutzerrollen in Spring Security Authorities um
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

/**
 * Lädt den Benutzer anhand der E-Mail-Adresse und wandelt ihn in ein UserDetails-Objekt um.
 * Wird von Spring Security intern beim Login-Versuch aufgerufen.
 */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Sucht den Benutzer in der Datenbank anhand der E-Mail-Adresse
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Wandelt Benutzerrollen in Spring Security Authorities um (z. B. "ROLE_ADMIN")
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Erstellt ein UserDetails-Objekt mit E-Mail, Passwort und Rollen
        // Die Booleans definieren Status wie Aktivität, Ablauf, Sperre
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // E-Mail
                user.getPassword(), // Verschlüsseltes Passwort
                user.isActive(), // Ob Benutzer aktiv ist
                true,   // // Account nicht abgelaufen
                true,   // // Credentials nicht abgelaufen
                true,   // // Account nicht gesperrt
                authorities // Rollen
        );
    }
}
