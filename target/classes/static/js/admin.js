// * @author EK

console.log('admin.js geladen');
window.selectedUserForDetails = null;
console.log('Initial window.selectedUserForDetails:', window.selectedUserForDetails);

async function viewUsers() {
    try {
        const response = await apiCall('/api/admin/users');
        if (response && Array.isArray(response)) {
            displayData('Alle Benutzer', formatUsersTable(response));
        } else {
            displayData('Alle Benutzer', '<p>Keine Benutzer gefunden oder ungültige Antwort.</p>');
        }
    } catch (error) {
        showError('Fehler beim Laden der Benutzer: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    }
}

// Funktion zum anzeigen ausstehender Regestrierungs Anfragen
async function viewRegistrationRequests() {
    try {
        console.log('📋 Lade ausstehende Registrierungsanfragen...');
        hideDataDisplay();
        const response = await apiCall('/api/admin/registration-requests/pending'); // Neuer Endpunkt

        if (response && Array.isArray(response)) {
            if (response.length === 0) {
                displayData('Ausstehende Registrierungsanfragen', '<p>Keine ausstehenden Registrierungsanfragen gefunden.</p>');
            } else {
                displayData('Ausstehende Registrierungsanfragen', formatRegistrationRequestsTable(response));
            }
        } else {
            displayData('Ausstehende Registrierungsanfragen', '<p>Keine Anfragen gefunden oder ungültige Antwort.</p>');
        }
    } catch (error) {
        showError('Fehler beim Laden der Registrierungsanfragen: ' + (error.message || "Unbekannt"));
    }
}

// Funktion zur Formatierung der Tabelle für Registrierungsanfragen
function formatRegistrationRequestsTable(requests) {
    if (!requests || requests.length === 0) {
        return '<p>Keine Registrierungsanfragen gefunden.</p>';
    }

    let tableHtml = `<table class="data-table">
                     <thead>
                       <tr>
                         <th>ID</th>
                         <th>Name</th>
                         <th>E-Mail</th>
                         <th>Angeforderte Rolle</th>
                         <th>Manager</th>
                         <th>Eingereicht am</th>
                         <th>Aktionen</th>
                       </tr>
                     </thead>
                     <tbody>`;

    requests.forEach(request => {
        tableHtml += `<tr>
                      <td>${request.id}</td>
                      <td>${request.firstName} ${request.lastName}</td>
                      <td>${request.email}</td>
                      <td>${request.requestedRole}</td>
                      <td>${request.managerName || '-'}</td>
                      <td>${formatDateTimeDisplay(request.createdAt)}</td>
                      <td class="actions-cell">
                          <button class="btn btn-success btn-small" onclick="approveRegistrationRequest(${request.id})">Genehmigen</button>
                          <button class="btn btn-danger btn-small" onclick="rejectRegistrationRequest(${request.id})" style="margin-left:5px;">Ablehnen</button>
                      </td>
                    </tr>`;
    });
    tableHtml += '</tbody></table>';
    return tableHtml;
}

// Funktion zum Genehmigen einer Registrierungsanfrage
async function approveRegistrationRequest(requestId) {
    if (confirm('Möchten Sie diese Registrierungsanfrage genehmigen und einen Benutzer erstellen?')) {
        try {
            await apiCall(`/api/admin/registration-requests/${requestId}/approve`, { method: 'PATCH' });
            showSuccess('Registrierungsanfrage erfolgreich genehmigt und Benutzer erstellt.');
            viewRegistrationRequests(); // Liste aktualisieren
            loadDashboardPageData(true); // Dashboard-Statistiken aktualisieren
        } catch (error) {
            showError('Fehler beim Genehmigen der Anfrage: ' + (error.message || "Unbekannt"));
        }
    }
}

// Funktion zum Ablehnen einer Registrierungsanfrage
async function rejectRegistrationRequest(requestId) {
    if (confirm('Möchten Sie diese Registrierungsanfrage ablehnen?')) {
        try {
            await apiCall(`/api/admin/registration-requests/${requestId}/reject`, { method: 'PATCH' });
            showSuccess('Registrierungsanfrage erfolgreich abgelehnt.');
            viewRegistrationRequests(); // Liste aktualisieren
            loadDashboardPageData(true); // Dashboard-Statistiken aktualisieren
        } catch (error) {
            showError('Fehler beim Ablehnen der Anfrage: ' + (error.message || "Unbekannt"));
        }
    }
}

function debugToken() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        showError('Kein JWT Token gefunden'); return;
    }
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const tokenInfo = {
            user: payload.sub || payload.email,
            roles: payload.roles || payload.authorities,
            issued: new Date(payload.iat * 1000),
            expires: new Date(payload.exp * 1000),
            isExpired: new Date() > new Date(payload.exp * 1000),
            currentTime: new Date()
        };
        console.log('🔍 Token Info:', tokenInfo);
        if (tokenInfo.isExpired) {
            showError('❌ Token ist abgelaufen! Bitte melden Sie sich neu an.');
        } else {
            const remaining = Math.floor((tokenInfo.expires - tokenInfo.currentTime) / (1000 * 60));
            showSuccess(`✅ Token ist gültig. Läuft ab in ${remaining} Minuten.`);
        }
        return tokenInfo;
    } catch (e) {
        showError('Token ist ungültig oder kann nicht dekodiert werden.');
    }
}

