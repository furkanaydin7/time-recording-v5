package ch.fhnw.timerecordingbackend.model;

import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import jakarta.persistence.*;

import ch.fhnw.timerecordingbackend.model.Role;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entität Klasse für Benutzer (Mitarbeiter/ Administratoren)
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.1 - Quellenbezeichnung angepasst
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * Verknüpfung von Benutzer mit Rollen
     * Mehrere Benutzer können mehrere Rollen haben
     * Quelle: ChatGPT.com
     */
    @ManyToMany(fetch = FetchType.EAGER)// zeitgleiches laden
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "planned_hours_per_day", nullable = false)
    private double plannedHoursPerDay = 8.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Absenzen und Zeiteinträge von Benutzern
     * Quelle: ChatGPT.com
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TimeEntry> timeEntries = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Absence> absences = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id") // Name der Fremdschlüsselspalte in der DB
    private User manager;

    /**
     * Konstruktoren
     */
    public User() {}

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return Vollständigen Namen (firstName + lastName)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Überprüfen ob bestimmte Rolle zugeordnet wurde
     * @param roleName Name der Rolle
     * @return true, wenn Benutzer bestimmte Rolle hat
     */
    public boolean hasRole(String roleName) {
        for (Role r : roles) {
            if (r.getName().equals(roleName))
                return true;
        }
        return false;
    }

    /**
     * User aktivieren und deaktivieren
     */
    public void deactivate() {
        this.active = false;
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Zeitstempel bei Änderungen aktualisieren
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Getter und Setter
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public double getPlannedHoursPerDay() {
        return plannedHoursPerDay;
    }

    public void setPlannedHoursPerDay(double plannedHoursPerDay) {
        this.plannedHoursPerDay = plannedHoursPerDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<TimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(Set<TimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public Set<Absence> getAbsences() {
        return absences;
    }

    public void setAbsences(Set<Absence> absences) {
        this.absences = absences;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", status=" + status +
                (manager != null ? ", managerId=" + manager.getId() : ", managerId=null") +
                '}';
    }
}
