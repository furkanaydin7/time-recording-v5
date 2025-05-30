package ch.fhnw.timerecordingbackend.dto.project;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO Anfragen zum Erstellen und aktualisieren von Projekten
 * @author PD
 * Code von anderen Teammitgliedern oder Quellen wird durch einzelne Kommentare deklariert
 * @version 1.0
 * Quelle: https://medium.com/paysafe-bulgaria/springboot-dto-validation-good-practices-and-breakdown-fee69277b3b0
 */
public class ProjectRequest {
    @NotBlank(message = "Projektname darf nicht leer sein")
    @Size(min = 2, max = 100, message = "Projektname muss zwischen 2 und 50 Zeichen lang sein")
    private String name;

    @Size(min = 2, max = 255, message = "Projektbeschreibung muss zwischen 2 und 255 Zeichen lang sein")
    private String description;

    @NotNull(message = "Projektmanager ID darf nicht leer sein")
    private Long managerId;

    /**
     * Konstruktoren
     */

    public ProjectRequest() {}

    public ProjectRequest(String name, String description, Long managerId) {
        this.name = name;
        this.description = description;
        this.managerId = managerId;
    }

    /**
     * Getter und Setter
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    @Override
    public String toString() {
        return "ProjectRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                '}';
    }
}