async function checkSystemStatus() {
    try {
        console.log('🔍 Prüfe System-Status...');
        await apiCall('/api/time-entries');
        console.log('✅ API Verbindung OK');
        debugToken();
        showSuccess('✅ System-Status: Alles scheint zu funktionieren.');
    } catch (error) {
        showError('❌ System-Problem: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    }
}

async function openCreateUserModal() {
    const createUserForm = document.getElementById('createUserForm');
    if (createUserForm) createUserForm.reset();
    const feedbackDiv = document.getElementById('createUserFeedback');
    const errorDiv = document.getElementById('createUserError');
    if (feedbackDiv) feedbackDiv.style.display = 'none';
    if (errorDiv) errorDiv.style.display = 'none';

    const newUserRoleSelect = document.getElementById('newUserRole');
    const newUserParentManagerSelect = document.getElementById('newUserParentManager'); // Neuer Manager Select

    try {
        // Rollen laden
        const allRolesResponse = await apiCall('/api/admin/roles');
        if (newUserRoleSelect) {
            newUserRoleSelect.innerHTML = '';
            if (allRolesResponse && Array.isArray(allRolesResponse)) {
                allRolesResponse.forEach(role => {
                    const option = document.createElement('option');
                    option.value = role.name;
                    option.textContent = String(role.name).replace('ROLE_', '');
                    newUserRoleSelect.appendChild(option);
                });
            }
        }

        // Manager laden (Benutzer mit Rolle MANAGER oder ADMIN)
        const allUsersResponse = await apiCall('/api/admin/users');
        if (newUserParentManagerSelect) {
            newUserParentManagerSelect.innerHTML = '<option value="">Kein direkter Manager</option>'; // Standardauswahl
            if (allUsersResponse && Array.isArray(allUsersResponse)) {
                allUsersResponse.forEach(user => {
                    if (user.roles && (user.roles.includes('MANAGER') || user.roles.includes('ADMIN'))) {
                        const option = document.createElement('option');
                        option.value = user.id;
                        option.textContent = `${user.firstName} ${user.lastName} (${user.email})`;
                        newUserParentManagerSelect.appendChild(option);
                    }
                });
            }
        }

    } catch (error) {
        const msg = 'Fehler beim Laden der Initialdaten für neuen Benutzer: ' + (error.message || "Unbekannt");
        if (errorDiv) {
            errorDiv.textContent = msg;
            errorDiv.style.display = 'block';
        } else {
            showError(msg);
        }
    }
    openModal('createUserModal');
}

async function showUserDetails(userId) {
    console.log('showUserDetails aufgerufen für User ID:', userId);
    try {
        const user = await apiCall(`/api/admin/users/${userId}`);
        console.log('User-Daten von API erhalten:', user);
        if (!user) {
            showError('Benutzerdetails nicht gefunden.');
            return;
        }
        window.selectedUserForDetails = user;
        console.log('window.selectedUserForDetails gesetzt auf:', window.selectedUserForDetails);

        document.getElementById('detailUserName').textContent = `${user.firstName} ${user.lastName}`;
        document.getElementById('detailUserEmail').textContent = user.email;
        document.getElementById('detailUserStatus').textContent = user.status ? String(user.status) : (user.active ? 'Aktiv' : 'Inaktiv');
        document.getElementById('detailUserPlannedHours').textContent = user.plannedHoursPerDay ? user.plannedHoursPerDay.toFixed(1) : 'N/A';
        document.getElementById('detailUserCreatedAt').textContent = formatDateTimeDisplay(user.createdAt);
        document.getElementById('detailUserUpdatedAt').textContent = formatDateTimeDisplay(user.updatedAt);

        const userRolesListDiv = document.getElementById('userRolesList');
        if (userRolesListDiv) {
            if (user.roles && user.roles.length > 0) {
                userRolesListDiv.innerHTML = user.roles.map(role =>
                    `<span class="user-role-tag">
                        ${String(role).replace('ROLE_', '')}
                        <button class="remove-role-btn" data-role="${role}" title="Rolle '${String(role).replace('ROLE_', '')}' entfernen" onclick="handleRemoveRole('${String(role)}')">&times;</button>
                    </span>`
                ).join(' ');
            } else {
                userRolesListDiv.innerHTML = 'Keine Rollen zugewiesen.';
            }
        }

        await loadRolesForAddDropdown(user.roles || []);

        const userStatusSelect = document.getElementById('userStatusSelect');
        if (userStatusSelect) {
            const statusValue = user.status ? String(user.status).toUpperCase() : (user.active ? 'ACTIVE' : 'INACTIVE');
            if (Array.from(userStatusSelect.options).some(opt => opt.value === statusValue)) {
                userStatusSelect.value = statusValue;
            } else {
                console.warn(`Status "${statusValue}" nicht in Select-Optionen gefunden. Fallback auf ersten Wert.`);
                userStatusSelect.selectedIndex = 0;
            }
        }

        openModal('userDetailModal');
        console.log('userDetailModal geöffnet');
    } catch (error) {
        console.error('Fehler in showUserDetails:', error);
        showError('Fehler beim Laden der Benutzerdetails: ' + (error.message || "Unbekannt"));
    }
}

async function handleRemoveRole(roleName) {
    if (!window.selectedUserForDetails || !window.selectedUserForDetails.id) {
        showError('Kein Benutzer ausgewählt oder Benutzer-ID fehlt.');
        return;
    }
    const user = window.selectedUserForDetails;
    if (confirm(`Möchten Sie die Rolle '${String(roleName).replace('ROLE_', '')}' von ${user.firstName} ${user.lastName} wirklich entfernen?`)) {
        try {
            await apiCall(`/api/admin/users/${user.id}/roles?roleName=${roleName}`, { method: 'DELETE' });
            showSuccess(`Rolle '${String(roleName).replace('ROLE_', '')}' erfolgreich von ${user.firstName} ${user.lastName} entfernt.`);
            await showUserDetails(user.id);
            if (document.getElementById('dataDisplay')?.style.display === 'block' && document.getElementById('dataTitle')?.textContent === 'Alle Benutzer') {
                viewUsers();
            }
        } catch (error) {
            showError('Fehler beim Entfernen der Rolle: ' + (error.message || "Unbekannt"));
        }
    }
}

async function loadRolesForAddDropdown(currentUserRoles) {
    try {
        const allRolesResponse = await apiCall('/api/admin/roles');
        const addRoleSelect = document.getElementById('addRoleSelect');
        if (!addRoleSelect) return;
        addRoleSelect.innerHTML = '<option value="">Rolle auswählen</option>';
        const currentRoleNamesSet = new Set((currentUserRoles || []).map(r => String(r)));

        if (allRolesResponse && Array.isArray(allRolesResponse)) {
            allRolesResponse.forEach(roleObj => {
                if (!currentRoleNamesSet.has(roleObj.name)) {
                    const option = document.createElement('option');
                    option.value = roleObj.name;
                    option.textContent = String(roleObj.name).replace('ROLE_', '');
                    addRoleSelect.appendChild(option);
                }
            });
        }
    } catch (error) {
        showError('Fehler beim Laden der Rollen für Dropdown: ' + (error.message || "Unbekannt"));
    }
}
async function viewSystemLogs() {
    try {
        hideDataDisplay();
        const response = await apiCall('/api/admin/logs');

        if (response && Array.isArray(response.logs)) {
            let html = '<h2>System-Logs</h2>';
            if (response.logs.length === 0) {
                html += '<p>Keine System-Logs gefunden.</p>';
            } else {
                // Status Spalte hinzugefügt für bessere Übersicht
                html += '<table class="data-table"><thead><tr><th>Timestamp</th><th>User</th><th>Action</th><th>Details</th><th>Status</th></tr></thead><tbody>';
                response.logs.forEach(logEntry => { // Hier logEntry statt log, um Verwechslung zu vermeiden, aber log ist auch ok
                    html += `<tr>
                                <td>${logEntry.timestamp ? formatDateTimeDisplay(logEntry.timestamp) : '-'}</td>
                                <td>${logEntry.userEmail || (logEntry.userId ? `ID: ${logEntry.userId}` : 'System')}</td>
                                <td>${logEntry.action || '-'}</td>
                                <td style="word-break: break-all;">${logEntry.details || '-'}</td>
                                <td>${logEntry.processedStatus || '-'}</td> 
                             </tr>`;
                });
                html += '</tbody></table>';
            }
            displayData('System-Logs', html);
        } else {
            displayData('System-Logs', '<p>System-Logs konnten nicht geladen werden oder haben ein unerwartetes Format.</p>');
        }
    } catch (error) {
        showError('Fehler beim Laden der System-Logs: ' + (error.message || "Unbekannt"));
    }
}

async function viewPasswordResetRequests() {
    try {
        hideDataDisplay();
        const response = await apiCall('/api/admin/logs');

        if (response && Array.isArray(response.logs)) {

            const resetRequests = response.logs.filter(log =>
                log.action && log.action.toLowerCase() === "passwort reset angefordert" &&
                (!log.processedStatus || log.processedStatus.toUpperCase() === "PENDING")
            );

            let html = '<h2>Ausstehende Passwort-Reset-Anfragen</h2>';
            if (resetRequests.length === 0) {
                html += '<p>Keine offenen Passwort-Reset-Anfragen gefunden.</p>';
            } else {
                html += '<ul id="passwordResetList" style="list-style: none; padding: 0;">';
                resetRequests.forEach(req => {
                    const userId = req.userId || 'N/A';
                    const userEmail = req.userEmail || 'E-Mail nicht im Log';
                    const escapedUserEmail = userEmail.replace(/'/g, "\\'");
                    const logEntryId = req.id;

                    html += `<li id="reset-request-${logEntryId}" style="padding: 10px; border-bottom: 1px solid #eee; margin-bottom: 5px; background-color: #f9f9f9; border-radius: 4px;">
                                <strong>E-Mail:</strong> ${userEmail}<br>
                                <strong>User ID:</strong> ${userId}<br>
                                <strong>Angefordert am:</strong> ${req.timestamp ? formatDateTimeDisplay(req.timestamp) : '-'}<br> 
                                <button class="btn btn-warning btn-small reset-user-password-btn" 
                                        onclick="handleResetPasswordFromAdminView(${userId}, '${escapedUserEmail}', ${logEntryId})" 
                                        ${userId === 'N/A' ? 'disabled title="User ID nicht verfügbar"' : ''}
                                        style="margin-top: 5px;">
                                    Passwort für User zurücksetzen
                                </button>
                             </li>`;
                });
                html += '</ul>';
            }
            displayData('Passwort-Reset-Anfragen', html);
        } else {
            showWarning("System-Logs konnten nicht geladen werden. Passwort-Reset-Anfragen können nicht angezeigt werden.");
        }
    } catch (error) {
        // Der Fehler "log is not defined" wird hier abgefangen.
        console.error("Fehlerdetails in viewPasswordResetRequests:", error); // Zusätzliches Logging des Fehlers selbst
        showError('Fehler beim Laden der Passwort-Reset-Anfragen: ' + (error.message || "Unbekannt"));
    }
}

// Die Funktion handleResetPasswordFromAdminView (mit dem erweiterten Logging für den confirm-Dialog)
async function handleResetPasswordFromAdminView(userId, userEmail, logId) {
    console.log(`[DEBUG Admin.js] handleResetPasswordFromAdminView: Start.`);
    console.log(`[DEBUG Admin.js] Übergebene userId: ${userId} (Typ: ${typeof userId}), userEmail: ${userEmail}, logId: ${logId}`); // Wichtig!

    const numUserId = parseInt(userId, 10);
    if (isNaN(numUserId)) {
        showError("Ungültige Benutzer-ID für Reset.");
        console.error(`[DEBUG Admin.js] handleResetPasswordFromAdminView: Invalid userId. Konnte nicht zu Zahl geparst werden. Value: ${userId}`);
        return;
    }
    console.log(`[DEBUG Admin.js] Parsed numUserId: ${numUserId} (Typ: ${typeof numUserId})`); // Wichtig!

    const confirmMsg = `Möchten Sie das Passwort für ${userEmail} (ID: ${numUserId}) wirklich zurücksetzen? Der Benutzer wird darüber nicht automatisch benachrichtigt.`;
    //
    let userConfirmed;
    try {
        userConfirmed = confirm(confirmMsg);
        console.log(`[DEBUG] confirm() Dialog hat zurückgegeben: ${userConfirmed} (Typ: ${typeof userConfirmed})`);
    } catch (e) {
        console.error("[DEBUG] Fehler während confirm() Dialog:", e);
        showError("Fehler bei der Bestätigungsabfrage.");
        return;
    }

    if (userConfirmed === true) {
        console.log(`[DEBUG] User ID ${numUserId}: Passwort-Reset bestätigt. Starte API-Call.`);
        try {
            const response = await apiCall(`/api/admin/users/${numUserId}/reset-password`, { method: 'POST' });
            console.log(`[DEBUG] API-Call /api/admin/users/${numUserId}/reset-password - Antwort:`, response);

            if (response && response.temporaryPassword) {
                showSuccess(`Passwort für ${userEmail || `ID ${numUserId}`} erfolgreich zurückgesetzt. Temporäres Passwort: <strong>${response.temporaryPassword}</strong>. Bitte teilen Sie dieses Passwort dem Benutzer sicher mit.`);
                console.log(`[DEBUG] Passwort erfolgreich zurückgesetzt für User ID ${numUserId}. Temporäres Passwort: ${response.temporaryPassword}`);
                viewPasswordResetRequests();
            } else {
                showError('Passwort-Reset durchgeführt, aber kein temporäres Passwort erhalten oder Antwort war unerwartet.');
                console.error(`[DEBUG] Kein temporäres Passwort in der Antwort oder unerwartete Antwort für User ID ${numUserId}:`, response);
            }
        } catch (error) {
            showError('Fehler beim Zurücksetzen des Passworts via API: ' + (error.message || "Unbekannt"));
            console.error(`[DEBUG] Fehler beim API-Call für Passwort-Reset für User ID ${numUserId}:`, error);
        }
    } else {
        console.log(`[DEBUG] User ID ${numUserId}: Passwort-Reset durch Benutzer abgebrochen (confirm_result: ${userConfirmed}).`);
    }
    console.log(`[DEBUG] handleResetPasswordFromAdminView: Ende. UserID: ${numUserId}`);
}



function initializeAdminFeatures() {
    const userEmail = localStorage.getItem('userEmail');
    const userRolesString = localStorage.getItem('userRoles');
    let userRoles = [];
    try {
        if (userRolesString) userRoles = JSON.parse(userRolesString);
    } catch (e) { console.error('Fehler beim Parsen der Rollen:', e); userRoles = []; }

    let isAdmin = false;
    let isManager = false;
    if (Array.isArray(userRoles)) {
        isAdmin = userRoles.some(role => String(role).toUpperCase() === 'ADMIN' || String(role).toUpperCase() === 'ROLE_ADMIN');
        isManager = userRoles.some(role => String(role).toUpperCase().includes('MANAGER'));
    }

    const adminCard = document.getElementById('adminCard');
    const createProjectBtn = document.getElementById('createProjectBtn');
    const viewPendingAbsencesBtn = document.getElementById('viewPendingAbsencesBtn');
    const viewTeamOrAllApprovedAbsencesBtn = document.getElementById('viewTeamOrAllApprovedAbsencesBtn');
    const viewEmployeeTimeEntriesBtn = document.getElementById('viewEmployeeTimeEntriesBtn');

    if (isAdmin) {
        if (adminCard) {
            adminCard.style.display = 'block';
            const adminActionButtons = adminCard.querySelector('.action-buttons');
            if (adminActionButtons && !document.getElementById('viewPasswordResetRequestsBtn')) {
                const resetButton = document.createElement('button');
                resetButton.className = 'btn btn-info';
                resetButton.id = 'viewPasswordResetRequestsBtn';
                resetButton.textContent = 'Passwort-Resets';
                resetButton.style.marginTop = '0.5rem';
                resetButton.style.marginLeft = '0.5rem'; // Für besseres Layout
                adminActionButtons.appendChild(resetButton);
                // Der Event-Listener wird in dashboard.js hinzugefügt
            }
        }
        if (createProjectBtn) createProjectBtn.style.display = 'inline-block';
    } else {
        if (adminCard) adminCard.style.display = 'none';
        if (createProjectBtn) createProjectBtn.style.display = 'none';
    }

    if (isAdmin || isManager) {
        if (viewPendingAbsencesBtn) viewPendingAbsencesBtn.style.display = 'inline-block';
        if (viewTeamOrAllApprovedAbsencesBtn) viewTeamOrAllApprovedAbsencesBtn.style.display = 'inline-block';
        if (viewEmployeeTimeEntriesBtn) viewEmployeeTimeEntriesBtn.style.display = 'inline-block';
    } else {
        if (viewPendingAbsencesBtn) viewPendingAbsencesBtn.style.display = 'none';
        if (viewTeamOrAllApprovedAbsencesBtn) viewTeamOrAllApprovedAbsencesBtn.style.display = 'none';
        if (viewEmployeeTimeEntriesBtn) viewEmployeeTimeEntriesBtn.style.display = 'none';
    }

    if (typeof DEBUG_MODE !== 'undefined' && DEBUG_MODE) {
        console.log('👤 User Info (Admin Check):', { email: userEmail, roles: userRoles, isAdmin: isAdmin, isManager: isManager });
    }
}

async function handleCreateUserSubmit(event) {
    event.preventDefault();
    hideAllMessages();
    const firstName = document.getElementById('newFirstName').value;
    const lastName = document.getElementById('newLastName').value;
    const email = document.getElementById('newEmail').value;
    const role = document.getElementById('newUserRole').value;
    const plannedHours = parseFloat(document.getElementById('newPlannedHours').value);
    const managerId = document.getElementById('newUserParentManager').value; // Manager ID auslesen

    const feedbackDiv = document.getElementById('createUserFeedback');
    const errorDiv = document.getElementById('createUserError');
    if(feedbackDiv) feedbackDiv.style.display = 'none';
    if(errorDiv) errorDiv.style.display = 'none';

    const submitBtn = document.getElementById('createUserSubmitBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Erstelle Benutzer...';
    }

    try {
        const userData = {
            firstName: firstName,
            lastName: lastName,
            email: email,
            role: role,
            plannedHoursPerDay: plannedHours,
            managerId: managerId ? parseInt(managerId) : null // managerId zum Payload hinzufügen
        };
        const response = await apiCall('/api/admin/users', { method: 'POST', body: userData });

        if (response && response.id) {
            if(feedbackDiv) {
                feedbackDiv.innerHTML = `✅ Benutzer "${response.firstName} ${response.lastName}" erfolgreich erstellt! Initialpasswort: <strong>${response.temporaryPassword || lastName.toLowerCase()}</strong>. Bitte teilen Sie dies dem Benutzer mit.`;
                feedbackDiv.style.display = 'block';
            }
            document.getElementById('createUserForm').reset();
            setTimeout(() => {
                closeModal('createUserModal');
                viewUsers();
                if (typeof loadDashboardPageData === 'function') loadDashboardPageData(true);
            }, 7000);
        } else {
            if(errorDiv) {
                errorDiv.textContent = '❌ Fehler: Benutzer konnte nicht erstellt werden. Unerwartete Antwort.';
                errorDiv.style.display = 'block';
            }
        }
    } catch (error) {
        if(errorDiv) {
            errorDiv.textContent = '❌ Fehler beim Erstellen des Benutzers: ' + (error.message || "Unbekannt");
            errorDiv.style.display = 'block';
        }
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Benutzer anlegen';
        }
    }
}