package ch.fhnw.timerecordingbackend.model.enums;

/**
 * Abwesenheitsarten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 */
public enum AbsenceType {
    VACATION("Urlaub"),
    ILLNESS("Krankheit"),
    HOME_OFFICE("Home Office"),
    TRAINING("Fortbildung"),
    PUBLIC_HOLIDAY("Feiertag"),
    UNPAID_LEAVE("Unbezahlte Urlaubstage"),
    SPECIAL_LEAVE("Sonderurlaub"),
    OTHER("Sonstige");

    /**String für Anzeigenamen
     * Quelle: https://coderanch.com/t/469226/java/Java-enums-space-display-values
     */
    private final String displayName;

    AbsenceType(String displayName) {
        this.displayName = displayName;
    }

    // Gibt den Anzeigename zurück
    public String getDisplayName() {
        return displayName;
    }

    /**
     * AbsenceType aus DisplayName ermitteln
     * @param displayName = Anzeigename
     * @return AbsenceType
     */
    public static AbsenceType fromDisplayName(String displayName) {
        for (AbsenceType t : AbsenceType.values()) {
            if (t.getDisplayName().equals(displayName)) {
                return t;
            }
        }
        return null;
    }
}
