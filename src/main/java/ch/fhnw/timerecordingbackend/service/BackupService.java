package ch.fhnw.timerecordingbackend.service;

import java.io.IOException;

/**
 * Interface für Backup-Services.
 * Definiert Methode zur Erstellung eines Backups.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public interface BackupService {
    String createBackup() throws IOException;
}
