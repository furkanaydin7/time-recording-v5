package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.model.Project;
import ch.fhnw.timerecordingbackend.model.SystemLog;
import ch.fhnw.timerecordingbackend.model.User;
import ch.fhnw.timerecordingbackend.repository.ProjectRepository;
import ch.fhnw.timerecordingbackend.repository.SystemLogRepository;
import ch.fhnw.timerecordingbackend.repository.TimeEntryRepository;
import ch.fhnw.timerecordingbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementierung ProjectService
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 */
@Service
public class ProjectServiceImpl implements ProjectService{

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, SystemLogRepository systemLogRepository, TimeEntryRepository timeEntryRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.systemLogRepository = systemLogRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public List<Project> findActiveProjects() {
        return projectRepository.findByActiveTrue();
    }


    @Override
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public Optional<Project> findByName(String name) {
        return projectRepository.findByName(name);
    }

    @Override
    public List<Project> findByActiveTrue() {
        return projectRepository.findByActiveTrue();
    }

    @Override
    public List<Project> findByManagerId(Long managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    @Override
    public List<Project> findByManagerIdAndActiveTrue(Long managerId) {
        return projectRepository.findByManagerIdAndActiveTrue(managerId);
    }

    /**
     * Projekt erstellen
     * @param project
     * @return
     */
    @Override
    @Transactional
    public Project createProject(Project project) {
        // Zeitstempel setzen
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        // Projekt speichern
        Project savedProject = projectRepository.save(project);

        // Log erstellen
        createSystemLog("Projekt erstellt: " + project.getName(),
                "Projekt ID: " + savedProject.getId());

        return savedProject;
    }

    /**
     * Projekt aktualisieren
     * @param id
     * @param updatedProject
     * @return
     */
    @Override
    @Transactional
    public Project updateProject(Long id, Project updatedProject) {
        // Projekt finden
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projekt nicht vorhanden"));

        // Projekt aktualisieren
        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setUpdatedAt(LocalDateTime.now());
        existingProject.setManager(updatedProject.getManager());

        Project savedProject = projectRepository.save(existingProject);

        // Log erstellen
        createSystemLog("Projekt aktualisiert: " + existingProject.getName(),
                "Projekt ID: " + existingProject.getId());

        return savedProject;
    }

    /**
     * Projekt deaktivieren
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Project deactivateProject(Long id) {
        // Projekt finden
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projekt nicht vorhanden"));

        // Projekt deaktivieren
        project.deactivate();
        Project deactivatedProject = projectRepository.save(project);

        // Log erstellen
        createSystemLog("Projekt deaktiviert: " + project.getName(),
                "Projekt ID: " + project.getId());

        return deactivatedProject;
    }

    /**
     * Projekt aktivieren
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Project activateProject(Long id) {
        // Projekt finden
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projekt nicht vorhanden"));

        // Projekt aktivieren
        project.activate();
        Project activatedProject = projectRepository.save(project);

        // Log erstellen
        createSystemLog("Projekt aktiviert: " + project.getName(),
                "Projekt ID: " + project.getId());

        return activatedProject;
    }

    /**
     * Manger Projekt zuweisen
     * @param projectId
     * @param managerId
     * @return
     */
    @Override
    @Transactional
    public Project assignManager(Long projectId, Long managerId) {
        // Projekt finden
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt nicht vorhanden"));

        // Manager finden
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht vorhanden"));

        // Prüfen ob  Benutzer Manager Rolle hat
        if (!manager.hasRole("MANAGER") && !manager.hasRole("ADMIN")) {
            throw new IllegalArgumentException("Benutzer hat keine Manager oder Admin Berechtigung");
        }

        // Manager zuweisen
        project.setManager(manager);
        project.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);

        // Log erstellen
        createSystemLog("Manager zugewiesen: " + manager.getFullName() + " zu Projekt " + project.getName(),
                "Projekt ID: " + project.getId() + ", Manager ID: " + manager.getId());

        return updatedProject;
    }

    /**
     * Manager von Projekt entfernen
     * @param projectId
     * @return
     */
    @Override
    @Transactional
    public Project removeManager(Long projectId) {
        // Projekt finden
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt nicht vorhanden"));

        String managerName = project.getManager() != null ? project.getManager().getFullName() : "Kein Manager";

        // Manager entfernen
        project.setManager(null);
        project.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);

        // Log erstellen
        createSystemLog("Manager entfernt: " + managerName + " von Projekt " + project.getName(),
                "Projekt ID: " + project.getId());

        return updatedProject;
    }

    /**
     * ActualHourse für Projekte berechnen
     * @param projectId
     * @return
     */
    @Override
    public String calculateTotalActualHoursForProject(Long projectId) {
        List<Object[]> results = timeEntryRepository.sumActualHoursByProjectId(projectId);

        if (results != null && !results.isEmpty()) {
            Object resultValue = results.get(0)[0];
            if (resultValue instanceof Number) { // Sicherstellen, dass es sich um eine Zahl handelt
                Double totalMinutes = ((Number) resultValue).doubleValue();
                long hours = (long) (totalMinutes / 60);
                long minutes = (long) (totalMinutes % 60);
                return String.format("%02d:%02d", hours, minutes);
            }
        }
        return "00:00";
    }

    // Hilfsmethode, um HH:mm String in Minuten zu parsen
    private double parseHoursToMinutes(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String[] parts = timeString.split(":");
            return Double.parseDouble(parts[0]) * 60 + Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Mitarbeiter für Projekt finden
     * @param projectId
     * @return
     */
    @Override
    public List<User> findUsersByProjectId(Long projectId) {
        // Dies wird alle Benutzer zurückgeben, die jemals Stunden auf dieses Projekt gebucht haben
        return timeEntryRepository.findDistinctUsersByProjectId(projectId);
    }

    @Override
    public List<Project> searchProjects(String searchTerm) {
        return projectRepository.searchProjects(searchTerm);
    }

    @Override
    public List<Project> findProjectsByUserId(Long userId) {
        return projectRepository.findProjectsByUserId(userId);
    }

    @Override
    public List<Project> findActiveProjectsByUserId(Long userId) {
        return projectRepository.findActiveProjectsByUserId(userId);
    }

    @Override
    public List<Project> findProjectsByManagerId(Long managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    @Override
    public List<Project> findActiveProjectsByManagerId(Long managerId) {
        return projectRepository.findByManagerIdAndActiveTrue(managerId);
    }

    @Override
    public long countUsersByProjectId(Long projectId) {
        return projectRepository.countUsersByProjectId(projectId);
    }

    @Override
    public boolean existsByName(String name) {
        return projectRepository.existsByName(name);
    }

    /**
     * Erstellen von Systemlogs
     * @param action
     * @param details
     */
    private void createSystemLog(String action, String details) {
        SystemLog log = new SystemLog();
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        systemLogRepository.save(log);
    }
}
