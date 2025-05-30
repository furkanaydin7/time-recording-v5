// * @author EK

function closeModal(modalId) {
    console.log('Versuche Modal zu schließen:', modalId);

    const modal = document.getElementById(modalId);
    if (!modal) {
        console.warn('Modal nicht gefunden:', modalId);
        return;
    }

    modal.style.display = 'none';
    console.log('Modal geschlossen:', modalId);

    // Formulare zurücksetzen
    const forms = modal.querySelectorAll('form');
    forms.forEach(form => {
        if (modalId !== 'projectDetailModal' && form.id.includes('edit')) {
            if (confirm('Änderungen speichern?')) {
                form.reset();
            }
        } else {
            form.reset();
        }
    });

    hideAllMessages();
}

function openModal(modalId) {
    console.log('Öffne Modal:', modalId); // Debug
    const modal = document.getElementById(modalId);
    if (modal) {
        // Alle anderen Modals schließen
        document.querySelectorAll('.modal').forEach(m => {
            if (m.id !== modalId) {
                m.style.display = 'none';
            }
        });

        modal.style.display = 'block';

        // Fokus auf erstes Input-Element setzen
        const firstInput = modal.querySelector('input, select, textarea');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    } else {
        console.warn('Modal nicht gefunden:', modalId);
    }
}

function setupModalOutsideClickHandlers() {
    window.addEventListener('click', function(event) {
        // Prüfen, ob auf das Modal-Overlay geklickt wurde (nicht auf den Inhalt)
        if (event.target.classList.contains('modal')) {
            const modalId = event.target.id;
            if (modalId) {
                closeModal(modalId);
            }
        }
    });
}

function setupModalEscapeHandler() {
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            // Finde das aktuell geöffnete Modal
            const openModal = document.querySelector('.modal[style*="display: block"], .modal[style*="display:block"]');
            if (openModal) {
                closeModal(openModal.id);
            }

            // Auch dataDisplay schließen
            const dataDisplay = document.getElementById('dataDisplay');
            if (dataDisplay && dataDisplay.style.display === 'block') {
                hideDataDisplay();
            }
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    setupModalOutsideClickHandlers();
    setupModalEscapeHandler();
});

function displayData(title, content) {
    document.getElementById('dataTitle').textContent = title;
    document.getElementById('dataContent').innerHTML = content;
    document.getElementById('dataDisplay').style.display = 'block';
    document.getElementById('dataDisplay').scrollIntoView({ behavior: 'smooth' });
}

