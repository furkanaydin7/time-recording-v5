// * @author EK
//Code überarbeitet mit ChatGPT

let currentProjectForEditing = null;
let availableManagers = [];

async function viewProjects() {
    try {
        const response = await apiCall('/api/projects/active');
        if (response && response.projects) {
            displayData('Aktive Projekte', formatProjectsTable(response.projects));
        } else {
            displayData('Aktive Projekte', '<p>Keine aktiven Projekte gefunden.</p>');
        }
    } catch (error) {
        showError('Fehler beim Laden der Projekte: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    }
}

async function loadManagersForProjectCreation() {
    try {
        const response = await apiCall('/api/admin/users'); // Alle User für Manager-Auswahl
        const managerSelect = document.getElementById('projectManager');
        if (!managerSelect) return;
        managerSelect.innerHTML = '<option value="">Wählen Sie einen Manager</option>';

        if (response && Array.isArray(response)) {
            response.forEach(user => {
                const userRoles = user.roles || [];
                const isManagerOrAdmin = userRoles.some(role =>
                    String(role).includes('ADMIN') || String(role).includes('MANAGER')
                );
                if (isManagerOrAdmin) {
                    const option = document.createElement('option');
                    option.value = user.id;
                    option.textContent = `${user.firstName} ${user.lastName} (${user.email})`;
                    managerSelect.appendChild(option);
                }
            });
        }
    } catch (error) {
        console.error('Fehler beim Laden der Manager:', error);
        showError('Fehler beim Laden der Manager-Liste: ' + (error.message || "Unbekannt"));
    }
}

async function openCreateProjectModal() {
    await loadManagersForProjectCreation();
    openModal('createProjectModal');
}

async function handleCreateProjectSubmit(event) {
    event.preventDefault();
    hideAllMessages();
    const projectName = document.getElementById('projectName').value;
    const projectDescription = document.getElementById('projectDescription').value;
    const projectManagerId = document.getElementById('projectManager').value;

    if (!projectManagerId) {
        showError('Bitte wählen Sie einen Projektmanager aus.'); return;
    }
    const projectData = {
        name: projectName,
        description: projectDescription,
        managerId: parseInt(projectManagerId)
    };

    try {
        const response = await apiCall('/api/projects', { method: 'POST', body: projectData });
        if (response && response.id) { // Backend sendet {id, message}
            showSuccess('✅ Projekt erfolgreich erstellt!');
            closeModal('createProjectModal');
            document.getElementById('createProjectForm').reset();
            loadDashboardPageData(true); // Dashboard aktualisieren
            viewProjects(); // Projektliste aktualisieren
        } else {
            showError('Fehler beim Erstellen des Projekts: Unerwartete Antwort vom Server.');
        }
    } catch (error) {
        showError('❌ Fehler beim Erstellen des Projekts: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    }
}


async function loadManagersForEditDropdown() {
    try {
        const response = await apiCall('/api/admin/users');
        availableManagers = [];
        if (response && Array.isArray(response)) {
            response.forEach(user => {
                const userRoles = user.roles || [];
                const isManagerOrAdmin = userRoles.some(role => String(role).includes('ADMIN') || String(role).includes('MANAGER'));
                if (isManagerOrAdmin) availableManagers.push(user);
            });
        }
        const managerSelect = document.getElementById('editProjectManager');
        if (!managerSelect) return;
        managerSelect.innerHTML = '<option value="">Nicht zugewiesen</option>';
        availableManagers.forEach(manager => {
            const option = document.createElement('option');
            option.value = manager.id;
            option.textContent = `${manager.firstName} ${manager.lastName} (${manager.email})`;
            managerSelect.appendChild(option);
        });
    } catch (error) {
        showError('Fehler beim Laden der Manager-Liste für Bearbeitung.');
    }
}

async function showProjectDetails(projectId) {
    try {
        const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
        const isAdmin = userRoles.some(r => String(r).includes('ADMIN'));
        const isManager = userRoles.some(r => String(r).includes('MANAGER'));
        const canEdit = isAdmin || isManager;

        const project = await apiCall(`/api/projects/${projectId}`);
        if (project) {
            currentProjectForEditing = project;
            document.getElementById('detailProjectName').textContent = project.name;
            document.getElementById('detailProjectDescription').textContent = project.description || 'N/A';
            document.getElementById('detailProjectManager').textContent = project.managerName || 'Nicht zugewiesen';
            const statusElement = document.getElementById('detailProjectStatus');
            statusElement.textContent = project.active ? 'Aktiv' : 'Inaktiv';
            statusElement.style.color = project.active ? '#28a745' : '#dc3545';
            document.getElementById('detailProjectCreatedAt').textContent = formatDateTimeDisplay(project.createdAt);
            document.getElementById('detailProjectUpdatedAt').textContent = formatDateTimeDisplay(project.updatedAt);
            document.getElementById('detailProjectTotalHours').textContent = project.statistics?.totalHoursWorked || '00:00';
            document.getElementById('detailProjectInvolvedEmployees').textContent = project.statistics?.activeEmployees || 0;

            const usersList = document.getElementById('detailProjectUsersList');
            usersList.innerHTML = '';
            if (project.involvedUsers && project.involvedUsers.length > 0) {
                project.involvedUsers.forEach(user => {
                    const listItem = document.createElement('li');
                    listItem.textContent = `${user.firstName} ${user.lastName} (${user.email})`;
                    usersList.appendChild(listItem);
                });
            } else { usersList.innerHTML = '<li>Keine Mitarbeiter haben Stunden auf dieses Projekt gebucht.</li>';}

            const projectEditSection = document.getElementById('projectEditSection');
            if (canEdit) {
                projectEditSection.style.display = 'block';
                document.getElementById('editProjectId').value = project.id;
                document.getElementById('editProjectStatus').value = project.active ? 'true' : 'false';
                await loadManagersForEditDropdown();
                const editProjectManagerSelect = document.getElementById('editProjectManager');
                editProjectManagerSelect.value = project.managerId || '';
                document.getElementById('deleteProjectBtn').style.display = isAdmin ? 'inline-block' : 'none';
            } else {
                projectEditSection.style.display = 'none';
            }
            openModal('projectDetailModal');
        } else {
            showError('Projekt Details nicht gefunden.');
        }
    } catch (error) {
        showError('Fehler beim Laden der Projektdetails: ' + (error.message || "Unbekannt"));
    }
}

async function handleEditProjectSubmit(event) {
    event.preventDefault();
    hideAllMessages();
    const projectId = document.getElementById('editProjectId').value;
    const newStatus = document.getElementById('editProjectStatus').value === 'true';
    const newManagerId = document.getElementById('editProjectManager').value;

    try {
        // Status-Änderung
        if (currentProjectForEditing && currentProjectForEditing.active !== newStatus) {
            const statusEndpoint = newStatus ? `/api/projects/${projectId}/activate` : `/api/projects/${projectId}/deactivate`;
            await apiCall(statusEndpoint, { method: 'PATCH' });
            showSuccess(`Projekt erfolgreich ${newStatus ? 'aktiviert' : 'deaktiviert'}!`);
        }

        // Manager-Änderung
        const currentManagerId = currentProjectForEditing?.managerId?.toString() || '';
        if (newManagerId !== currentManagerId) {
            if (newManagerId === '') { // Manager entfernen
                await apiCall(`/api/projects/${projectId}/manager`, { method: 'DELETE' });
                showSuccess('Manager erfolgreich vom Projekt entfernt!');
            } else { // Manager zuweisen/ändern
                await apiCall(`/api/projects/${projectId}/manager`, { method: 'POST', body: { managerId: parseInt(newManagerId) } });
                showSuccess('Manager erfolgreich zugewiesen!');
            }
        }
        closeModal('projectDetailModal');
        loadDashboardPageData(true);
        viewProjects();
    } catch (error) {
        showError('Fehler beim Speichern der Projektänderungen: ' + (error.message || "Unbekannt"));
    }
}

async function handleDeleteProject() {
    const projectId = document.getElementById('editProjectId').value;
    if (confirm('Sind Sie sicher, dass Sie dieses Projekt löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden und deaktiviert das Projekt nur.')) {
        try {

            await apiCall(`/api/projects/${projectId}/deactivate`, { method: 'PATCH' });
            showSuccess('Projekt erfolgreich deaktiviert!');
            closeModal('projectDetailModal');
            loadDashboardPageData(true);
            viewProjects();
        } catch (error) {
            showError('Fehler beim Deaktivieren des Projekts: ' + (error.message || "Unbekannt"));
        }
    }
}