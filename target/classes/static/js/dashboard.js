// * @author EK
//Kommentare erzeugt it @ChatGPT
//Code überarbeitet mit @ChatGPT

window.projects = []; // Globale Variable für Projekte

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Dashboard wird initialisiert (aus dashboard.js)...');
    const jwtToken = localStorage.getItem('jwtToken');

    if (!jwtToken) {
        console.log('❌ Kein JWT Token gefunden, Weiterleitung zum Login.');
        window.location.href = '/'; // Oder index.html
        return;
    }

    const userEmailFromStorage = localStorage.getItem('userEmail');
    const userEmailSpan = document.getElementById('userEmail');
    if (userEmailSpan && userEmailFromStorage) {
        userEmailSpan.textContent = userEmailFromStorage;
    }

    // Admin-Features initialisieren (zeigt/versteckt Admin-Karte etc.)
    if (typeof initializeAdminFeatures === 'function') {
        initializeAdminFeatures();
    } else {
        console.warn("initializeAdminFeatures ist nicht definiert. Stelle sicher, dass admin.js korrekt geladen wurde.");
    }

    // Dashboard-Daten laden (Statistiken etc.)
    loadDashboardPageData();

    // Alle Event-Listener binden
    bindDashboardEventListeners();

    console.log('🎉 Dashboard initialisiert und Event Listener gebunden!');
});

