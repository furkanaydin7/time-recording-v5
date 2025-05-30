// src/test/java/ch/fhnw/timerecordingbackend/service/UserServiceIntegrationTest.java
package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.RoleRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService; // The actual service
    @Autowired
    private UserRepository userRepository; // To verify persistence
    @Autowired
    private RoleRepository roleRepository; // To set up roles
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role employeeRole;
    private Role adminRole;
    private User managerUser;

    @BeforeEach
    void setUp() {
        employeeRole = roleRepository.findByName("EMPLOYEE").orElseGet(() -> {
            Role role = new Role("EMPLOYEE", "Employee Role");
            return roleRepository.save(role);
        });
        adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role role = new Role("ADMIN", "Admin Role");
            return roleRepository.save(role);
        });
        Role managerRole = roleRepository.findByName("MANAGER").orElseGet(() -> {
            Role role = new Role("MANAGER", "Manager Role");
            return roleRepository.save(role);
        });

        // Manager erstellen
        managerUser = userRepository.findByEmail("manager.test@example.com").orElseGet(() -> {
            User manager = new User("Manager", "Test", "manager.test@example.com", passwordEncoder.encode("password"));
            manager.addRole(managerRole);
            manager.setActive(true);
            manager.setStatus(UserStatus.ACTIVE);
            return userRepository.save(manager);
        });
    }

    @Test
    void createUser_Success_NoManager() {
        User newUser = new User("Jane", "Doe", "jane.doe@example.com", passwordEncoder.encode("password"));
        User createdUser = userService.createUser(newUser, "EMPLOYEE", null);

        assertNotNull(createdUser.getId());
        assertEquals("jane.doe@example.com", createdUser.getEmail());
        assertTrue(createdUser.hasRole("EMPLOYEE"));
        assertNull(createdUser.getManager());

        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("jane.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void createUser_Success_WithManager() {
        User newUser = new User("Alice", "Smith", "alice.smith@example.com", passwordEncoder.encode("password"));
        User createdUser = userService.createUser(newUser, "EMPLOYEE", managerUser.getId());

        assertNotNull(createdUser.getId());
        assertEquals("alice.smith@example.com", createdUser.getEmail());
        assertTrue(createdUser.hasRole("EMPLOYEE"));
        assertNotNull(createdUser.getManager());
        assertEquals(managerUser.getId(), createdUser.getManager().getId());

        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(managerUser.getId(), foundUser.get().getManager().getId());
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        User existingUser = new User("Existing", "User", "existing.user@example.com", passwordEncoder.encode("password"));
        userRepository.save(existingUser); // Save directly to simulate existing user

        User newUser = new User("Duplicate", "Email", "existing.user@example.com", passwordEncoder.encode("newpass"));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userService.createUser(newUser, "EMPLOYEE", null));

        assertEquals("Email existiert bereits", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        User userToUpdate = new User("Initial", "Name", "update.me@example.com", passwordEncoder.encode("pass"));
        userToUpdate.addRole(employeeRole);
        userRepository.save(userToUpdate);

        userToUpdate.setFirstName("Updated");
        userToUpdate.setPlannedHoursPerDay(7.0);
        userToUpdate.setActive(false);
        userToUpdate.setStatus(UserStatus.INACTIVE);

        User result = userService.updateUser(userToUpdate.getId(), userToUpdate);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals(7.0, result.getPlannedHoursPerDay());
        assertFalse(result.isActive());
        assertEquals(UserStatus.INACTIVE, result.getStatus());

        Optional<User> foundUser = userRepository.findById(userToUpdate.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated", foundUser.get().getFirstName());
    }

    @Test
    void deactivateUser_Success() {
        User userToDeactivate = new User("Active", "User", "active.user@example.com", passwordEncoder.encode("pass"));
        userToDeactivate.addRole(employeeRole);
        userRepository.save(userToDeactivate);

        User deactivatedUser = userService.deactivateUser(userToDeactivate.getId());

        assertFalse(deactivatedUser.isActive());
        assertEquals(UserStatus.INACTIVE, deactivatedUser.getStatus());

        Optional<User> foundUser = userRepository.findById(userToDeactivate.getId());
        assertTrue(foundUser.isPresent());
        assertFalse(foundUser.get().isActive());
    }

    @Test
    void activateUser_Success() {
        User userToActivate = new User("Inactive", "User", "inactive.user@example.com", passwordEncoder.encode("pass"));
        userToActivate.addRole(employeeRole);
        userToActivate.deactivate(); // Set to inactive initially
        userRepository.save(userToActivate);

        User activatedUser = userService.activateUser(userToActivate.getId());

        assertTrue(activatedUser.isActive());
        assertEquals(UserStatus.ACTIVE, activatedUser.getStatus());

        Optional<User> foundUser = userRepository.findById(userToActivate.getId());
        assertTrue(foundUser.isPresent());
        assertTrue(foundUser.get().isActive());
    }

    @Test
    void changePassword_Success() {
        User user = new User("Pwd", "Change", "pwd.change@example.com", passwordEncoder.encode("oldPassword"));
        user.addRole(employeeRole);
        userRepository.save(user);

        // Passwort ändern
        User foundUser = userRepository.findByEmail("pwd.change@example.com").get();
        assertTrue(passwordEncoder.matches("oldPassword", foundUser.getPassword()));
        foundUser.setPassword(passwordEncoder.encode("newPassword"));
        userRepository.save(foundUser);

        Optional<User> updatedUser = userRepository.findById(foundUser.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.get().getPassword()));
    }
}