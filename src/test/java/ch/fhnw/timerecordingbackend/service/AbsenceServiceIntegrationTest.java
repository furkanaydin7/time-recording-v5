
package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Absence;
import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceStatus;
import ch.fhnw.timerecordingbackend.model.enums.AbsenceType;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.AbsenceRepository;
import ch.fhnw.timerecordingbackend.repository.RoleRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AbsenceServiceIntegrationTest {

    @Autowired
    private AbsenceService absenceService;
    @Autowired
    private AbsenceRepository absenceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User employeeUser;
    private User managerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        roleRepository.findByName("EMPLOYEE").orElseGet(() -> roleRepository.save(new Role("EMPLOYEE")));
        roleRepository.findByName("MANAGER").orElseGet(() -> roleRepository.save(new Role("MANAGER")));
        roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        employeeUser = userRepository.findByEmail("absencetest.employee@example.com").orElseGet(() -> {
            User user = new User("Absence", "Employee", "absencetest.employee@example.com", passwordEncoder.encode("password"));
            user.addRole(roleRepository.findByName("EMPLOYEE").get());
            user.setActive(true);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });

        managerUser = userRepository.findByEmail("absencetest.manager@example.com").orElseGet(() -> {
            User user = new User("Absence", "Manager", "absencetest.manager@example.com", passwordEncoder.encode("password"));
            user.addRole(roleRepository.findByName("MANAGER").get());
            user.setActive(true);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });

        adminUser = userRepository.findByEmail("absencetest.admin@example.com").orElseGet(() -> {
            User user = new User("Absence", "Admin", "absencetest.admin@example.com", passwordEncoder.encode("password"));
            user.addRole(roleRepository.findByName("ADMIN").get());
            user.setActive(true);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });

        // Set manager for employee
        employeeUser.setManager(managerUser);
        userRepository.save(employeeUser);
    }

    @Test
    void createAbsence_Success() {
        Absence newAbsence = new Absence(employeeUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), AbsenceType.VACATION);
        Absence createdAbsence = absenceService.createAbsence(newAbsence);

        assertNotNull(createdAbsence.getId());
        assertEquals(AbsenceStatus.PENDING, createdAbsence.getStatus());
        assertEquals(employeeUser.getId(), createdAbsence.getUser().getId());

        Optional<Absence> foundAbsence = absenceRepository.findById(createdAbsence.getId());
        assertTrue(foundAbsence.isPresent());
        assertEquals(AbsenceType.VACATION, foundAbsence.get().getType());
    }

    @Test
    void createAbsence_OverlappingDates_ThrowsException() {
        Absence existingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), AbsenceType.VACATION);
        absenceService.createAbsence(existingAbsence);

        Absence overlappingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(7), LocalDate.now().plusDays(12), AbsenceType.ILLNESS);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                absenceService.createAbsence(overlappingAbsence));

        assertTrue(exception.getMessage().contains("überschneidet sich"));
    }

    @Test
    void updateAbsence_Success() {
        Absence existingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), AbsenceType.VACATION);
        absenceService.createAbsence(existingAbsence);

        Absence updatedAbsenceData = new Absence(employeeUser, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), AbsenceType.HOME_OFFICE);

        Absence result = absenceService.updateAbsence(existingAbsence.getId(), updatedAbsenceData);

        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(10), result.getStartDate());
        assertEquals(AbsenceType.HOME_OFFICE, result.getType());

        Optional<Absence> foundAbsence = absenceRepository.findById(existingAbsence.getId());
        assertTrue(foundAbsence.isPresent());
        assertEquals(LocalDate.now().plusDays(10), foundAbsence.get().getStartDate());
    }

    @Test
    void deleteAbsence_Success() {
        Absence absenceToDelete = new Absence(employeeUser, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), AbsenceType.OTHER);
        absenceService.createAbsence(absenceToDelete);

        absenceService.deleteAbsence(absenceToDelete.getId());

        Optional<Absence> deletedAbsence = absenceRepository.findById(absenceToDelete.getId());
        assertFalse(deletedAbsence.isPresent());
    }

    @Test
    void approveAbsence_ByAdmin_Success() {
        Absence pendingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(15), LocalDate.now().plusDays(17), AbsenceType.TRAINING);
        absenceService.createAbsence(pendingAbsence);

        Absence approvedAbsence = absenceService.approveAbsence(pendingAbsence.getId(), adminUser.getId());

        assertEquals(AbsenceStatus.APPROVED, approvedAbsence.getStatus());
        assertEquals(adminUser.getId(), approvedAbsence.getApprover().getId());

        Optional<Absence> foundAbsence = absenceRepository.findById(approvedAbsence.getId());
        assertTrue(foundAbsence.isPresent());
        assertEquals(AbsenceStatus.APPROVED, foundAbsence.get().getStatus());
    }

    @Test
    void approveAbsence_ByDirectManager_Success() {
        Absence pendingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), AbsenceType.HOME_OFFICE);
        absenceService.createAbsence(pendingAbsence);

        Absence approvedAbsence = absenceService.approveAbsence(pendingAbsence.getId(), managerUser.getId());

        assertEquals(AbsenceStatus.APPROVED, approvedAbsence.getStatus());
        assertEquals(managerUser.getId(), approvedAbsence.getApprover().getId());
    }

    @Test
    void approveAbsence_NotAuthorized_ThrowsAccessDeniedException() {
        Absence pendingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(25), LocalDate.now().plusDays(27), AbsenceType.OTHER);
        absenceService.createAbsence(pendingAbsence);

        User unauthorizedUser = userRepository.findByEmail("unauthorized.user@example.com").orElseGet(() -> {
            User user = new User("Unauthorized", "User", "unauthorized.user@example.com", passwordEncoder.encode("password"));
            user.addRole(roleRepository.findByName("EMPLOYEE").get());
            user.setActive(true);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });

        assertThrows(AccessDeniedException.class, () ->
                absenceService.approveAbsence(pendingAbsence.getId(), unauthorizedUser.getId()));
    }

    @Test
    void rejectAbsence_ByAdmin_Success() {
        Absence pendingAbsence = new Absence(employeeUser, LocalDate.now().plusDays(30), LocalDate.now().plusDays(32), AbsenceType.ILLNESS);
        absenceService.createAbsence(pendingAbsence);

        Absence rejectedAbsence = absenceService.rejectAbsence(pendingAbsence.getId(), adminUser.getId());

        assertEquals(AbsenceStatus.REJECTED, rejectedAbsence.getStatus());
        assertNull(rejectedAbsence.getApprover());

        Optional<Absence> foundAbsence = absenceRepository.findById(rejectedAbsence.getId());
        assertTrue(foundAbsence.isPresent());
        assertEquals(AbsenceStatus.REJECTED, foundAbsence.get().getStatus());
    }
}