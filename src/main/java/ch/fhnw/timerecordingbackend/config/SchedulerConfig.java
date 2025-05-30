package ch.fhnw.timerecordingbackend.config;

import ch.fhnw.timerecordingbackend.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

/**
 * Konfiguration für geplante Aufgaben.
 * Führt täglich um 02:00 Uhr automatisch ein Backup über den BackupService aus.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private BackupService backupService;

    // Führt das Backup jeden Tag um 02:00 Uhr nachts aus
    @Scheduled(cron = "0 0 2 * * ?") // Sekunde Minute Stunde TagMonat Monat TagWoche
    public void performDailyBackup() {
        logger.info("Starte geplanten täglichen Backup-Prozess...");
        try {
            // Führt die Backup-Erstellung aus und loggt den Speicherort
            String backupPath = backupService.createBackup();
            logger.info("Geplanter täglicher Backup erfolgreich abgeschlossen. Gespeichert unter: {}", backupPath);
        } catch (IOException e) {
            logger.error("Fehler während des geplanten täglichen Backups: {}", e.getMessage(), e);
        }
    }
}