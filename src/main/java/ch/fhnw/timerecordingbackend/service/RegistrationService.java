package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.registration.RegistrationRequest;
import ch.fhnw.timerecordingbackend.model.Registration;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface für Registrierungsanfragen
 * @author PD
 */
public interface RegistrationService {

    Registration submitRegistrationRequest(RegistrationRequest requestDto);

    // Rückgabetypen ändern sich auf die umbenannte Entität
    List<Registration> getAllPendingRequests(); // ENTITÄTSNAME GEÄNDERT
    List<Registration> getAllRequests(); // ENTITÄTSNAME GEÄNDERT
    Optional<Registration> getRequestById(Long id); // ENTITÄTSNAME GEÄNDERT

    void approveRegistrationRequest(Long requestId, String adminEmail);
    void rejectRegistrationRequest(Long requestId, String adminEmail);
}
