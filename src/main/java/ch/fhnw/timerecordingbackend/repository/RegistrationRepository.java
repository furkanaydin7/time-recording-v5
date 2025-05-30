package ch.fhnw.timerecordingbackend.repository;

import ch.fhnw.timerecordingbackend.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Registrierungsanfragen
 * @author PD
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Registration> findByStatus(String status);
}
