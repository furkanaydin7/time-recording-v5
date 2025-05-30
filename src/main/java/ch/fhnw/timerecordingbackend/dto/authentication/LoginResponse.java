package ch.fhnw.timerecordingbackend.dto.authentication;

import java.util.List;

/**
 * Antwort-DTO für den Login-Vorgang, enthält JWT-Token und Benutzerinformationen.
 * Beinhaltet eine innere UserDto-Klasse mit ID, Name und Rollen.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 * @version 1.1 - Rollenerkennung hinzugefügt PD
 */
public class LoginResponse {

    // Das vom Server generierte JWT-Token
    private String token;
    // Benutzerinformationen
    private UserDto user;

    // Standard-Konstruktur
    public LoginResponse() {}

    public LoginResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    // Getter und Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    /**
     * Innere statische Klasse zur Darstellung eines Benutzers in der Login-Antwort
     * Wird verwendet, um Benutzerinformationen wie ID, Name und Rolle sicher und strukturiert zurückzugeben
     */
    public static class UserDto {
        private Long id;
        private String name;
        private String role;
        private List<String> roles;

         // Standard-Konstruktur
        public UserDto() {}

        /**
         * Konstruktor mit Rollenliste und primärer Rollenerkennung.
         * @param id Benutzer-ID
         * @param name Benutzername
         * @param role Primärrolle
         * @param roles Liste aller Rollen
         */
        public UserDto(Long id, String name, String role, List<String> roles) {
            this.id = id;
            this.name = name;
            this.role = (roles != null && !roles.isEmpty()) ? roles.get(0) : "EMPLOYEE"; // Standardrolle "Employee" PD
            this.roles = roles;
        }

        // Einfacher Konstruktor für Tests und Kompatibilität PD
        public UserDto(Long id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.roles = List.of(role); // Single role als Liste
        }

        // Getter und Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        // Roles Getter und Setter PD
        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        // toString-Methode PD
        @Override
        public String toString() {
            return "UserDto{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", roles='" + roles + '\'' +
                    '}';
        }
    }
}