function bindDashboardEventListeners() {
    // Logout Button
    const logoutButton = document.querySelector('.logout-btn'); // Besser über ID, falls vorhanden
    if (logoutButton) logoutButton.addEventListener('click', logout);

    // Passwort ändern Icon
    const changePwdIcon = document.getElementById('changePasswordIcon');
    if (changePwdIcon) changePwdIcon.addEventListener('click', openChangePasswordModal);

    // Passwort ändern Formular
    const changePwdForm = document.getElementById('changePasswordForm');
    if (changePwdForm) changePwdForm.addEventListener('submit', handleChangePasswordSubmit);

    // --- Zeiterfassung Karte ---
    document.getElementById('startTimer')?.addEventListener('click', startTimeTracking);
    document.getElementById('stopTimer')?.addEventListener('click', stopTimeTracking);

    const openManualEntryModalBtn = document.getElementById('openManualEntryModalBtn');
    if (openManualEntryModalBtn) openManualEntryModalBtn.addEventListener('click', openManualEntryModal);

    const viewTimeEntriesBtn = document.getElementById('viewTimeEntriesBtn');
    if (viewTimeEntriesBtn) viewTimeEntriesBtn.addEventListener('click', viewTimeEntries);
    document.getElementById('viewEmployeeTimeEntriesBtn')?.addEventListener('click', viewEmployeeTimeEntriesHandler);


    // --- Projekte Karte ---
    const viewProjectsBtn = document.getElementById('viewProjectsBtn');
    if (viewProjectsBtn) viewProjectsBtn.addEventListener('click', viewProjects);

    const createProjectBtn = document.getElementById('createProjectBtn');
    if (createProjectBtn) createProjectBtn.addEventListener('click', openCreateProjectModal);


    // --- Abwesenheiten Karte ---
    const viewAbsencesBtn = document.getElementById('viewAbsencesBtn');
    if (viewAbsencesBtn) viewAbsencesBtn.addEventListener('click', viewAbsences);

    const openCreateAbsenceModalBtn = document.getElementById('openCreateAbsenceModalBtn');
    if (openCreateAbsenceModalBtn) openCreateAbsenceModalBtn.addEventListener('click', openCreateAbsenceModal);

    const viewPendingAbsencesBtn = document.getElementById('viewPendingAbsencesBtn');
    if (viewPendingAbsencesBtn) viewPendingAbsencesBtn.addEventListener('click', viewPendingAbsencesForApproval);

    const viewTeamOrAllApprovedAbsencesBtn = document.getElementById('viewTeamOrAllApprovedAbsencesBtn');
    if (viewTeamOrAllApprovedAbsencesBtn) viewTeamOrAllApprovedAbsencesBtn.addEventListener('click', viewTeamOrAllApprovedAbsencesHandler);


    // --- Admin Panel Buttons ---
    const viewUsersBtn = document.getElementById('viewUsersBtn');
    if (viewUsersBtn) viewUsersBtn.addEventListener('click', viewUsers);

    const openCreateUserModalBtn = document.getElementById('openCreateUserModalBtn');
    if (openCreateUserModalBtn) openCreateUserModalBtn.addEventListener('click', openCreateUserModal);

    document.getElementById('viewRegistrationRequestsBtn')?.addEventListener('click', viewRegistrationRequests);
    document.querySelector('button[onclick="viewSystemLogs()"]')?.setAttribute('id', 'viewSystemLogsBtn');

    const viewSystemLogsBtn = document.getElementById('viewSystemLogsBtn');
    if (viewSystemLogsBtn) viewSystemLogsBtn.addEventListener('click', viewSystemLogs);

    const debugTokenBtn = document.getElementById('debugTokenBtn');
    if (debugTokenBtn) debugTokenBtn.addEventListener('click', debugToken);

    const checkSystemStatusBtn = document.getElementById('checkSystemStatusBtn');
    if (checkSystemStatusBtn) checkSystemStatusBtn.addEventListener('click', checkSystemStatus);

    // NEUER Listener für den "Passwort-Resets anzeigen" Button (ID aus admin.js)
    const viewPasswordResetRequestsBtn = document.getElementById('viewPasswordResetRequestsBtn');
    if (viewPasswordResetRequestsBtn) {
        viewPasswordResetRequestsBtn.addEventListener('click', viewPasswordResetRequests);
    }


    // Data Display Schließen Button
    const hideDataDisplayBtn = document.getElementById('hideDataDisplayBtn'); // ID ist besser
    if (hideDataDisplayBtn) {
        hideDataDisplayBtn.addEventListener('click', hideDataDisplay);
    }

    // --- Formular-Listener für Modals (Submit-Handler) ---
    document.getElementById('manualEntryForm')?.addEventListener('submit', handleManualEntrySubmit);
    document.getElementById('editTimeEntryForm')?.addEventListener('submit', handleEditTimeEntrySubmit);
    document.getElementById('createProjectForm')?.addEventListener('submit', handleCreateProjectSubmit);
    document.getElementById('createAbsenceForm')?.addEventListener('submit', handleCreateAbsenceSubmit);
    document.getElementById('editAbsenceForm')?.addEventListener('submit', handleEditAbsenceSubmit);
    document.getElementById('editProjectForm')?.addEventListener('submit', handleEditProjectSubmit);
    document.getElementById('deleteProjectBtn')?.addEventListener('click', handleDeleteProject);
    document.getElementById('createUserForm')?.addEventListener('submit', handleCreateUserSubmit);

    // Event Listener für Zeitberechnung im manuellen Eintrag Modal
    document.getElementById('manualStartTime')?.addEventListener('change', calculateWorkTime);
    document.getElementById('manualEndTime')?.addEventListener('change', calculateWorkTime);
    document.getElementById('manualBreakStartTime')?.addEventListener('change', calculateWorkTime);
    document.getElementById('manualBreakEndTime')?.addEventListener('change', calculateWorkTime);

    // Event Listener für Start-/Enddatum Validierung bei Abwesenheiten (Create Modal)
    const createAbsenceStartDate = document.getElementById('startDate'); // ID aus createAbsenceModal
    if (createAbsenceStartDate) {
        createAbsenceStartDate.addEventListener('change', function() {
            const endDateInput = document.getElementById('endDate'); // ID aus createAbsenceModal
            if (endDateInput) {
                endDateInput.min = this.value;
                if (endDateInput.value && new Date(endDateInput.value) < new Date(this.value)) {
                    endDateInput.value = this.value;
                }
            }
        });
    }
    // Event Listener für Start-/Enddatum Validierung bei Abwesenheiten (Edit Modal)
    const editAbsenceStartDate = document.getElementById('editAbsenceStartDate');
    if (editAbsenceStartDate) {
        editAbsenceStartDate.addEventListener('change', function() {
            const endDateInput = document.getElementById('editAbsenceEndDate');
            if (endDateInput) {
                endDateInput.min = this.value;
                if (endDateInput.value && new Date(endDateInput.value) < new Date(this.value)) {
                    endDateInput.value = this.value;
                }
            }
        });
    }


    // --- Listener für Benutzerdetails-Modal (userDetailModal) ---
    const updateUserStatusBtn = document.getElementById('updateUserStatusBtn');
    if (updateUserStatusBtn) {
        updateUserStatusBtn.addEventListener('click', async function() {
            if (!window.selectedUserForDetails || !window.selectedUserForDetails.id) {
                showError('Kein Benutzer ausgewählt oder Benutzer-ID fehlt.');
                return;
            }
            const statusSelect = document.getElementById('userStatusSelect');
            if (!statusSelect) {
                showError('Status-Auswahlfeld nicht gefunden.');
                return;
            }
            const newStatus = statusSelect.value;
            try {
                await apiCall(`/api/admin/users/${window.selectedUserForDetails.id}/status?status=${newStatus}`, { method: 'PATCH' });
                showSuccess(`Status für ${window.selectedUserForDetails.firstName} ${window.selectedUserForDetails.lastName} erfolgreich auf ${newStatus} aktualisiert.`);
                const user = await apiCall(`/api/admin/users/${window.selectedUserForDetails.id}`);
                if (user) {
                    window.selectedUserForDetails = user;
                    document.getElementById('detailUserStatus').textContent = user.status ? String(user.status) : (user.active ? 'Aktiv' : 'Inaktiv');
                    if (document.getElementById('dataTitle')?.textContent === 'Alle Benutzer') {
                        viewUsers();
                    }
                }
            } catch (error) {
                showError('Fehler beim Aktualisieren des Status: ' + (error.message || "Unbekannt"));
            }
        });
    }

    const addRoleBtn = document.getElementById('addRoleBtn');
    if (addRoleBtn) {
        addRoleBtn.addEventListener('click', async function() {
            if (!window.selectedUserForDetails || !window.selectedUserForDetails.id) {
                showError('Kein Benutzer ausgewählt oder Benutzer-ID fehlt.');
                return;
            }
            const roleSelect = document.getElementById('addRoleSelect');
            if (!roleSelect) {
                showError('Rollen-Auswahlfeld nicht gefunden.');
                return;
            }
            const roleName = roleSelect.value;
            if (!roleName) {
                showError('Bitte wählen Sie eine Rolle zum Hinzufügen aus.');
                return;
            }
            try {
                await apiCall(`/api/admin/users/${window.selectedUserForDetails.id}/roles?roleName=${roleName}`, { method: 'POST' });
                showSuccess(`Rolle ${roleName} erfolgreich zu ${window.selectedUserForDetails.firstName} ${window.selectedUserForDetails.lastName} hinzugefügt.`);
                const user = await apiCall(`/api/admin/users/${window.selectedUserForDetails.id}`);
                if (user) {
                    window.selectedUserForDetails = user;
                    await loadRolesForAddDropdown(user.roles || []);
                    document.getElementById('userRolesList').innerHTML = (user.roles?.map(r => `<span class="user-role-tag">${String(r).replace('ROLE_', '')} <button class="remove-role-btn" data-role="${r}" onclick="handleRemoveRole('${String(r)}')">&times;</button></span>`).join(' ') || 'Keine Rollen');
                    if (document.getElementById('dataTitle')?.textContent === 'Alle Benutzer') {
                        viewUsers();
                    }
                }
            } catch (error) {
                showError('Fehler beim Hinzufügen der Rolle: ' + (error.message || "Unbekannt"));
            }
        });
    }

    const resetPasswordBtn = document.getElementById('resetPasswordBtn');
    if (resetPasswordBtn && !resetPasswordBtn.getAttribute('data-listener-attached')) {
        resetPasswordBtn.addEventListener('click', async function() {
            console.log('Passwort zurücksetzen Button geklickt (aus dashboard.js).');
            console.log('Aktueller Wert von window.selectedUserForDetails beim Klick:', window.selectedUserForDetails);

            if (!window.selectedUserForDetails || !window.selectedUserForDetails.id) {
                console.log('Bedingung (!window.selectedUserForDetails || !window.selectedUserForDetails.id) ist WAHR');
                showError('Kein Benutzer ausgewählt oder Benutzer-ID fehlt.');
                return;
            }
            console.log('Benutzer für Reset ausgewählt:', window.selectedUserForDetails.firstName, window.selectedUserForDetails.lastName);

            if (confirm(`Möchten Sie das Passwort für ${window.selectedUserForDetails.firstName} ${window.selectedUserForDetails.lastName} wirklich zurücksetzen?`)) {
                console.log('Passwort-Reset bestätigt.');
                try {
                    const response = await apiCall(`/api/admin/users/${window.selectedUserForDetails.id}/reset-password`, { method: 'POST' });
                    console.log('Antwort vom Passwort-Reset API-Call:', response);
                    alert(`✅ Passwort zurückgesetzt. Temporäres Passwort für ${window.selectedUserForDetails.firstName} ${window.selectedUserForDetails.lastName}: ${response.temporaryPassword}`);
                } catch (error) {
                    console.error('Fehler beim API-Call zum Passwort-Reset:', error);
                    showError('Fehler beim Zurücksetzen des Passworts: ' + (error.message || "Unbekannt"));
                }
            } else {
                console.log('Passwort-Reset abgebrochen.');
            }
        });
        resetPasswordBtn.setAttribute('data-listener-attached', 'true');
    }


    // --- Globale Modal-Schließmechanismen ---
    // Schließen bei Klick außerhalb des Modal-Contents
    window.addEventListener('click', function(event) {
        document.querySelectorAll('.modal').forEach(modal => {
            if (event.target === modal) {
                closeModal(modal.id);
            }
        });
    });

    // Schließen bei Klick auf .close Buttons (X)
    document.querySelectorAll('.modal .close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const modalId = this.getAttribute('data-modal-id-to-close') || this.closest('.modal')?.id;
            if (modalId) {
                closeModal(modalId);
            }
        });
    });

    // --- Schließen-Buttons für spezifische Modals (Cancel-Buttons) ---
    const cancelButtons = [
        { id: 'cancelManualEntryBtn', modal: 'manualEntryModal' },
        { id: 'cancelEditTimeEntryBtn', modal: 'editTimeEntryModal' },
        { id: 'cancelCreateProjectBtn', modal: 'createProjectModal' },
        { id: 'cancelCreateAbsenceBtn', modal: 'createAbsenceModal' },
        { id: 'cancelEditAbsenceBtn', modal: 'editAbsenceModal' },
        { id: 'cancelCreateUserBtn', modal: 'createUserModal' },
        { id: 'cancelChangePasswordBtn', modal: 'changePasswordModal' },
        { id: 'closeProjectDetailModalBtn', modal: 'projectDetailModal' },
        { id: 'closeUserDetailModalBtn', modal: 'userDetailModal' }
    ];

    cancelButtons.forEach(buttonConfig => {
        const button = document.getElementById(buttonConfig.id);
        if (button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                if (buttonConfig.modal) {
                    closeModal(buttonConfig.modal);
                } else if (buttonConfig.action === 'hideDataDisplay') {
                    hideDataDisplay();
                }
            });
        }
    });

    // --- Buttons im Duplikat-Info Modal ---
    const duplicateInfoModal = document.getElementById('duplicateInfoModal');
    if (duplicateInfoModal) {
        document.getElementById('duplicateInfoViewEntriesBtn')?.addEventListener('click', function() {
            viewTimeEntries(); closeModal('duplicateInfoModal');
        });
        document.getElementById('duplicateInfoChangeDateBtn')?.addEventListener('click', function() {
            openManualEntryModal(); closeModal('duplicateInfoModal');
        });
        document.getElementById('duplicateInfoCloseBtn')?.addEventListener('click', function() {
            closeModal('duplicateInfoModal');
        });
    }

    // --- Dynamisch hinzugefügte Elemente (z.B. Time/Break Slots) ---
    document.getElementById('addEditTimeSlotBtn')?.addEventListener('click', function(e) {
        e.preventDefault(); addEditTimeSlot();
    });
    document.getElementById('addEditBreakSlotBtn')?.addEventListener('click', function(e) {
        e.preventDefault(); addEditBreakSlot();
    });


    // --- Keyboard Shortcuts ---
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {

            const openModals = document.querySelectorAll('.modal[style*="display: block"], .modal[style*="display:block"]');
            if (openModals.length > 0) {
                closeModal(openModals[openModals.length - 1].id);
            } else if (document.getElementById('dataDisplay')?.style.display === 'block') {
                hideDataDisplay();
            }
        }
        // STRG + Enter für Start/Stop Timer
        if (e.ctrlKey && e.key === 'Enter') {
            e.preventDefault(); // Verhindert Standardverhalten (z.B. Formular absenden)
            const startTimerBtn = document.getElementById('startTimer');
            const stopTimerBtn = document.getElementById('stopTimer');

            if (stopTimerBtn && stopTimerBtn.style.display !== 'none') { // Wenn Stop-Button sichtbar ist
                stopTimeTracking();
            } else if (startTimerBtn && startTimerBtn.style.display !== 'none') { // Wenn Start-Button sichtbar ist
                startTimeTracking();
            }
        }
        // STRG + SHIFT + D für Debug Token
        if (e.ctrlKey && e.shiftKey && (e.key === 'D' || e.key === 'd')) {
            e.preventDefault();
            if (typeof debugToken === 'function') debugToken();
        }
    });
}


