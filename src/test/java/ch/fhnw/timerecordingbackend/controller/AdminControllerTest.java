package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.config.WebSecurityConfig;
import ch.fhnw.timerecordingbackend.dto.admin.UserRegistrationRequest;
import ch.fhnw.timerecordingbackend.dto.admin.UserResponse;
import ch.fhnw.timerecordingbackend.model.Registration;
import ch.fhnw.timerecordingbackend.model.Role;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.model.enums.UserStatus;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author EK
 * Quelle: ChatGPT.com
 */

@ActiveProfiles("test")
@WebMvcTest(AdminController.class)
@Import({WebSecurityConfig.class, UserDetailsServiceImpl.class, JwtTokenProvider.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SystemLogRepository systemLogRepository;

    @MockBean
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Role adminRole;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role("ADMIN");
        adminRole.setId(1L);
        employeeRole = new Role("EMPLOYEE");
        employeeRole.setId(2L);

        user1 = new User("Admin", "User", "admin@example.com", "password");
        user1.setId(1L);
        user1.setRoles(Set.of(adminRole));
        user1.setCreatedAt(LocalDateTime.now().minusDays(1));
        user1.setUpdatedAt(LocalDateTime.now().minusHours(5));
        user1.setStatus(UserStatus.ACTIVE);
        user1.setActive(true);
        user1.setPlannedHoursPerDay(8.0);


        user2 = new User("Test", "Employee", "employee@example.com", "password");
        user2.setId(2L);
        user2.setRoles(Set.of(employeeRole));
        user2.setCreatedAt(LocalDateTime.now().minusDays(2));
        user2.setUpdatedAt(LocalDateTime.now().minusHours(10));
        user2.setStatus(UserStatus.ACTIVE);
        user2.setActive(true);
        user2.setPlannedHoursPerDay(8.0);
    }
    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_whenAdminAndValidRequest_shouldReturnCreatedUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("New");
        request.setLastName("UserAdmin");
        request.setEmail("new.user.admin@example.com");
        request.setRole("EMPLOYEE");
        request.setPlannedHoursPerDay(8.0);
        request.setPassword("CustomPass123");

        when(passwordEncoder.encode("CustomPass123")).thenReturn("gehashtesCustomPass123");

        User createdUserModel = new User();
        createdUserModel.setId(1L);
        createdUserModel.setFirstName("New");
        createdUserModel.setLastName("UserAdmin");
        createdUserModel.setEmail("new.user.admin@example.com");
        createdUserModel.setActive(true);
        createdUserModel.setStatus(UserStatus.ACTIVE);
        createdUserModel.setRoles(Collections.singleton(employeeRole));
        createdUserModel.setCreatedAt(LocalDateTime.now());
        createdUserModel.setUpdatedAt(LocalDateTime.now());
        createdUserModel.setPlannedHoursPerDay(8.0);


        when(userService.createUser(any(User.class), eq("EMPLOYEE"), nullable(Long.class)))
                .thenReturn(createdUserModel);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("UserAdmin"))
                .andExpect(jsonPath("$.email").value("new.user.admin@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("EMPLOYEE"))
                .andExpect(jsonPath("$.temporaryPassword").value("CustomPass123"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_whenAdminAndValidRequestAndNoPassword_shouldUseLastNameAsTemporaryPassword() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("New");
        request.setLastName("NoPassUser");
        request.setEmail("new.nopassuser.admin@example.com");
        request.setRole("MANAGER");
        request.setPlannedHoursPerDay(8.0);

        when(passwordEncoder.encode("nopassuser")).thenReturn("gehashtesNoPassUser");

        User createdUserModel = new User();
        createdUserModel.setId(2L);
        createdUserModel.setFirstName("New");
        createdUserModel.setLastName("NoPassUser");
        createdUserModel.setEmail("new.nopassuser.admin@example.com");
        createdUserModel.setActive(true);
        createdUserModel.setStatus(UserStatus.ACTIVE);
        Role managerRole = new Role("MANAGER");
        managerRole.setId(3L); // Annahme andere ID
        createdUserModel.setRoles(Collections.singleton(managerRole));
        createdUserModel.setCreatedAt(LocalDateTime.now());
        createdUserModel.setUpdatedAt(LocalDateTime.now());
        createdUserModel.setPlannedHoursPerDay(8.0);


        when(userService.createUser(any(User.class), eq("MANAGER"), nullable(Long.class)))
                .thenReturn(createdUserModel);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.temporaryPassword").value("nopassuser"));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_whenEmailExists_shouldReturnBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Existing");
        request.setLastName("User");
        request.setEmail("existing.email@example.com");
        request.setRole("EMPLOYEE");
        request.setPassword("password123");

        when(passwordEncoder.encode(anyString())).thenReturn("gehashtesPassword");

        when(userService.createUser(any(User.class), anyString(), nullable(Long.class)))
                .thenThrow(new jakarta.validation.ValidationException("Email existiert bereits"));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email existiert bereits"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateUser_whenAdminAndValidRequest_shouldReturnUpdatedUser() throws Exception {
        Long userIdToUpdate = 1L;
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("UpdatedFirstName");
        request.setLastName("UpdatedLastName");
        request.setEmail("original.email@example.com");
        request.setPlannedHoursPerDay(7.5);
        request.setRole("EMPLOYEE"); // Muss gesetzt sein wegen @Valid

        User updatedUserModel = new User();
        updatedUserModel.setId(userIdToUpdate);
        updatedUserModel.setFirstName("UpdatedFirstName");
        updatedUserModel.setLastName("UpdatedLastName");
        updatedUserModel.setEmail("original.email@example.com");
        updatedUserModel.setPlannedHoursPerDay(7.5);
        updatedUserModel.setActive(true);
        updatedUserModel.setStatus(UserStatus.ACTIVE);
        updatedUserModel.setRoles(Collections.singleton(employeeRole));
        updatedUserModel.setUpdatedAt(LocalDateTime.now());
        updatedUserModel.setCreatedAt(user1.getCreatedAt());


        when(userService.updateUser(eq(userIdToUpdate), any(User.class)))
                .thenAnswer(invocation -> {
                    User userFromControllerArg = invocation.getArgument(1);
                    updatedUserModel.setFirstName(userFromControllerArg.getFirstName());
                    updatedUserModel.setLastName(userFromControllerArg.getLastName());
                    updatedUserModel.setPlannedHoursPerDay(userFromControllerArg.getPlannedHoursPerDay());
                    updatedUserModel.setActive(userFromControllerArg.isActive()); // Kommt vom neu erstellten User im Controller
                    updatedUserModel.setStatus(userFromControllerArg.isActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE);
                    return updatedUserModel;
                });

        mockMvc.perform(put("/api/admin/users/{id}", userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdToUpdate))
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("UpdatedLastName"))
                .andExpect(jsonPath("$.email").value("original.email@example.com"))
                .andExpect(jsonPath("$.plannedHoursPerDay").value(7.5));

        verify(userService).updateUser(eq(userIdToUpdate), any(User.class));
    }

    // === Tests für Benutzerdeaktivierung (bereits vorhanden und geprüft) ===
    @Test
    @WithMockUser(authorities = "ADMIN")
    void deactivateUser_whenAdmin_shouldDeactivateUser() throws Exception {
        Long userIdToDeactivate = 1L;

        User deactivatedUserModel = new User();
        deactivatedUserModel.setId(userIdToDeactivate);
        deactivatedUserModel.setFirstName("Test");
        deactivatedUserModel.setLastName("User");
        deactivatedUserModel.setEmail("test.user@example.com");
        deactivatedUserModel.setActive(false);
        deactivatedUserModel.setStatus(UserStatus.INACTIVE);
        deactivatedUserModel.setRoles(Collections.singleton(employeeRole));
        deactivatedUserModel.setUpdatedAt(LocalDateTime.now());
        deactivatedUserModel.setCreatedAt(user1.getCreatedAt());
        deactivatedUserModel.setPlannedHoursPerDay(user1.getPlannedHoursPerDay());


        when(userService.deactivateUser(userIdToDeactivate)).thenReturn(deactivatedUserModel);

        mockMvc.perform(patch("/api/admin/user/{id}/deactivate", userIdToDeactivate)) // Pfad /user/ beibehalten wie im Controller
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdToDeactivate))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(userService).deactivateUser(userIdToDeactivate);
    }

    // === Tests für System-Logs (bereits vorhanden und geprüft) ===
    @Test
    @WithMockUser(authorities = "ADMIN")
    void getSystemLogs_whenAdmin_shouldReturnLogs() throws Exception {
        SystemLog log1 = new SystemLog(); /* ... Daten setzen ... */
        log1.setId(1L);
        log1.setAction("User Login");
        SystemLog log2 = new SystemLog(); /* ... Daten setzen ... */
        log2.setId(2L);
        log2.setAction("Time Entry Created");
        List<SystemLog> mockLogs = List.of(log2, log1); // Annahme: sortiert

        when(systemLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")))
                .thenReturn(mockLogs);

        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs.length()").value(2))
                .andExpect(jsonPath("$.logs[0].action").value("Time Entry Created"))
                .andExpect(jsonPath("$.logs[1].action").value("User Login"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllUsers_whenAdmin_shouldReturnListOfUsers() throws Exception {
        List<User> users = List.of(user1, user2);
        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()))
                .andExpect(jsonPath("$[1].email").value(user2.getEmail()));

        verify(userService).findAllUsers();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        when(userService.findById(user1.getId())).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/api/admin/users/{id}", user1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1.getId()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()));

        verify(userService).findById(user1.getId());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getUserById_whenUserNotFound_shouldReturnNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).findById(99L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllRoles_whenAdmin_shouldReturnRolesList() throws Exception {
        List<Role> roles = List.of(adminRole, employeeRole);
        when(userService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value(adminRole.getName()))
                .andExpect(jsonPath("$[1].name").value(employeeRole.getName()));

        verify(userService).getAllRoles();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPendingRegistrationRequests_whenAdmin_shouldReturnPendingRequests() throws Exception {
        Registration reg1 = new Registration("Pending", "User1", "pending1@example.com", "EMPLOYEE", null);
        reg1.setId(1L);
        reg1.setCreatedAt(LocalDateTime.now());
        Registration reg2 = new Registration("Pending", "User2", "pending2@example.com", "MANAGER", user1);
        reg2.setId(2L);
        reg2.setCreatedAt(LocalDateTime.now());


        List<Registration> pendingRequests = List.of(reg1, reg2);
        when(registrationService.getAllPendingRequests()).thenReturn(pendingRequests);

        mockMvc.perform(get("/api/admin/registration-requests/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("pending1@example.com"))
                .andExpect(jsonPath("$[0].requestedRole").value("EMPLOYEE"))
                .andExpect(jsonPath("$[0].managerName").value("N/A"))
                .andExpect(jsonPath("$[1].email").value("pending2@example.com"))
                .andExpect(jsonPath("$[1].requestedRole").value("MANAGER"))
                .andExpect(jsonPath("$[1].managerName").value(user1.getFullName()));


        verify(registrationService).getAllPendingRequests();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ADMIN")
    void approveRegistrationRequest_whenAdmin_shouldApprove() throws Exception {
        Long requestId = 1L;

        doNothing().when(registrationService).approveRegistrationRequest(eq(requestId), eq("admin@example.com"));

        mockMvc.perform(patch("/api/admin/registration-requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registrierungsanfrage genehmigt und Benutzer erstellt."));

        verify(registrationService).approveRegistrationRequest(eq(requestId), eq("admin@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ADMIN")
    void rejectRegistrationRequest_whenAdmin_shouldReject() throws Exception {
        Long requestId = 1L;
        doNothing().when(registrationService).rejectRegistrationRequest(eq(requestId), eq("admin@example.com"));

        mockMvc.perform(patch("/api/admin/registration-requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registrierungsanfrage wurde abgelehnt und aus der Datenbank entfernt. Der Nutzer kann sich erneut registrieren."));

        verify(registrationService).rejectRegistrationRequest(eq(requestId), eq("admin@example.com"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void rejectRegistrationRequest_whenServiceThrowsValidationException_shouldReturnBadRequest() throws Exception {
        Long requestId = 2L;
        String adminEmail = "user";
        doThrow(new ValidationException("Anfrage ist nicht ausstehend"))
                .when(registrationService).rejectRegistrationRequest(eq(requestId), eq(adminEmail));

        mockMvc.perform(patch("/api/admin/registration-requests/{id}/reject", requestId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Anfrage ist nicht ausstehend"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void activateUser_whenAdmin_shouldActivateUser() throws Exception {
        Long userIdToActivate = user2.getId();
        user2.setActive(true); // simulieren, dass es nachher aktiv ist
        user2.setStatus(UserStatus.ACTIVE);
        user2.setUpdatedAt(LocalDateTime.now());

        when(userService.activateUser(userIdToActivate)).thenReturn(user2);

        mockMvc.perform(patch("/api/admin/users/{id}/activate", userIdToActivate) // Korrekter Pfad users
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdToActivate))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).activateUser(userIdToActivate);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void changeUserStatus_whenAdmin_shouldChangeStatus() throws Exception {
        Long userIdToChangeStatus = user1.getId();
        UserStatus newStatus = UserStatus.INACTIVE;
        user1.setStatus(newStatus);
        user1.setActive(false);
        user1.setUpdatedAt(LocalDateTime.now());

        when(userService.updateUserStatus(userIdToChangeStatus, newStatus)).thenReturn(user1);

        mockMvc.perform(patch("/api/admin/users/{id}/status", userIdToChangeStatus)
                        .param("status", "INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdToChangeStatus))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.active").value(false));

        verify(userService).updateUserStatus(userIdToChangeStatus, newStatus);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void addRoleToUser_whenAdmin_shouldAddRole() throws Exception {
        Long targetUserId = user2.getId(); // User ohne Admin-Rolle
        String roleNameToAdd = "ADMIN";
        Role newAdminRole = new Role(roleNameToAdd);
        newAdminRole.setId(adminRole.getId());

        User userWithAddedRole = new User(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getPassword());
        userWithAddedRole.setId(user2.getId());
        userWithAddedRole.setRoles(Set.of(employeeRole, newAdminRole)); // Beide Rollen
        userWithAddedRole.setPlannedHoursPerDay(user2.getPlannedHoursPerDay());
        userWithAddedRole.setActive(user2.isActive());
        userWithAddedRole.setStatus(user2.getStatus());
        userWithAddedRole.setCreatedAt(user2.getCreatedAt());
        userWithAddedRole.setUpdatedAt(LocalDateTime.now());


        when(userService.addRoleToUser(targetUserId, roleNameToAdd)).thenReturn(userWithAddedRole);

        mockMvc.perform(post("/api/admin/users/{id}/roles", targetUserId)
                        .param("roleName", roleNameToAdd)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUserId))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles[?(@ == 'EMPLOYEE')]").exists())
                .andExpect(jsonPath("$.roles[?(@ == 'ADMIN')]").exists());


        verify(userService).addRoleToUser(targetUserId, roleNameToAdd);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void removeRoleFromUser_whenAdmin_shouldRemoveRole() throws Exception {
        Long targetUserId = user1.getId(); // User mit Admin-Rolle
        String roleNameToRemove = "ADMIN";

        User userWithRoleRemoved = new User(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getPassword());
        userWithRoleRemoved.setId(user1.getId());
        userWithRoleRemoved.setRoles(Collections.emptySet());
        userWithRoleRemoved.setPlannedHoursPerDay(user1.getPlannedHoursPerDay());
        userWithRoleRemoved.setActive(user1.isActive());
        userWithRoleRemoved.setStatus(user1.getStatus());
        userWithRoleRemoved.setCreatedAt(user1.getCreatedAt());
        userWithRoleRemoved.setUpdatedAt(LocalDateTime.now());


        when(userService.removeRoleFromUser(targetUserId, roleNameToRemove)).thenReturn(userWithRoleRemoved);

        mockMvc.perform(delete("/api/admin/users/{id}/roles", targetUserId)
                        .param("roleName", roleNameToRemove)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUserId))
                .andExpect(jsonPath("$.roles", hasSize(0)));

        verify(userService).removeRoleFromUser(targetUserId, roleNameToRemove);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void searchUsers_whenAdmin_shouldReturnMatchingUsers() throws Exception {
        String searchTerm = "Admin";
        List<User> searchResults = List.of(user1); // user1 hat "Admin" im Vornamen

        when(userService.searchUsers(searchTerm)).thenReturn(searchResults);

        mockMvc.perform(get("/api/admin/users/search")
                        .param("searchTerm", searchTerm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Admin"));

        verify(userService).searchUsers(searchTerm);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void resetUserPassword_whenAdmin_shouldReturnTemporaryPassword() throws Exception {
        Long targetUserId = user2.getId();
        String tempPassword = "newTempPassword123";

        when(userService.resetPasswordToTemporary(targetUserId)).thenReturn(tempPassword);

        mockMvc.perform(post("/api/admin/users/{id}/reset-password", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Passwort zurückgesetzt"))
                .andExpect(jsonPath("$.temporaryPassword").value(tempPassword))
                .andExpect(jsonPath("$.userId").value(targetUserId));

        verify(userService).resetPasswordToTemporary(targetUserId);
    }
}

