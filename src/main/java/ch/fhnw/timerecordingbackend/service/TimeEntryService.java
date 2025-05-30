package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;

import java.util.List;
import java.util.Map;

/**
 * Service-Interface für Zeiteintragsverwaltung.
 * Definiert Operationen zum Erstellen, Aktualisieren, Löschen und Abrufen von Zeiteinträgen sowie
 * zum Starten und Stoppen der Zeiterfassung und Zuordnen von Projekten.
 * Implementierungen übernehmen die Geschäftslogik und Kommunikation mit dem Repository.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public interface TimeEntryService {

    /**
     * Erstellt einen neuen Zeiteintrag basierend auf den übergebenen Daten.
     * @param timeEntryRequest DTO mit Datum, Zeiten, Pausen und optionaler Projekt-ID
     * @return DTO mit den gespeicherten Zeiteintragsdaten
     */
    TimeEntryResponse createTimeEntry(TimeEntryRequest timeEntryRequest);

    /**
     * Aktualisiert einen bestehenden Zeiteintrag.
     * @param id ID des zu aktualisierenden Zeiteintrags
     * @param timeEntryRequest DTO mit aktualisierten Feldern
     */
    void updateTimeEntry(Long id, TimeEntryRequest timeEntryRequest);

    /**
     * Löscht einen Zeiteintrag.
     * @param id ID des zu löschenden Zeiteintrags
     */
    void deleteTimeEntry(Long id);

    /**
     * Liest alle Zeiteinträge des aktuell angemeldeten Nutzers aus.
     * @return Liste der TimeEntryResponse-DTOs
     */
    List<TimeEntryResponse> getCurrentUserTimeEntries();

    /**
     * Liest alle Zeiteinträge eines bestimmten Nutzers.
     * @param userId ID des Nutzers
     * @return Liste der TimeEntryResponse-DTOs
     */
    List<TimeEntryResponse> getUserTimeEntries(Long userId);

    /**
     * Liest alle Zeiteinträge der Teammitglieder eines Managers.
     * @return Liste der TimeEntryResponse-DTOs
     */
    List<TimeEntryResponse> getTeamTimeEntries();

    /**
     * Liest alle Zeiteinträge im System (nur für Admins).
     * @return Liste der TimeEntryResponse-DTOs
     */
    List<TimeEntryResponse> getAllTimeEntries();

    /**
     * Startet eine Zeiterfassung für ein (optional angegebenes) Projekt.
     * @param projectId optional ID des Projekts
     * @return Map mit Informationen zum gestarteten Zeiteintrag (z.B. ID, Startzeit)
     */
    Map<String, Object> startTimeTracking(Long projectId);

    /**
     * Stoppt eine laufende Zeiterfassung.
     * @param entryId ID des zu stoppenden Zeiteintrags
     * @return Map mit Informationen zum gestoppten Zeiteintrag (z.B. Endzeit, Dauer)
     */
    Map<String, Object> stopTimeTracking(Long entryId);

    /**
     * Weist einem bestehenden Zeiteintrag ein Projekt zu.
     * @param timeEntryId ID des Zeiteintrags
     * @param projectId ID des Projekts
     */
    void assignProject(Long timeEntryId, Long projectId);
}
