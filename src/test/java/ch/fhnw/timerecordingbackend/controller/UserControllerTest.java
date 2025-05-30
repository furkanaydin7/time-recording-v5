package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.config.WebSecurityConfig;
import ch.fhnw.timerecordingbackend.dto.authentication.ChangePasswordRequest;
import ch.fhnw.timerecordingbackend.dto.authentication.ResetPasswordRequest;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.security.JwtTokenProvider;
import ch.fhnw.timerecordingbackend.security.UserDetailsServiceImpl;
import ch.fhnw.timerecordingbackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import ch.fhnw.timerecordingbackend.model.SystemLog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author EK
 * Quelle: ChatGPT.com
 */

@WebMvcTest(UserController.class)
@Import({WebSecurityConfig.class, UserDetailsServiceImpl.class, JwtTokenProvider.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SystemLogRepository systemLogRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ChangePasswordRequest changePasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail("user@example.com");

        testUser = new User("Test", "User", "user@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test // TC07: Mitarbeiter ändert Passwort
    @WithMockUser(username = "user@example.com")
    void changePassword_whenAuthenticatedAndValidRequest_shouldReturnOk() throws Exception {
        doNothing().when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Passwort geändert"));
    }
    @Test
    void requestPasswordReset_whenEmailExists_shouldReturnOkAndLog() throws Exception {
        when(userService.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.of(testUser));

        when(systemLogRepository.save(any(SystemLog.class))).thenAnswer(invocation -> {
            SystemLog logToSave = invocation.getArgument(0);
            if (logToSave.getId() == null) {
                logToSave.setId(1L);
            }
            return logToSave;
        });

        mockMvc.perform(post("/api/users/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ihre Anfrage zum Zurücksetzen des Passworts wurde an den Administrator weitergeleitet."));
    }

    @Test // TC08: Mitarbeiter setzt Passwort zurück - E-Mail nicht vorhanden
    void requestPasswordReset_whenEmailDoesNotExist_shouldStillReturnOk() throws Exception {
        when(userService.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/users/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wenn ein Konto mit dieser E-Mail-Adresse existiert, wurde Ihre Anfrage verarbeitet."));
    }
}
