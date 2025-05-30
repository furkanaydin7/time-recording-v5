package ch.fhnw.timerecordingbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entitätsklasse für Regestrierungs Anfragen
 * @author PD
 */
@Entity
@Table(name = "registration_requests")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "requested_role", nullable = false, length = 50)
    private String requestedRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Konstruktoren
    public Registration() { // KONSTRUKTORNAME GEÄNDERT
        this.createdAt = LocalDateTime.now();
    }

    public Registration(String firstName, String lastName, String email, String requestedRole, User manager) { // KONSTRUKTORNAME GEÄNDERT
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.requestedRole = requestedRole;
        this.manager = manager;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters und Setters
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

    public String getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