async function loadDashboardPageData(forceRefresh = false) {
    console.log('📊 Lade Dashboard-Seiten-Daten...');
    const loadingDiv = document.getElementById('loading');
    if (loadingDiv) loadingDiv.style.display = 'block';

    try {
        const cacheBuster = forceRefresh ? `?_t=${Date.now()}` : '';

        // Zeiteinträge für Statistiken (Wochenstunden, Einträge heute)
        const timeEntriesResponse = await apiCall(`/api/time-entries${cacheBuster}`);
        if (timeEntriesResponse && timeEntriesResponse.entries) {
            const today = new Date();
            const todayEntries = timeEntriesResponse.entries.filter(entry => {
                const entryDate = new Date(entry.date);
                return entryDate.toDateString() === today.toDateString();
            });
            const entryCountEl = document.getElementById('entryCount');
            if (entryCountEl) entryCountEl.textContent = todayEntries.length;

            const totalHoursEl = document.getElementById('totalHours');
            if (totalHoursEl) totalHoursEl.textContent = calculateWeekHoursForDashboard(timeEntriesResponse.entries);
        }

        // Aktive Projekte für Statistik und Dropdowns
        const projectsResponse = await apiCall(`/api/projects/active${cacheBuster}`);
        if (projectsResponse && projectsResponse.projects) {
            window.projects = projectsResponse.projects; // Globale Variable aktualisieren
            const projectCountEl = document.getElementById('projectCount');
            if (projectCountEl) projectCountEl.textContent = window.projects.length;

            populateProjectDropdown(document.getElementById('manualProject'), window.projects);
            populateProjectDropdown(document.getElementById('editProject'), window.projects);
        }
        const absencesResponse = await apiCall(`/api/absences${cacheBuster}`);
        if (absencesResponse && absencesResponse.absences) {
            const pending = absencesResponse.absences.filter(absence => absence.status === 'PENDING'); // Annahme: Status ist PENDING
            const pendingAbsencesEl = document.getElementById('pendingAbsences');
            if (pendingAbsencesEl) pendingAbsencesEl.textContent = pending.length;
        }

    } catch (error) {
        console.error('❌ Fehler beim Laden der Dashboard-Seiten-Daten:', error);
        showError('Fehler beim Laden der Dashboard-Daten: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    } finally {
        if (loadingDiv) loadingDiv.style.display = 'none';
    }
}

async function viewEmployeeTimeEntriesHandler() {
    try {
        hideDataDisplay();
        const userRolesString = localStorage.getItem('userRoles');
        let userRoles = [];
        if (userRolesString) userRoles = JSON.parse(userRolesString);

        let response;
        let title = "Zeiteinträge Mitarbeiter";

        if (userRoles.some(role => String(role).toUpperCase() === 'ADMIN')) {
            response = await apiCall(`/api/time-entries/all?_t=${Date.now()}`);
            title = "Alle Zeiteinträge (Admin)";
        } else if (userRoles.some(role => String(role).toUpperCase() === 'MANAGER')) {
            response = await apiCall(`/api/time-entries/team?_t=${Date.now()}`);
            title = "Zeiteinträge Team (Manager)";
        } else {
            showError("Keine Berechtigung, diese Einträge anzuzeigen.");
            return;
        }

        if (response && response.entries) {
            console.log(`✅ <span class="math-inline">\{response\.entries\.length\} Zeiteinträge für '</span>{title}' geladen`);
            displayData(title, formatTimeEntriesTable(response.entries));
        } else {
            displayData(title, '<p>Keine Zeiteinträge gefunden.</p>');
        }
    } catch (error) {
        console.error('❌ Fehler beim Laden der Mitarbeiter-Zeiteinträge:', error);
        showError('Fehler beim Laden der Mitarbeiter-Zeiteinträge: ' + (error.message || "Unbekannt"));
    }
}

function calculateWeekHoursForDashboard(entries) {
    const now = new Date();

    const currentDayOfWeek = now.getDay() === 0 ? 6 : now.getDay() -1; // 0 (Mo) bis 6 (So)

    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - currentDayOfWeek); // Gehe zum Montag zurück
    weekStart.setHours(0, 0, 0, 0);

    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6); // Sonntag dieser Woche
    weekEnd.setHours(23, 59, 59, 999);

    let totalMinutes = 0;
    if (Array.isArray(entries)) {
        entries.forEach(entry => {
            if (entry && entry.date && entry.actualHours) {
                const entryDate = new Date(entry.date);
                if (entryDate >= weekStart && entryDate <= weekEnd) {
                    totalMinutes += parseTimeToMinutes(entry.actualHours);
                }
            }
        });
    }
    return formatMinutesToHours(totalMinutes);
}

setInterval(() => {
    let isTimerCurrentlyActive = false;
    if (typeof activeTimeEntry !== 'undefined' && activeTimeEntry != null) {
        isTimerCurrentlyActive = true;
    }

    if (document.visibilityState === 'visible' && !isTimerCurrentlyActive) {
        console.log('Automatisches Neuladen der Dashboard-Daten...');
        loadDashboardPageData(true);
    }
}, 5 * 60 * 1000); // 5 Minuten

// Service Worker Registrierung (optional, für Offline-Fähigkeiten oder Caching)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        console.log('🔧 Service Worker Unterstützung im Browser erkannt.');

    });
}