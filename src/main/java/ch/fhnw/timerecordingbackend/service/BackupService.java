package ch.fhnw.timerecordingbackend.service;

import java.io.IOException;

/**
 * Interface für Backup-Services.
 * Definiert Methode zur Erstellung eines Backups.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public interface BackupService {

    /**
     * Erstellt ein Backup aller relevanten Entitäten (Benutzer, Projekte, Zeiteinträge, Abwesenheiten).
     * Speichert die Daten in einer JSON-Datei im konfigurierten Backup-Verzeichnis.
     * @return Pfad oder Name der erstellten Backup-Datei
     * @throws IOException bei Fehlern während der Datei-Generierung oder -Speicherung
     */
    String createBackup() throws IOException;
}
