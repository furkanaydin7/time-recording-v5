package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.config.WebSecurityConfig;
import ch.fhnw.timerecordingbackend.security.JwtTokenProvider;
import ch.fhnw.timerecordingbackend.security.UserDetailsServiceImpl;
import ch.fhnw.timerecordingbackend.service.BackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author EK
 * Quelle: ChatGPT.com
 */


@WebMvcTest(BackupController.class)
@Import({WebSecurityConfig.class, UserDetailsServiceImpl.class, JwtTokenProvider.class})
@TestPropertySource(properties = {"backup.storage.path=./test-backups"}) // Override für Test
class BackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BackupService backupService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Value("${backup.storage.path}")
    private String backupStoragePath;

    @Test // TC17: System führt regelmässige Backups durch (hier: manuelles Erstellen durch Admin)
    @WithMockUser(authorities = "ADMIN")
    void triggerBackup_whenAdmin_shouldReturnSuccess() throws Exception {
        String mockBackupPath = backupStoragePath + "/timerecording_backup_test.json";
        when(backupService.createBackup()).thenReturn(mockBackupPath);

        mockMvc.perform(post("/api/admin/backups/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Backup erfolgreich erstellt."))
                .andExpect(jsonPath("$.path").value(mockBackupPath));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listBackups_whenAdmin_shouldReturnListOfFiles() throws Exception {
        Path backupDir = Paths.get(backupStoragePath);
        Files.createDirectories(backupDir);
        Path file1 = Files.createFile(backupDir.resolve("timerecording_backup_20230101_100000.json"));


        mockMvc.perform(get("/api/admin/backups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files", hasSize(1)))
                .andExpect(jsonPath("$.files[0]").value("timerecording_backup_20230101_100000.json"));

        Files.deleteIfExists(file1);
        Files.deleteIfExists(backupDir);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void downloadBackup_whenAdminAndFileExists_shouldReturnFile() throws Exception {
        String filename = "timerecording_backup_download_test.json";
        Path backupDir = Paths.get(backupStoragePath);
        Files.createDirectories(backupDir);
        Path filePath = backupDir.resolve(filename);
        Files.writeString(filePath, "{\"data\":\"test\"}");

        mockMvc.perform(get("/api/admin/backups/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andExpect(jsonPath("$.data").value("test"));

        Files.deleteIfExists(filePath);
        Files.deleteIfExists(backupDir);
    }

    @Test
    @WithMockUser(authorities = "USER") // Nicht-Admin Benutzer
    void triggerBackup_whenNotAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/backups/create"))
                .andExpect(status().isForbidden());
    }
}

