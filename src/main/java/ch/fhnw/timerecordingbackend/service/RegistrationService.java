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

    List<Registration> getAllPendingRequests();

    List<Registration> getAllRequests();

    Optional<Registration> getRequestById(Long id);

    void approveRegistrationRequest(Long requestId, String adminEmail);

    void rejectRegistrationRequest(Long requestId, String adminEmail);
}
