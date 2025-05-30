package ch.fhnw.timerecordingbackend.excpetion;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Globale Ausnahmebehandlung für REST-Controller.
 * Fängt Validierungs-, Status- und andere Ausnahmen und formatiert einheitliche JSON-Antworten.
 * Beinhaltet Handler für MethodArgumentNotValidException, ValidationException,
 * IllegalArgumentException, ResponseStatusException, AccessDeniedException und alle anderen Exceptions.
 * @author FA
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * Kommentare und Code wurden mithilfe von KI ergänzt und erweitert.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Behandelt Validierungsfehler bei @Valid-annotierten Requests.
     * Überschreibt die Methode aus ResponseEntityExceptionHandler.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Fehlerdetails aus BindingResult extrahieren
        Map<String, Object> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        // Standardfelder für die Fehlerantwort
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("errors", errors);
        body.put("message", "Validation failed for arguments");
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Behandelt Validierungs-Ausnahmen aus dem Service-Layer.
     * Antwort mit HTTP 400 Bad Request.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage()); // Meldung aus der Exception
        // Pfad der Anfrage extrahieren (ohne 'uri=' Präfix)
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt IllegalArgumentException und gibt Bad Request zurück.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt ResponseStatusException für eigene Service- und Controller-Fehler.
     * Nutzt den in der Exception definierten HTTP-Status.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", ex.getStatusCode().value());
        // Ermittelt den Reason-Phrase des HTTP-Status
        body.put("error", HttpStatus.resolve(ex.getStatusCode().value()).getReasonPhrase());
        body.put("message", ex.getReason());
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Behandelt AccessDeniedException für fehlende Berechtigungen (HTTP 403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Fallback-Handler für alle anderen ungeprüften Exceptions.
     * Gibt HTTP 500 Internal Server Error zurück.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred: " + ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
