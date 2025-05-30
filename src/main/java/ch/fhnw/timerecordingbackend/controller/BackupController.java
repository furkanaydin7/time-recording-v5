package ch.fhnw.timerecordingbackend.controller;

import ch.fhnw.timerecordingbackend.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST-Controller für Backup-Verwaltung.
 * Ermöglicht das Erstellen, Auflisten und Herunterladen von Backup-Dateien.
 * Nur für Benutzer mit der Rolle ADMIN zugänglich.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
@RestController
@RequestMapping("/api/admin/backups")
@PreAuthorize("hasAuthority('ADMIN')")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    @Autowired
    private BackupService backupService;

    @Value("${backup.storage.path}")
    private String backupStoragePath;

    @PostMapping("/create")
    public ResponseEntity<?> triggerBackup() {
        try {
            String backupPath = backupService.createBackup();
            return ResponseEntity.ok(Map.of(
                    "message", "Backup erfolgreich erstellt.",
                    "path", backupPath
            ));
        } catch (IOException e) {
            logger.error("Fehler beim manuellen Erstellen des Backups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Backup konnte nicht erstellt werden: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listBackups() {
        Path backupDir = Paths.get(backupStoragePath);
        if (!Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            return ResponseEntity.ok(Map.of("message", "Backup-Verzeichnis existiert nicht.", "files", List.of()));
        }
        try (Stream<Path> paths = Files.list(backupDir)) {
            List<String> filenames = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("timerecording_backup_") && name.endsWith(".json"))
                    .sorted()
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("files", filenames));
        } catch (IOException e) {
            logger.error("Fehler beim Auflisten der Backup-Dateien: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Backup-Dateien konnten nicht geladen werden: " + e.getMessage()));
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        Path backupDir = Paths.get(backupStoragePath);
        Path filePath = backupDir.resolve(filename);

        if (!filename.startsWith("timerecording_backup_") || !filename.endsWith(".json")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Vermeide potenziellen Path Traversal
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                logger.warn("Backup-Datei nicht gefunden oder nicht lesbar: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("Fehler beim Erstellen der URL für die Backup-Datei: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
