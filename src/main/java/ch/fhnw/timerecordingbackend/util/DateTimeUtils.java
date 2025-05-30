package ch.fhnw.timerecordingbackend.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils Klasse für Datum und Zeit Operationen
 * Hilfsmethoden für häufig verwendete Datum/Zeit Berechnungen
 * @author PD
 * Quelle: ChatGPT.com
 */
public class DateTimeUtils {

    // Standard-Formatierungen
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Private Constructor - Utility-Klasse soll nicht instanziiert werden
     */
    private DateTimeUtils() {
        throw new IllegalStateException("Utility-Klasse kann nicht instanziiert werden");
    }

    /**
     * Formatiert LocalDate zu String im Format "yyyy-MM-dd"
     * @param date Das zu formatierende Datum
     * @return Formatiertes Datum als String oder null wenn date null ist
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    /**
     * Formatiert LocalDate zu String im Anzeigeformat "dd.MM.yyyy"
     * @param date Das zu formatierende Datum
     * @return Formatiertes Datum als String oder null wenn date null ist
     */
    public static String formatDateDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMAT) : null;
    }

    /**
     * Formatiert LocalTime zu String im Format "HH:mm"
     * @param time Die zu formatierende Zeit
     * @return Formatierte Zeit als String oder null wenn time null ist
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : null;
    }

    /**
     * Formatiert LocalDateTime zu String im Format "yyyy-MM-dd HH:mm:ss"
     * @param dateTime Das zu formatierende Datum/Zeit
     * @return Formatiertes Datum/Zeit als String oder null wenn dateTime null ist
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : null;
    }

    /**
     * Formatiert LocalDateTime zu String im Anzeigeformat "dd.MM.yyyy HH:mm"
     * @param dateTime Das zu formatierende Datum/Zeit
     * @return Formatiertes Datum/Zeit als String oder null wenn dateTime null ist
     */
    public static String formatDateTimeDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMAT) : null;
    }

    /**
     * Parst String zu LocalDate
     * @param dateString Datum als String im Format "yyyy-MM-dd"
     * @return LocalDate oder null bei ungültigem Format
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parst String zu LocalTime
     * @param timeString Zeit als String im Format "HH:mm"
     * @return LocalTime oder null bei ungültigem Format
     */
    public static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeString, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parst String zu LocalDateTime
     * @param dateTimeString Datum/Zeit als String im Format "yyyy-MM-dd HH:mm:ss"
     * @return LocalDateTime oder null bei ungültigem Format
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Konvertiert Stunden (Double) zu "HH:mm" Format
     * @param hours Stunden als Double (z.B. 8.5 für 8:30)
     * @return Zeit im Format "HH:mm"
     */
    public static String formatHours(Double hours) {
        if (hours == null) {
            return "00:00";
        }

        int totalMinutes = (int) Math.round(hours * 60);
        int h = totalMinutes / 60;
        int m = Math.abs(totalMinutes % 60);

        // Negative Stunden mit Vorzeichen darstellen
        if (totalMinutes < 0 && h == 0) {
            return String.format("-%02d:%02d", Math.abs(h), m);
        }

        return String.format("%02d:%02d", h, m);
    }

    /**
     * Konvertiert "HH:mm" Format zu Stunden (Double)
     * @param timeString Zeit im Format "HH:mm" oder "-HH:mm"
     * @return Stunden als Double
     */
    public static Double parseHours(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return 0.0;
        }

        try {
            boolean negative = timeString.startsWith("-");
            String cleanTime = timeString.replace("-", "");
            String[] parts = cleanTime.split(":");

            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                double result = hours + (minutes / 60.0);
                return negative ? -result : result;
            }

            return Double.parseDouble(timeString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Berechnet die Differenz zwischen zwei Zeitpunkten in Stunden
     * @param startTime Startzeit
     * @param endTime Endzeit
     * @return Differenz in Stunden als Double
     */
    public static Double calculateHoursBetween(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return 0.0;
        }

        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes() / 60.0;
    }

    /**
     * Addiert zwei Zeitangaben im "HH:mm" Format
     * @param time1 Erste Zeitangabe
     * @param time2 Zweite Zeitangabe
     * @return Summe im "HH:mm" Format
     */
    public static String addHours(String time1, String time2) {
        Double hours1 = parseHours(time1);
        Double hours2 = parseHours(time2);
        return formatHours(hours1 + hours2);
    }

    /**
     * Subtrahiert zwei Zeitangaben im "HH:mm" Format
     * @param time1 Erste Zeitangabe (Minuend)
     * @param time2 Zweite Zeitangabe (Subtrahend)
     * @return Differenz im "HH:mm" Format
     */
    public static String subtractHours(String time1, String time2) {
        Double hours1 = parseHours(time1);
        Double hours2 = parseHours(time2);
        return formatHours(hours1 - hours2);
    }

    /**
     * Prüft ob ein Datum ein Werktag ist (Montag bis Freitag)
     * @param date Das zu prüfende Datum
     * @return true wenn Werktag, false wenn Wochenende
     */
    public static boolean isWorkday(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Berechnet alle Werktage zwischen zwei Daten (inklusive)
     * @param startDate Startdatum
     * @param endDate Enddatum
     * @return Anzahl der Werktage
     */
    public static long countWorkdays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(DateTimeUtils::isWorkday)
                .count();
    }

    /**
     * Gibt alle Werktage zwischen zwei Daten zurück
     * @param startDate Startdatum
     * @param endDate Enddatum
     * @return Liste aller Werktage
     */
    public static List<LocalDate> getWorkdaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>();
        }

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(DateTimeUtils::isWorkday)
                .toList();
    }

    /**
     * Gibt den ersten Tag des Monats zurück
     * @param date Beliebiges Datum im gewünschten Monat
     * @return Erster Tag des Monats
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }

    /**
     * Gibt den letzten Tag des Monats zurück
     * @param date Beliebiges Datum im gewünschten Monat
     * @return Letzter Tag des Monats
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }

    /**
     * Gibt den ersten Tag der Woche (Montag) zurück
     * @param date Beliebiges Datum in der gewünschten Woche
     * @return Montag der Woche
     */
    public static LocalDate getFirstDayOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : null;
    }

    /**
     * Gibt den letzten Tag der Woche (Sonntag) zurück
     * @param date Beliebiges Datum in der gewünschten Woche
     * @return Sonntag der Woche
     */
    public static LocalDate getLastDayOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : null;
    }

    /**
     * Prüft ob ein Datum in der Vergangenheit liegt
     * @param date Das zu prüfende Datum
     * @return true wenn in der Vergangenheit
     */
    public static boolean isInPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Prüft ob ein Datum in der Zukunft liegt
     * @param date Das zu prüfende Datum
     * @return true wenn in der Zukunft
     */
    public static boolean isInFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Prüft ob ein Datum heute ist
     * @param date Das zu prüfende Datum
     * @return true wenn heute
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Prüft ob sich zwei Datumsbereiche überschneiden
     * @param start1 Start des ersten Bereichs
     * @param end1 Ende des ersten Bereichs
     * @param start2 Start des zweiten Bereichs
     * @param end2 Ende des zweiten Bereichs
     * @return true wenn sich die Bereiche überschneiden
     */
    public static boolean dateRangesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    /**
     * Berechnet die Anzahl der Tage zwischen zwei Daten
     * @param startDate Startdatum
     * @param endDate Enddatum
     * @return Anzahl der Tage (kann negativ sein wenn startDate nach endDate liegt)
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Konvertiert LocalDateTime zu Instant mit System-Zeitzone
     * @param dateTime LocalDateTime
     * @return Instant
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    /**
     * Konvertiert Instant zu LocalDateTime mit System-Zeitzone
     * @param instant Instant
     * @return LocalDateTime
     */
    public static LocalDateTime fromInstant(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    /**
     * Erstellt YearMonth aus Jahr und Monat
     * @param year Jahr
     * @param month Monat (1-12)
     * @return YearMonth
     * @throws IllegalArgumentException bei ungültigen Werten
     */
    public static YearMonth createYearMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Monat muss zwischen 1 und 12 liegen");
        }
        return YearMonth.of(year, month);
    }

    /**
     * Parst YearMonth aus String im Format "yyyy-MM"
     * @param yearMonthString String im Format "yyyy-MM"
     * @return YearMonth oder null bei ungültigem Format
     */
    public static YearMonth parseYearMonth(String yearMonthString) {
        if (yearMonthString == null || yearMonthString.trim().isEmpty()) {
            return null;
        }
        try {
            return YearMonth.parse(yearMonthString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
