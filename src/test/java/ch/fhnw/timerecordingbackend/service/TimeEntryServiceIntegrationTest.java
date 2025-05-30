// src/test/java/ch/fhnw/timerecordingbackend/service/TimeEntryServiceIntegrationTest.java
package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;
import ch.fhnw.timerecordingbackend.model.Project;
import ch.fhnw.timerecordingbackend.model.TimeEntry;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.ProjectRepository;
import ch.fhnw.timerecordingbackend.repository.TimeEntryRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import ch.fhnw.timerecordingbackend.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Mock SecurityUtils
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TimeEntryServiceIntegrationTest {

    @Autowired
    private TimeEntryService timeEntryService;
    @Autowired
    private TimeEntryRepository timeEntryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SecurityUtils securityUtils;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("time.test@example.com").orElseGet(() -> {
            User user = new User("Time", "Tester", "time.test@example.com", passwordEncoder.encode("password"));
            user.setActive(true);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });

        testProject = projectRepository.findByName("Test Project for Time").orElseGet(() -> {
            Project project = new Project("Test Project for Time", "Description");
            project.setActive(true);
            return projectRepository.save(project);
        });

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void createTimeEntry_Success() {
        TimeEntryRequest request = new TimeEntryRequest();
        request.setDate(LocalDate.now());
        request.setStartTimes(List.of("08:00"));
        request.setEndTimes(List.of("17:00"));
        request.setBreaks(List.of(new TimeEntryRequest.BreakTime("12:00", "12:30")));
        request.setProjectId(testProject.getId());

        TimeEntryResponse response = timeEntryService.createTimeEntry(request);

        assertNotNull(response.getId());
        assertEquals(LocalDate.now(), response.getDate());
        assertEquals("08:30", response.getActualHours());
        assertEquals(testProject.getId(), response.getProject().getId());

        Optional<TimeEntry> savedEntry = timeEntryRepository.findById(response.getId());
        assertTrue(savedEntry.isPresent());
        assertEquals(testUser.getId(), savedEntry.get().getUser().getId());
    }

    @Test
    void createTimeEntry_DuplicateEntry_ThrowsException() {
        TimeEntry initialEntry = new TimeEntry();
        initialEntry.setUser(testUser);
        initialEntry.setDate(LocalDate.now());
        initialEntry.addStartTime(LocalTime.of(8,0));
        initialEntry.addEndTime(LocalTime.of(9,0));
        initialEntry.setActualHours("01:00");
        initialEntry.setPlannedHours("08:00");
        initialEntry.setDifference("-07:00");
        timeEntryRepository.save(initialEntry);

        TimeEntryRequest request = new TimeEntryRequest();
        request.setDate(LocalDate.now());
        request.setStartTimes(List.of("10:00"));
        request.setEndTimes(List.of("11:00"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                timeEntryService.createTimeEntry(request));

        assertTrue(exception.getReason().contains("bereits ein Zeiteintrag"));
    }

    @Test
    void updateTimeEntry_Success() {
        TimeEntry initialEntry = new TimeEntry();
        initialEntry.setUser(testUser);
        initialEntry.setDate(LocalDate.now());
        initialEntry.addStartTime(LocalTime.of(8,0));
        initialEntry.addEndTime(LocalTime.of(12,0));
        initialEntry.setActualHours("04:00");
        initialEntry.setPlannedHours("08:00");
        initialEntry.setDifference("-04:00");
        timeEntryRepository.save(initialEntry);

        TimeEntryRequest updateRequest = new TimeEntryRequest();
        updateRequest.setDate(LocalDate.now());
        updateRequest.setStartTimes(List.of("09:00"));
        updateRequest.setEndTimes(List.of("17:00"));
        updateRequest.setBreaks(List.of(new TimeEntryRequest.BreakTime("12:00", "12:30")));
        updateRequest.setProjectId(testProject.getId());

        timeEntryService.updateTimeEntry(initialEntry.getId(), updateRequest);

        Optional<TimeEntry> updatedEntry = timeEntryRepository.findById(initialEntry.getId());
        assertTrue(updatedEntry.isPresent());
        assertTrue(updatedEntry.get().getStartTimes().contains(LocalTime.of(9,0)));
        assertEquals("07:30", updatedEntry.get().getActualHours());
        assertEquals(testProject.getId(), updatedEntry.get().getProject().getId());
    }

    @Test
    void deleteTimeEntry_Success() {
        TimeEntry entryToDelete = new TimeEntry();
        entryToDelete.setUser(testUser);
        entryToDelete.setDate(LocalDate.now().minusDays(1));
        entryToDelete.addStartTime(LocalTime.of(8,0));
        entryToDelete.setActualHours("08:00");
        entryToDelete.setPlannedHours("08:00");
        entryToDelete.setDifference("00:00");
        timeEntryRepository.save(entryToDelete);

        timeEntryService.deleteTimeEntry(entryToDelete.getId());

        Optional<TimeEntry> deletedEntry = timeEntryRepository.findById(entryToDelete.getId());
        assertFalse(deletedEntry.isPresent());
    }

    @Test
    void startTimeTracking_Success() {
        Map<String, Object> response = timeEntryService.startTimeTracking(null);

        assertNotNull(response.get("entryId"));
        assertNotNull(response.get("startTime"));
        assertEquals("Zeiterfassung gestartet", response.get("message"));
        assertNull(response.get("projectId"));

        Optional<TimeEntry> activeEntry = timeEntryRepository.findById((Long) response.get("entryId"));
        assertTrue(activeEntry.isPresent());
        assertTrue(activeEntry.get().isActive());
    }

    @Test
    void stopTimeTracking_Success() {
        TimeEntry activeEntry = new TimeEntry();
        activeEntry.setUser(testUser);
        activeEntry.setDate(LocalDate.now());
        activeEntry.addStartTime(LocalTime.now().minusHours(1));
        activeEntry.setActualHours("00:00");
        activeEntry.setPlannedHours("08:00");
        activeEntry.setDifference("-08:00");
        timeEntryRepository.save(activeEntry);

        Map<String, Object> response = timeEntryService.stopTimeTracking(activeEntry.getId());

        assertNotNull(response.get("endTime"));
        assertEquals("Zeiterfassung gestoppt", response.get("message"));

        Optional<TimeEntry> stoppedEntry = timeEntryRepository.findById(activeEntry.getId());
        assertTrue(stoppedEntry.isPresent());
        assertFalse(stoppedEntry.get().isActive());
        assertNotNull(stoppedEntry.get().getActualHours());
    }
}