function hideDataDisplay() {
    document.getElementById('dataDisplay').style.display = 'none';
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function formatDateTimeDisplay(dateTimeString) {
    if (!dateTimeString) return '-';
    const date = new Date(dateTimeString);
    const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
    return date.toLocaleDateString('de-DE', options);
}

function hideAllMessages() {
    const errorMessageDiv = document.getElementById('errorMessage');
    const successMessageDiv = document.getElementById('successMessage');
    const warningMessageDiv = document.getElementById('warningMessage');

    if (errorMessageDiv) errorMessageDiv.style.display = 'none';
    if (successMessageDiv) successMessageDiv.style.display = 'none';
    if (warningMessageDiv) warningMessageDiv.style.display = 'none';
}

function showError(message) {
    hideAllMessages();
    const errorDiv = document.getElementById('errorMessage');
    if (!errorDiv) {
        console.error("Error-Div not found. Message:", message);
        alert("Fehler: " + message); // Fallback
        return;
    }

    if (message.includes('\n') || message.includes('•')) {
        errorDiv.innerHTML = `❌ ${message.replace(/\n/g, '<br>').replace(/•/g, '&bull;')}`;
    } else {
        errorDiv.textContent = `❌ ${message}`;
    }
    errorDiv.style.display = 'block';
    const timeoutDuration = message.length > 100 ? 12000 : 8000;
    setTimeout(() => {
        if (errorDiv) errorDiv.style.display = 'none';
    }, timeoutDuration);
}

function showSuccess(message) {
    hideAllMessages();
    const successDiv = document.getElementById('successMessage');
    if (!successDiv) {
        console.log("Success-Div not found. Message:", message);
        alert("Erfolg: " + message); // Fallback
        return;
    }
    successDiv.textContent = `✅ ${message}`;
    successDiv.style.display = 'block';
    setTimeout(() => {
        if (successDiv) successDiv.style.display = 'none';
    }, 10000);
}

function showWarning(message) {
    hideAllMessages();
    const warningDiv = document.getElementById('warningMessage');
    if (!warningDiv) {
        console.warn("Warning-Div not found. Message:", message);
        alert("Warnung: " + message); // Fallback
        return;
    }
    if (message.includes('\n') || message.includes('•')) {
        warningDiv.innerHTML = `⚠️ ${message.replace(/\n/g, '<br>').replace(/•/g, '&bull;')}`;
    } else {
        warningDiv.textContent = `⚠️ ${message}`;
    }
    warningDiv.style.display = 'block';
    setTimeout(() => {
        if (warningDiv) warningDiv.style.display = 'none';
    }, 10000);
}


function formatTimeEntriesTable(entries) {
    if (!entries || entries.length === 0) {
        return '<p>Keine Zeiteinträge gefunden.</p>';
    }
    // Ensure sorting happens correctly, especially if entries come from different sources
    entries.sort((a, b) => {
        const dateComparison = new Date(b.date) - new Date(a.date);
        if (dateComparison !== 0) return dateComparison;
        // If dates are the same, sort by user name if available
        if (a.user && b.user) return String(a.user).localeCompare(String(b.user)); //
        return 0;
    });

    let tableHtml = `<table class="data-table">
                     <thead>
                       <tr>
                         <th>Datum</th>
                         <th>Benutzer</th>
                         <th>Projekt</th>
                         <th>Start</th>
                         <th>Ende</th>
                         <th>Pause(n)</th>
                         <th>Effektiv</th>
                         <th>Geplant</th>
                         <th>Differenz</th>
                         <th>Aktionen</th>
                       </tr>
                     </thead>
                     <tbody>`;

    const currentUserRolesString = localStorage.getItem('userRoles');
    let currentUserRoles = [];
    try {
        if (currentUserRolesString) currentUserRoles = JSON.parse(currentUserRolesString);
    } catch(e) { console.error("Error parsing roles for table formatting", e); }

    const currentUserId = parseInt(localStorage.getItem('userId')); // Make sure userId is stored and retrieved correctly
    const isAdmin = currentUserRoles.some(role => String(role).toUpperCase() === 'ADMIN' || String(role).toUpperCase() === 'ROLE_ADMIN');


    entries.forEach(entry => {
        const startTimesDisplay = entry.startTimes && entry.startTimes.length > 0
            ? entry.startTimes.map(t => t ? String(t).substring(0,5) : '-').join(', ') //
            : '-';
        const endTimesDisplay = entry.endTimes && entry.endTimes.length > 0
            ? entry.endTimes.map(t => t ? String(t).substring(0,5) : '(läuft)').join(', ') //
            : (entry.startTimes && entry.startTimes.length > 0 ? '(läuft)' : '-'); //
        const breaksFormatted = entry.breaks && entry.breaks.length > 0
            ? entry.breaks.map(b => `${b.start ? String(b.start).substring(0,5) : '??'}-${b.end ? String(b.end).substring(0,5) : '??'}`).join('<br>') //
            : '-';
        const entryJsonString = JSON.stringify(entry).replace(/'/g, "&apos;").replace(/"/g, "&quot;"); //

        // START: Logik für Bearbeiten-Button
        const isOwner = entry.userId === currentUserId; // Prüft, ob der aktuelle Benutzer der Eigentümer des Eintrags ist
        let actionsHtml = '';

        // Bearbeiten-Button: Sichtbar für Eigentümer oder Admin
        if (isOwner || isAdmin) {
            actionsHtml += `<button class="btn btn-secondary btn-small" onclick='openEditTimeEntryModal(${entryJsonString})'>Bearbeiten</button>`; //
        }
        // Löschen-Button: Sichtbar für Eigentümer oder Admin
        if (isOwner || isAdmin) {
            actionsHtml += `<button class="btn btn-danger btn-small" onclick='deleteTimeEntryHandler(${entry.id})' style="margin-left:5px;">Löschen</button>`; //
        }
        if (actionsHtml === '') {
            actionsHtml = '-'; // Zeigt einen Bindestrich an, wenn keine Aktionen verfügbar sind
        }
        // ENDE: Logik für Bearbeiten-Button


        // Die Spalte 'Benutzer' wird hier gefüllt. Das 'entry.user' Feld sollte den Namen des Benutzers enthalten.
        tableHtml += `<tr>
                      <td>${formatDate(entry.date)}</td> 
                      <td>${entry.user || 'N/A'}</td> 
                      <td>${entry.project ? entry.project.name : 'Kein Projekt'}</td>
                      <td>${startTimesDisplay}</td>
                      <td>${endTimesDisplay}</td>
                      <td>${breaksFormatted}</td>
                      <td>${entry.actualHours || '-'}</td>
                      <td>${entry.plannedHours || '-'}</td>
                      <td>${entry.difference || '-'}</td>
                      <td class="actions-cell">${actionsHtml}</td>
                    </tr>`;
    });
    tableHtml += '</tbody></table>';
    return tableHtml;
}

function formatProjectsTable(projects) {
    if (!projects || projects.length === 0) {
        return '<p>Keine aktiven Projekte gefunden.</p>';
    }
    let html = '<table class="data-table"><thead><tr><th>Name</th><th>Beschreibung</th><th>Manager</th><th>Status</th></tr></thead><tbody>';
    projects.forEach(project => {
        html += `<tr onclick="showProjectDetails(${project.id})" style="cursor:pointer;">
                  <td><strong>${project.name}</strong></td>
                  <td>${project.description || '-'}</td>
                  <td>${project.managerName || '-'}</td>
                  <td><span style="color: ${project.active ? '#28a745' : '#dc3545'}">${project.active ? 'Aktiv' : 'Inaktiv'}</span></td>
              </tr>`;
    });
    html += '</tbody></table>';
    return html;
}

function formatAbsencesTable(absences) {
    if (!absences || absences.length === 0) {
        return '<p>Keine Abwesenheiten gefunden.</p>';
    }

    // Sortieren, sodass die neuesten Anträge oben sind
    absences.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    let html = '<table class="data-table">' +
        '<thead>' +
        '<tr>' +
        '<th>Typ</th>' +
        '<th>Von</th>' +
        '<th>Bis</th>' +
        '<th>Tage</th>' +
        '<th>Status</th>' +
        '<th>Genehmiger</th>' +
        '<th>Antragsteller</th>' +
        '<th>Aktionen</th>'+
        '</tr>' +
        '</thead>' +
        '<tbody>';
    absences.forEach(absence => {
        const typeLabels = {
            'VACATION': 'Urlaub',
            'ILLNESS': 'Krankheit',
            'HOME_OFFICE': 'Home Office',
            'TRAINING': 'Fortbildung',
            'PUBLIC_HOLIDAY': 'Feiertag',
            'UNPAID_LEAVE': 'Unbezahlter Urlaub',
            'SPECIAL_LEAVE': 'Sonderurlaub',
            'OTHER': 'Sonstiges'
        };

        const dayCount = calculateDaysBetween(absence.startDate, absence.endDate);

        let statusText = 'Unbekannt';
        let statusColor = '#666'; // Standardfarbe für unbekannt

        if (absence.status === 'APPROVED') {
            statusText = 'Genehmigt';
            statusColor = '#28a745'; // Grün
        } else if (absence.status === 'REJECTED') {
            statusText = 'Abgelehnt';
            statusColor = '#dc3545'; // Rot
        } else if (absence.status === 'PENDING') {
            statusText = 'Ausstehend';
            statusColor = '#ffc107'; // Gelb
        }

        let processedByDisplay = '-'; // Standardwert
        if (absence && absence.processedByName) { // Prüfen, ob 'processedByName' existiert und einen Wert hat
            processedByDisplay = absence.processedByName;
        }

        // JSON-String des Abwesenheitsobjekts für die Übergabe an die Bearbeitungsfunktion
        // Sonderzeichen im JSON-String für HTML-Attribute escapen
        const absenceJsonString = JSON.stringify(absence)
            .replace(/'/g, "&apos;")
            .replace(/"/g, "&quot;");

        let actionsHtml = '';
        // Buttons nur anzeigen, wenn der Status PENDING ist
        if (absence.status === 'PENDING') {
            actionsHtml = `
                <button class="btn btn-secondary btn-small" onclick='openEditAbsenceModal(${absenceJsonString})'>Bearbeiten</button>
                <button class="btn btn-danger btn-small" onclick='cancelAbsenceHandler(${absence.id})' style="margin-left:5px;">Stornieren</button>
            `;
        } else {
            actionsHtml = '-';
        }

        html += `<tr>
                    <td>${typeLabels[absence.type] || absence.type}</td>
                    <td>${formatDate(absence.startDate)}</td>
                    <td>${formatDate(absence.endDate)}</td>
                    <td>${dayCount}</td>
                    <td><span style="color: ${statusColor}">${statusText}</span></td>
                    <td>${processedByDisplay}</td>
                    <td>${absence.firstName || ''} ${absence.lastName || ''}</td>
                    <td class="actions-cell">${actionsHtml}</td>
                </tr>`;
    });
    html += '</tbody></table>';
    return html;
}

function formatPendingAbsencesTableForApproval(absences) {
    if (!absences || absences.length === 0) {
        return '<p>Keine ausstehenden Abwesenheitsanträge gefunden.</p>';
    }
    absences.sort((a, b) => new Date(a.startDate) - new Date(b.startDate));

    // Aktuelle Benutzerrollen für die bedingte Anzeige von Buttons abrufen
    const userRolesString = localStorage.getItem('userRoles');
    let currentUserRoles = [];// Älteste zuerst
    try {
        if (userRolesString) currentUserRoles = JSON.parse(userRolesString); //
    } catch (e) {
        console.error('Fehler beim Parsen der Rollen für Tabellenformatierung (Pending):', e);
    }
    const isAdmin = currentUserRoles.some(role => String(role).toUpperCase() === 'ADMIN');

    let html = `<table class="data-table">
                  <thead>
                    <tr>
                      <th>Antragsteller</th>
                      <th>Typ</th>
                      <th>Von</th>
                      <th>Bis</th>
                      <th>Tage</th>
                      <th>Status</th>
                      <th>Aktionen</th>
                    </tr>
                  </thead>
                  <tbody>`;

    absences.forEach(absence => {
        const typeLabels = {
            'VACATION': 'Urlaub', 'ILLNESS': 'Krankheit', 'HOME_OFFICE': 'Home Office',
            'TRAINING': 'Fortbildung', 'PUBLIC_HOLIDAY': 'Feiertag',
            'UNPAID_LEAVE': 'Unbezahlter Urlaub', 'SPECIAL_LEAVE': 'Sonderurlaub', 'OTHER': 'Sonstiges'
        };
        const dayCount = calculateDaysBetween(absence.startDate, absence.endDate);
        const applicantName = `${absence.firstName || ''} ${absence.lastName || ''}`.trim() || absence.email || 'Unbekannt';

        // Für diese Tabelle ist der Status immer "Ausstehend"
        const statusText = 'Ausstehend';
        const statusColor = '#ffc107';

        // JSON-String des Abwesenheitsobjekts für die Übergabe an die Bearbeitungsfunktion
        const absenceJsonString = JSON.stringify(absence)
            .replace(/'/g, "&apos;")
            .replace(/"/g, "&quot;");

        let actionsCellHtml = '';

        // Genehmigen und Ablehnen Buttons sind immer da für die Benutzer (Admins/Manager), die diese Ansicht sehen
        actionsCellHtml += `<button class="btn btn-success btn-small" onclick="approveAbsenceRequest(${absence.id})">Genehmigen</button>`;
        actionsCellHtml += `<button class="btn btn-danger btn-small" onclick="rejectAbsenceRequest(${absence.id})" style="margin-left:5px;">Ablehnen</button>`;

        if (isAdmin) {
            actionsCellHtml += `<button class="btn btn-secondary btn-small" onclick='openEditAbsenceModal(${absenceJsonString})' style="margin-left:5px;">Bearbeiten</button>`;
        }
        html += `<tr>
                   <td>${applicantName}</td>
                   <td>${typeLabels[absence.type] || absence.type}</td>
                   <td>${formatDate(absence.startDate)}</td>
                   <td>${formatDate(absence.endDate)}</td>
                   <td>${dayCount}</td>
                   <td><span style="color: ${statusColor}">${statusText}</span></td>
                   <td class="actions-cell">
                       ${actionsCellHtml}
                   </td>
                 </tr>`;
    });
    html += '</tbody></table>';
    return html;
}

function formatUsersTable(users) {
    if (!users || users.length === 0) {
        return '<p>Keine Benutzer gefunden.</p>';
    }
    let html = '<table class="data-table"><thead><tr><th>Name</th><th>E-Mail</th><th>Rollen</th><th>Status</th><th>Erstellt</th></tr></thead><tbody>';
    users.forEach(user => {
        const roles = user.roles ? user.roles.map(role => String(role).replace('ROLE_', '')).join(', ') : '-';
        const statusColor = user.active ? '#28a745' : '#dc3545';
        const statusText = user.active ? 'Aktiv' : 'Inaktiv';
        html += `<tr onclick="showUserDetails(${user.id})" style="cursor:pointer;">
                  <td>${user.firstName} ${user.lastName}</td>
                  <td>${user.email}</td>
                  <td>${roles}</td>
                  <td><span style="color: ${statusColor}">${user.status || statusText}</span></td>
                  <td>${formatDate(user.createdAt)}</td>
              </tr>`;
    });
    html += '</tbody></table>';
    return html;
}

function addEditTimeSlot(startTime = '', endTime = '') {
    editTimeSlotIdCounter++;
    const container = document.getElementById('editTimeSlotsContainer');
    const slotDiv = document.createElement('div');
    slotDiv.className = 'time-slot-container form-row';
    slotDiv.id = `edit-time-slot-${editTimeSlotIdCounter}`;
    slotDiv.innerHTML = `
        <div class="form-group">
            <label for="editStartTime-${editTimeSlotIdCounter}">Start ${editTimeSlotIdCounter}:</label>
            <input type="time" id="editStartTime-${editTimeSlotIdCounter}" name="editStartTimes" class="form-input" value="${startTime ? String(startTime).substring(0,5) : ''}" required>
        </div>
        <div class="form-group">
            <label for="editEndTime-${editTimeSlotIdCounter}">Ende ${editTimeSlotIdCounter}:</label>
            <input type="time" id="editEndTime-${editTimeSlotIdCounter}" name="editEndTimes" class="form-input" value="${endTime ? String(endTime).substring(0,5) : ''}">
        </div>
        ${editTimeSlotIdCounter > 1 || (container.children.length > 0) ?
        `<div class="remove-slot-btn-container" style="align-self: flex-end; margin-bottom: 1rem;"><button type="button" class="btn btn-danger btn-small" onclick="removeEditTimeSlot('edit-time-slot-${editTimeSlotIdCounter}')">Slot entfernen</button></div>` : ''}
    `;
    container.appendChild(slotDiv);
}

function removeEditTimeSlot(slotId) {
    const slotToRemove = document.getElementById(slotId);
    if (slotToRemove) {
        slotToRemove.remove();
    }
}

function addEditBreakSlot(startTime = '', endTime = '') {
    editBreakSlotIdCounter++;
    const container = document.getElementById('editBreakSlotsContainer');
    const slotDiv = document.createElement('div');
    slotDiv.className = 'break-slot-container form-row';
    slotDiv.id = `edit-break-slot-${editBreakSlotIdCounter}`;
    slotDiv.innerHTML = `
        <div class="form-group">
            <label for="editBreakStartTime-${editBreakSlotIdCounter}">Pause Start ${editBreakSlotIdCounter}:</label>
            <input type="time" id="editBreakStartTime-${editBreakSlotIdCounter}" name="editBreakStartTimes" class="form-input" value="${startTime ? String(startTime).substring(0,5) : ''}">
        </div>
        <div class="form-group">
            <label for="editBreakEndTime-${editBreakSlotIdCounter}">Pause Ende ${editBreakSlotIdCounter}:</label>
            <input type="time" id="editBreakEndTime-${editBreakSlotIdCounter}" name="editBreakEndTimes" class="form-input" value="${endTime ? String(endTime).substring(0,5) : ''}">
        </div>
        <div class="remove-slot-btn-container" style="align-self: flex-end; margin-bottom: 1rem;"><button type="button" class="btn btn-danger btn-small" onclick="removeEditBreakSlot('edit-break-slot-${editBreakSlotIdCounter}')">Pause entfernen</button></div>
    `;
    container.appendChild(slotDiv);
}

function removeEditBreakSlot(slotId) {
    const slotToRemove = document.getElementById(slotId);
    if (slotToRemove) {
        slotToRemove.remove();
    }
}

function parseTimeToMinutes(timeString) {
    if (!timeString || typeof timeString !== 'string') return 0;
    const parts = timeString.split(':');
    if (parts.length !== 2) return 0;
    const hours = parseInt(parts[0], 10);
    const minutes = parseInt(parts[1], 10);
    if (isNaN(hours) || isNaN(minutes)) return 0;
    return hours * 60 + minutes;
}

function formatMinutesToHours(totalMinutes) {
    if (isNaN(totalMinutes) || totalMinutes < 0) totalMinutes = 0;
    const hours = Math.floor(totalMinutes / 60);
    const minutes = Math.round(totalMinutes % 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

function calculateDaysBetween(startDate, endDate) {
    if (!startDate || !endDate) return 0;
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
}

function populateProjectDropdown(selectElement, projectList, selectedProjectId = null) {
    if (!selectElement) return;
    selectElement.innerHTML = '<option value="">Kein Projekt</option>';
    if (projectList && Array.isArray(projectList)) {
        projectList.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = project.name;
            if (selectedProjectId && project.id && project.id.toString() === selectedProjectId.toString()) {
                option.selected = true;
            }
            selectElement.appendChild(option);
        });
    }
}