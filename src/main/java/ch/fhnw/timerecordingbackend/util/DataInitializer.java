package ch.fhnw.timerecordingbackend.util;

import ch.fhnw.timerecordingbackend.model.*;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DataInitializer für Initialdaten beim Start der Anwendung
 * Erstellt Standard-Rollen, Admin-Benutzer und Beispieldaten
 * @author PD
 * Quelle: ChatGPT.com
 */

@Component
//@Profile("!test") // Für System test hinzufügen
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AbsenceRepository absenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("🚀 DataInitializer gestartet - Erstelle Initialdaten...");

            createRoles();
            createUsers();
            createProjects();
            initializeExampleAbsences();

            System.out.println("✅ Initialdaten erfolgreich erstellt!");
        }
    }

    /**
     * Erstellt Standard-Rollen
     */
    private void createRoles() {
        System.out.println("📋 Erstelle Standard-Rollen...");

        // Admin-Rolle
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("System Administrator");
            roleRepository.save(adminRole);
            System.out.println("  ✓ Rolle erstellt: ADMIN");
        }

        // Manager-Rolle
        if (!roleRepository.existsByName("MANAGER")) {
            Role managerRole = new Role();
            managerRole.setName("MANAGER");
            managerRole.setDescription("Project Manager");
            roleRepository.save(managerRole);
            System.out.println("  ✓ Rolle erstellt: MANAGER");
        }

        // Employee-Rolle
        if (!roleRepository.existsByName("EMPLOYEE")) {
            Role employeeRole = new Role();
            employeeRole.setName("EMPLOYEE");
            employeeRole.setDescription("Employee");
            roleRepository.save(employeeRole);
            System.out.println("  ✓ Rolle erstellt: EMPLOYEE");
        }
    }

    /**
     * Erstellt Standard-Benutzer
     */
    private void createUsers() {
        System.out.println("👤 Erstelle Admin-Benutzer...");
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new RuntimeException("Manager role not found"));
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        User adminUser = null;
        if (!userRepository.existsByEmail("admin@timerecording.ch")) {
            adminUser = new User();
            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setEmail("admin@timerecording.ch");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setActive(true);
            adminUser.setStatus(UserStatus.ACTIVE);
            adminUser.setPlannedHoursPerDay(8.0);
            adminUser.setRoles(Set.of(adminRole));
            // Admin hat keinen direkten Manager (manager Feld bleibt null)
            userRepository.save(adminUser);
            System.out.println("  ✓ Admin-Benutzer erstellt: admin@timerecording.ch (Passwort: admin123)");
        } else {
            adminUser = userRepository.findByEmail("admin@timerecording.ch").orElse(null);
        }


        System.out.println("👥 Erstelle Beispiel-Benutzer...");
        User managerMax = null;
        if (!userRepository.existsByEmail("manager@timerecording.ch")) {
            managerMax = new User();
            managerMax.setFirstName("Max");
            managerMax.setLastName("Manager");
            managerMax.setEmail("manager@timerecording.ch");
            managerMax.setPassword(passwordEncoder.encode("manager123"));
            managerMax.setActive(true);
            managerMax.setStatus(UserStatus.ACTIVE);
            managerMax.setPlannedHoursPerDay(8.0);
            managerMax.setRoles(Set.of(managerRole));
            if (adminUser != null) { // Annahme: Admin ist der Manager von Max Manager
                managerMax.setManager(adminUser);
            }
            userRepository.save(managerMax);
            System.out.println("  ✓ Manager erstellt: manager@timerecording.ch (Passwort: manager123)");
        } else {
            managerMax = userRepository.findByEmail("manager@timerecording.ch").orElse(null);
        }

        if (!userRepository.existsByEmail("anna.schmidt@timerecording.ch")) {
            User employeeAnna = new User();
            employeeAnna.setFirstName("Anna");
            employeeAnna.setLastName("Schmidt");
            employeeAnna.setEmail("anna.schmidt@timerecording.ch");
            employeeAnna.setPassword(passwordEncoder.encode("employee123"));
            employeeAnna.setActive(true);
            employeeAnna.setStatus(UserStatus.ACTIVE);
            employeeAnna.setPlannedHoursPerDay(8.0);
            employeeAnna.setRoles(Set.of(employeeRole));
            if (managerMax != null) { // Anna Schmidt berichtet an Max Manager
                employeeAnna.setManager(managerMax);
            }
            userRepository.save(employeeAnna);
            System.out.println("  ✓ Mitarbeiter erstellt: anna.schmidt@timerecording.ch (Passwort: employee123, Manager: Max Manager)");
        }
        createMoreUsers(managerMax); // managerMax an createMoreUsers übergeben
    }

    /**
     * Erstellt Beispiel-Mitarbeiter
     */
    private void createMoreUsers(User directManager) { // Parameter für direkten Manager
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        if (!userRepository.existsByEmail("peter.mueller@timerecording.ch")) {
            User peter = new User();
            peter.setFirstName("Peter");
            peter.setLastName("Müller");
            peter.setEmail("peter.mueller@timerecording.ch");
            peter.setPassword(passwordEncoder.encode("employee123"));
            peter.setActive(true);
            peter.setStatus(UserStatus.ACTIVE);
            peter.setPlannedHoursPerDay(8.0);
            peter.setRoles(Set.of(employeeRole));
            if (directManager != null) { peter.setManager(directManager); }
            userRepository.save(peter);
            System.out.println("  ✓ Mitarbeiter erstellt: peter.mueller@timerecording.ch (Manager: " + (directManager != null ? directManager.getFullName() : "N/A") + ")");
        }

        // Laura Weber
        if (!userRepository.existsByEmail("laura.weber@timerecording.ch")) {
            User laura = new User();
            laura.setFirstName("Laura");
            laura.setLastName("Weber");
            laura.setEmail("laura.weber@timerecording.ch");
            laura.setPassword(passwordEncoder.encode("employee123"));
            laura.setActive(true);
            laura.setStatus(UserStatus.ACTIVE);
            laura.setPlannedHoursPerDay(8.0);
            laura.setRoles(Set.of(employeeRole));
            if (directManager != null) { laura.setManager(directManager); }
            userRepository.save(laura);
            System.out.println("  ✓ Mitarbeiter erstellt: laura.weber@timerecording.ch (Passwort: employee123)");
        }

        // Thomas Fischer
        if (!userRepository.existsByEmail("thomas.fischer@timerecording.ch")) {
            User thomas = new User();
            thomas.setFirstName("Thomas");
            thomas.setLastName("Fischer");
            thomas.setEmail("thomas.fischer@timerecording.ch");
            thomas.setPassword(passwordEncoder.encode("employee123"));
            thomas.setActive(true);
            thomas.setStatus(UserStatus.ACTIVE);
            thomas.setPlannedHoursPerDay(8.0);
            thomas.setRoles(Set.of(employeeRole));
            if (directManager != null) { thomas.setManager(directManager); }
            userRepository.save(thomas);
            System.out.println("  ✓ Mitarbeiter erstellt: thomas.fischer@timerecording.ch (Passwort: employee123)");
        }
    }

    /**
     * Erstellt Beispiel-Projekte
     */
    private void createProjects() {
        System.out.println("📁 Erstelle Beispiel-Projekte...");

        User manager = userRepository.findByEmail("manager@timerecording.ch")
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Website Redesign
        if (!projectRepository.existsByName("Website Redesign")) {
            Project project1 = new Project();
            project1.setName("Website Redesign");
            project1.setDescription("Komplette Überarbeitung der Unternehmenswebsite");
            project1.setActive(true);
            project1.setManager(manager);
            projectRepository.save(project1);
            System.out.println("  ✓ Projekt erstellt: Website Redesign");
        }

        // Mobile App Development
        if (!projectRepository.existsByName("Mobile App Development")) {
            Project project2 = new Project();
            project2.setName("Mobile App Development");
            project2.setDescription("Entwicklung einer mobilen App für iOS und Android");
            project2.setActive(true);
            project2.setManager(manager);
            projectRepository.save(project2);
            System.out.println("  ✓ Projekt erstellt: Mobile App Development");
        }

        // Database Migration
        if (!projectRepository.existsByName("Database Migration")) {
            Project project3 = new Project();
            project3.setName("Database Migration");
            project3.setDescription("Migration der Legacy-Datenbank zu einer modernen Lösung");
            project3.setActive(true);
            project3.setManager(manager);
            projectRepository.save(project3);
            System.out.println("  ✓ Projekt erstellt: Database Migration");
        }
    }

    /**
     * Erstellt Beispiel-Abwesenheiten
     */
    private void initializeExampleAbsences() {
        System.out.println("🏖️ Erstelle Beispiel-Abwesenheiten...");

        User laura = userRepository.findByEmail("laura.weber@timerecording.ch").orElse(null);
        User thomas = userRepository.findByEmail("thomas.fischer@timerecording.ch").orElse(null);
        User admin = userRepository.findByEmail("admin@timerecording.ch").orElse(null);

        if (laura != null) {
            // Genehmigter Urlaub für Laura (nächste Woche)
            Absence vacation = new Absence();
            vacation.setUser(laura);
            vacation.setStartDate(LocalDate.now().plusDays(7));
            vacation.setEndDate(LocalDate.now().plusDays(11));
            vacation.setType(AbsenceType.VACATION);
            vacation.setStatus(AbsenceStatus.APPROVED);
            vacation.setApprover(admin);
            vacation.setCreatedAt(LocalDateTime.now());
            vacation.setUpdatedAt(LocalDateTime.now());

            absenceRepository.save(vacation);
            System.out.println("  ✓ Urlaub für Laura Weber erstellt (genehmigt)");
        }

        if (thomas != null) {
            // Ausstehende Fortbildung für Thomas
            Absence training = new Absence();
            training.setUser(thomas);
            training.setStartDate(LocalDate.now().plusDays(14));
            training.setEndDate(LocalDate.now().plusDays(16));
            training.setType(AbsenceType.TRAINING);
            training.setStatus(AbsenceStatus.PENDING);
            training.setCreatedAt(LocalDateTime.now());
            training.setUpdatedAt(LocalDateTime.now());

            absenceRepository.save(training);
            System.out.println("  ✓ Fortbildung für Thomas Fischer erstellt (ausstehend)");
        }

        // Zusätzliche Beispiel-Abwesenheiten für mehr Testdaten
        User anna = userRepository.findByEmail("anna.schmidt@timerecording.ch").orElse(null);
        if (anna != null) {
            // Home Office für Anna (diese Woche)
            Absence homeOffice = new Absence();
            homeOffice.setUser(anna);
            homeOffice.setStartDate(LocalDate.now().plusDays(2));
            homeOffice.setEndDate(LocalDate.now().plusDays(4));
            homeOffice.setType(AbsenceType.HOME_OFFICE);
            homeOffice.setStatus(AbsenceStatus.APPROVED);
            homeOffice.setApprover(admin);
            homeOffice.setCreatedAt(LocalDateTime.now());
            homeOffice.setUpdatedAt(LocalDateTime.now());

            absenceRepository.save(homeOffice);
            System.out.println("  ✓ Home Office für Anna Schmidt erstellt (genehmigt)");
        }

        User peter = userRepository.findByEmail("peter.mueller@timerecording.ch").orElse(null);
        if (peter != null) {
            // Ausstehender Sonderurlaub für Peter
            Absence specialLeave = new Absence();
            specialLeave.setUser(peter);
            specialLeave.setStartDate(LocalDate.now().plusDays(21));
            specialLeave.setEndDate(LocalDate.now().plusDays(21)); // Nur ein Tag
            specialLeave.setType(AbsenceType.SPECIAL_LEAVE);
            specialLeave.setStatus(AbsenceStatus.PENDING);
            specialLeave.setCreatedAt(LocalDateTime.now());
            specialLeave.setUpdatedAt(LocalDateTime.now());

            absenceRepository.save(specialLeave);
            System.out.println("  ✓ Sonderurlaub für Peter Müller erstellt (ausstehend)");
        }
    }
}