package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.config.WebSecurityConfig;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.security.JwtTokenProvider;
import ch.fhnw.timerecordingbackend.security.UserDetailsServiceImpl;
import ch.fhnw.timerecordingbackend.service.TimeEntryServiceImpl; // Nur diese für Mocking
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author EK
 * Quelle: ChatGPT.com
 */

@ActiveProfiles("test")
@WebMvcTest(TimeEntryController.class)
@Import({WebSecurityConfig.class, UserDetailsServiceImpl.class, JwtTokenProvider.class})
class TimeEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Explizit benannter Mock für TimeEntryServiceImpl, der von SpEL verwendet wird.
    // Dieser Mock wird auch für die Injektion des TimeEntryService-Interfaces im Controller verwendet.
    @MockBean(name = "timeEntryServiceImpl")
    private TimeEntryServiceImpl timeEntryServiceImplMock;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private TimeEntryRequest timeEntryRequestDTO;
    private TimeEntryResponse timeEntryResponseDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "User", "test@example.com", "password");
        testUser.setId(1L);

        timeEntryRequestDTO = new TimeEntryRequest();
        timeEntryRequestDTO.setDate(LocalDate.now());
        timeEntryRequestDTO.setStartTimes(List.of("08:00"));
        timeEntryRequestDTO.setEndTimes(List.of("17:00"));
        timeEntryRequestDTO.setBreaks(List.of(new TimeEntryRequest.BreakTime("12:00", "13:00")));
        timeEntryRequestDTO.setProjectId(1L);

        timeEntryResponseDTO = new TimeEntryResponse();
        timeEntryResponseDTO.setId(1L);
        timeEntryResponseDTO.setDate(LocalDate.now());
        timeEntryResponseDTO.setActualHours("08:00");
        timeEntryResponseDTO.setUserId(testUser.getId());
        timeEntryResponseDTO.setUser(testUser.getFullName());
    }

    @Test // TC09
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"}) // explizite Rolle
    void createTimeEntry_whenValidRequest_shouldReturnCreatedTimeEntry() throws Exception {
        when(timeEntryServiceImplMock.createTimeEntry(any(TimeEntryRequest.class))).thenReturn(timeEntryResponseDTO);

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeEntryRequestDTO)))
                .andExpect(status().isOk()) // Ihr Controller gibt ResponseEntity.ok() zurück
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.actualHours").value("08:00"));
    }

    @Test // TC10
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"})
    void updateTimeEntry_whenOwner_shouldReturnOk() throws Exception {
        Long entryId = 1L;
        doNothing().when(timeEntryServiceImplMock).updateTimeEntry(eq(entryId), any(TimeEntryRequest.class));
        when(timeEntryServiceImplMock.isOwnerOfTimeEntry(entryId)).thenReturn(true);

        mockMvc.perform(put("/api/time-entries/{id}", entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeEntryRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eintrag aktualisiert"));
    }

    @Test // TC11
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"})
    void deleteTimeEntry_whenOwner_shouldReturnOk() throws Exception {
        Long entryId = 1L;
        when(timeEntryServiceImplMock.isOwnerOfTimeEntry(entryId)).thenReturn(true);
        doNothing().when(timeEntryServiceImplMock).deleteTimeEntry(entryId);

        mockMvc.perform(delete("/api/time-entries/{id}", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eintrag gelöscht"));
    }

    @Test // TC11 - Admin darf auch löschen
    @WithMockUser(authorities = "ADMIN")
    void deleteTimeEntry_whenAdmin_shouldReturnOk() throws Exception {
        Long entryId = 1L;
        doNothing().when(timeEntryServiceImplMock).deleteTimeEntry(entryId);

        mockMvc.perform(delete("/api/time-entries/{id}", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eintrag gelöscht"));
    }


    @Test // TC19
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"})
    void getCurrentUserTimeEntries_shouldReturnOwnEntries() throws Exception {
        when(timeEntryServiceImplMock.getCurrentUserTimeEntries()).thenReturn(List.of(timeEntryResponseDTO));

        mockMvc.perform(get("/api/time-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].id").value(1L));
    }

    @Test // TC21
    @WithMockUser(authorities = "ADMIN")
    void getAllTimeEntries_whenAdmin_shouldReturnAllEntries() throws Exception {
        TimeEntryResponse anotherEntry = new TimeEntryResponse();
        anotherEntry.setId(2L);
        anotherEntry.setUserId(2L);
        when(timeEntryServiceImplMock.getAllTimeEntries()).thenReturn(List.of(timeEntryResponseDTO, anotherEntry));

        mockMvc.perform(get("/api/time-entries/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"})
    void startTimeTracking_shouldReturnOk() throws Exception {
        Map<String, Object> serviceResponse = Map.of("entryId", 1L, "startTime", LocalDateTime.now().toString(), "message", "Zeiterfassung gestartet");
        when(timeEntryServiceImplMock.startTimeTracking(null)).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zeiterfassung gestartet"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"EMPLOYEE"})
    void stopTimeTracking_shouldReturnOk() throws Exception {
        Long entryId = 1L;
        Map<String, Object> serviceResponse = Map.of("message", "Zeiterfassung gestoppt", "endTime", LocalDateTime.now().toString());
        when(timeEntryServiceImplMock.stopTimeTracking(entryId)).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/time-entries/{entryId}/stop", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zeiterfassung gestoppt"));
    }
}


