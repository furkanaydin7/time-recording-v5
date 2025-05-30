package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.config.WebSecurityConfig;
import ch.fhnw.timerecordingbackend.dto.registration.RegistrationRequest;
import ch.fhnw.timerecordingbackend.model.Registration;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.security.JwtTokenProvider;
import ch.fhnw.timerecordingbackend.security.UserDetailsServiceImpl;
import ch.fhnw.timerecordingbackend.service.RegistrationService;
import ch.fhnw.timerecordingbackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ch.fhnw.timerecordingbackend.model.Role;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author EK
 * Quelle: ChatGPT.com
 */

@ActiveProfiles("test")
@WebMvcTest(PublicRegistrationController.class)
@Import({WebSecurityConfig.class, UserDetailsServiceImpl.class, JwtTokenProvider.class})
class PublicRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    private RegistrationRequest registrationRequest;
    private Registration createdRegistration;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setFirstName("Test");
        registrationRequest.setLastName("User");
        registrationRequest.setEmail("test.user@example.com");
        registrationRequest.setRole("EMPLOYEE");
        registrationRequest.setManagerId(null);

        createdRegistration = new Registration();
        createdRegistration.setId(1L);
        createdRegistration.setFirstName("Test");
        createdRegistration.setLastName("User");
        createdRegistration.setEmail("test.user@example.com");
        createdRegistration.setRequestedRole("EMPLOYEE");
        createdRegistration.setStatus("PENDING");
        createdRegistration.setCreatedAt(LocalDateTime.now());
    }

    @Test // TC01: Benutzerregistrierung
    void submitRegistrationRequest_whenValidRequest_shouldReturnAccepted() throws Exception {
        when(registrationService.submitRegistrationRequest(any(RegistrationRequest.class))).thenReturn(createdRegistration);

        mockMvc.perform(post("/api/public/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Registrierungsanfrage erfolgreich eingereicht. Sie wird vom Administrator geprüft."));
    }

    @Test // TC01: Benutzerregistrierung - Fehlerfall (z.B. E-Mail existiert)
    void submitRegistrationRequest_whenEmailExists_shouldReturnBadRequest() throws Exception {
        when(registrationService.submitRegistrationRequest(any(RegistrationRequest.class)))
                .thenThrow(new ValidationException("Eine Registrierungsanfrage mit dieser E-Mail-Adresse existiert bereits oder wurde bereits verarbeitet."));

        mockMvc.perform(post("/api/public/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Eine Registrierungsanfrage mit dieser E-Mail-Adresse existiert bereits oder wurde bereits verarbeitet."));
    }

    @Test
    void getPublicManagers_shouldReturnListOfManagers() throws Exception {
        User manager = new User("Manager", "Test", "manager@example.com", "password");
        manager.setId(1L);
        manager.getRoles().add(new Role("MANAGER"));

        when(userService.findAllUsers()).thenReturn(List.of(manager));

        mockMvc.perform(get("/api/public/managers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("manager@example.com")))
                .andExpect(jsonPath("$[0].roles[0]", is("MANAGER")));
    }
}



