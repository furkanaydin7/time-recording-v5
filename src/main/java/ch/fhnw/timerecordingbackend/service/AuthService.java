package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.authentication.LoginRequest;
import ch.fhnw.timerecordingbackend.dto.authentication.LoginResponse;

/**
 * Service-Interface für Authentifizierungsoperationen.
 * Definiert Methoden für Login mit JWT-Token-Erstellung und Logout.
 * Implementierungen übernehmen die Validierungslogik und Token-Management.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
public interface AuthService {

    /**
     * Authentifiziert einen Nutzer anhand von LoginRequest und erstellt einen JWT-Token.
     * @param loginRequest DTO mit E-Mail und Passwort des Nutzers
     * @return LoginResponse-DTO mit generiertem Token und Nutzerinformationen
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Invalidiert einen bestehenden JWT-Token beim Logout.
     * @param token Das zu invalidierende JWT-Token
     */
    void logout(String token);
}
