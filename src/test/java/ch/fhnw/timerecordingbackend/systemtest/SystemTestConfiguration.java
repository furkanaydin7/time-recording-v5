package ch.fhnw.timerecordingbackend.systemtest;

import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.RoleRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Konfiguration für Systemtests
 * Erstellt Testdaten für die Systemtests
 * @author PD
 * Quelle: ChatGPT.com
 */
@Configuration
@Profile("test")
public class SystemTestConfiguration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Erstellt Testdaten für die Systemtests
     */
    @Bean
    CommandLineRunner initTestData() {
        return args -> {
            if (userRepository.count() == 0) {
                System.out.println("Initialisiere Testdaten für Systemtests...");
                createTestRoles();
                createTestUsers();
                System.out.println("Testdaten erfolgreich erstellt!");
            }
        };
    }

    private void createTestRoles() {
        // Admin
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("System Administrator");
            roleRepository.save(adminRole);
            System.out.println("  ✓ Admin-Rolle erstellt");
        }

        // Manager
        if (!roleRepository.existsByName("MANAGER")) {
            Role managerRole = new Role();
            managerRole.setName("MANAGER");
            managerRole.setDescription("Project Manager");
            roleRepository.save(managerRole);
            System.out.println("  ✓ Manager-Rolle erstellt");
        }

        // Employee
        if (!roleRepository.existsByName("EMPLOYEE")) {
            Role employeeRole = new Role();
            employeeRole.setName("EMPLOYEE");
            employeeRole.setDescription("Employee");
            roleRepository.save(employeeRole);
            System.out.println("  ✓ Employee-Rolle erstellt");
        }
    }

    private void createTestUsers() {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new RuntimeException("Manager role not found"));
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        // Admin
        if (!userRepository.existsByEmail("admin@timerecording.ch")) {
            User adminUser = new User();
            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setEmail("admin@timerecording.ch");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setActive(true);
            adminUser.setStatus(UserStatus.ACTIVE);
            adminUser.setPlannedHoursPerDay(8.0);
            adminUser.setRoles(Set.of(adminRole));
            userRepository.save(adminUser);
            System.out.println("  ✓ Admin-Benutzer erstellt: admin@timerecording.ch");
        }

        // Manager
        if (!userRepository.existsByEmail("manager@timerecording.ch")) {
            User managerUser = new User();
            managerUser.setFirstName("Max");
            managerUser.setLastName("Manager");
            managerUser.setEmail("manager@timerecording.ch");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setActive(true);
            managerUser.setStatus(UserStatus.ACTIVE);
            managerUser.setPlannedHoursPerDay(8.0);
            managerUser.setRoles(Set.of(managerRole));
            userRepository.save(managerUser);
            System.out.println("  ✓ Manager-Benutzer erstellt: manager@timerecording.ch");
        }

        // Employee
        if (!userRepository.existsByEmail("anna.schmidt@timerecording.ch")) {
            User employeeUser = new User();
            employeeUser.setFirstName("Anna");
            employeeUser.setLastName("Schmidt");
            employeeUser.setEmail("anna.schmidt@timerecording.ch");
            employeeUser.setPassword(passwordEncoder.encode("employee123"));
            employeeUser.setActive(true);
            employeeUser.setStatus(UserStatus.ACTIVE);
            employeeUser.setPlannedHoursPerDay(8.0);
            employeeUser.setRoles(Set.of(employeeRole));

            // Manager zuweisen
            User manager = userRepository.findByEmail("manager@timerecording.ch").orElse(null);
            if (manager != null) {
                employeeUser.setManager(manager);
            }

            userRepository.save(employeeUser);
            System.out.println("  ✓ Employee-Benutzer erstellt: anna.schmidt@timerecording.ch");
        }
    }
}