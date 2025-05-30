// * @author EK

let activeTimeEntry = null;
let startTimeForTimer = null;
let timerInterval = null;
let currentEditingTimeEntry = null;
let editTimeSlotIdCounter = 0;
let editBreakSlotIdCounter = 0;

function startTimer() {
    const timerDisplay = document.getElementById('timerDisplay');
    if (timerDisplay) {
        timerInterval = setInterval(updateTimer, 1000);
        timerDisplay.classList.add('timer-running');
    }
}

function stopTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
    const timerDisplay = document.getElementById('timerDisplay');
    if (timerDisplay) {
        timerDisplay.classList.remove('timer-running');
        timerDisplay.textContent = '00:00:00';
    }
}

function updateTimer() {
    const timerDisplay = document.getElementById('timerDisplay');
    if (!startTimeForTimer || !timerDisplay) return;

    const now = new Date();
    const diff = now - startTimeForTimer;
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    timerDisplay.textContent =
        `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

async function startTimeTracking() {
    try {
        console.log('🚀 Starte Zeiterfassung...');
        hideAllMessages();
        const today = new Date().toISOString().split('T')[0];
        const now = new Date();
        const currentTime = now.toTimeString().slice(0, 5); // HH:MM

        const projectSelectElement = document.getElementById('currentProject');
        let selectedProjectIdForTimer = null; // Platzhalter

        const timeEntryData = {
            date: today,
            startTimes: [currentTime], // Backend erwartet Array
            endTimes: [],
            breaks: [],
            projectId: selectedProjectIdForTimer
        };

        console.log('📤 Sende Zeiterfassung-Daten:', timeEntryData);

        const response = await apiCall('/api/time-entries/start', {
            method: 'POST',
            body: JSON.stringify( selectedProjectIdForTimer ? { projectId: selectedProjectIdForTimer } : {})

        });


        if (response && response.entryId) {
            activeTimeEntry = {
                id: response.entryId,
                date: today,
                startTimes: [currentTime],
                endTimes: [],
                breaks: [],
                project: window.projects.find(p => p.id === selectedProjectIdForTimer) || null
            }

            startTimeForTimer = new Date(response.startTime); // startTime ist ein String wie "2025-05-28T14:30:00"
            startTimer();

            document.getElementById('startTimer').style.display = 'none';
            document.getElementById('stopTimer').style.display = 'inline-block';
            if (activeTimeEntry.project) {
                document.getElementById('currentProject').textContent = `Projekt: ${activeTimeEntry.project.name}`;
            } else {
                document.getElementById('currentProject').textContent = 'Kein Projekt ausgewählt';
            }

            showSuccess('✅ Zeiterfassung gestartet!');
            loadDashboardPageData(true); // Stats aktualisieren
        }
    } catch (error) {
        console.error('❌ Zeiterfassung Start Fehler:', error);
        if (error.message.startsWith('DUPLICATE_ENTRY') || error.message.includes('Zeiterfassung läuft bereits')) {
            showDuplicateEntryInfo('heute'); // Diese Funktion muss existieren und global sein oder hier definiert
        } else {
            showError('❌ Fehler beim Starten der Zeiterfassung: ' + (error.message || "Unbekannt"));
        }
    }
}

async function stopTimeTracking() {
    if (!activeTimeEntry || !activeTimeEntry.id) {
        showError('Keine aktive Zeiterfassung gefunden zum Stoppen.');
        return;
    }

    try {
        console.log('🛑 Stoppe Zeiterfassung für Eintrag ID:', activeTimeEntry.id);
        hideAllMessages();
        // Backend-Endpoint ist /api/time-entries/{entryId}/stop
        const response = await apiCall(`/api/time-entries/${activeTimeEntry.id}/stop`, {
            method: 'POST'

        });

        if (response) {
            stopTimer();
            const workedMinutes = parseTimeToMinutes(response.actualHours || '00:00');
            const hours = Math.floor(workedMinutes / 60);
            const minutes = workedMinutes % 60;
            const actualHoursFormatted = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;

            showSuccess(`✅ Zeiterfassung gestoppt! Arbeitszeit heute: ${actualHoursFormatted}`);

            activeTimeEntry = null;
            startTimeForTimer = null;
            document.getElementById('startTimer').style.display = 'inline-block';
            document.getElementById('stopTimer').style.display = 'none';
            document.getElementById('currentProject').textContent = 'Kein Projekt ausgewählt';

            loadDashboardPageData(true); // Stats aktualisieren
        }
    } catch (error) {
        console.error('❌ Fehler beim Stoppen der Zeiterfassung:', error);
        showError('❌ Fehler beim Stoppen der Zeiterfassung: ' + (error.message || "Unbekannt"));
    }
}


async function viewTimeEntries() {
    try {
        console.log('📋 Lade Zeiteinträge...');
        hideDataDisplay(); // Vorherige Ansicht schließen
        const response = await apiCall(`/api/time-entries?_t=${Date.now()}`);
        if (response && response.entries) {
            console.log(`✅ ${response.entries.length} Zeiteinträge geladen`);
            displayData('Zeiteinträge', formatTimeEntriesTable(response.entries));
        } else {
            displayData('Zeiteinträge', '<p>Keine Zeiteinträge gefunden.</p>');
        }
    } catch (error) {
        console.error('❌ Fehler beim Laden der Zeiteinträge:', error);
        showError('Fehler beim Laden der Zeiteinträge: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
    }
}

async function deleteTimeEntryHandler(entryId) {
    if (confirm('Sind Sie sicher, dass Sie diesen Zeiteintrag löschen möchten?')) {
        try {
            await apiCall(`/api/time-entries/${entryId}`, { method: 'DELETE' });
            showSuccess('Zeiteintrag erfolgreich gelöscht.');
            viewTimeEntries(); // Liste neu laden
            loadDashboardPageData(true); // Dashboard Statistiken aktualisieren
        } catch (error) {
            showError('Fehler beim Löschen des Zeiteintrags: ' + (error.message || "Unbekannt"));
        }
    }
}


// ----- Manueller Eintrag -----
function openManualEntryModal() {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    document.getElementById('manualDate').value = yesterdayStr;
    document.getElementById('manualStartTime').value = '08:00';
    document.getElementById('manualEndTime').value = '17:00';
    document.getElementById('manualBreakStartTime').value = '12:00';
    document.getElementById('manualBreakEndTime').value = '12:30';
    document.getElementById('manualProject').value = ''; // Projekt zurücksetzen

    loadProjectsForManualEntry(); // Lädt Projekte ins Dropdown
    calculateWorkTime(); // Berechnet initiale Zeit
    openModal('manualEntryModal'); // openModal ist in uiHelpers.js
}

async function loadProjectsForManualEntry() {
    try {

        const projectSelect = document.getElementById('manualProject');
        if (window.projects && projectSelect) {
            populateProjectDropdown(projectSelect, window.projects);
        } else if (projectSelect) { // Fallback, falls window.projects nicht da ist
            const response = await apiCall('/api/projects/active');
            if (response && response.projects) {
                window.projects = response.projects;
                populateProjectDropdown(projectSelect, window.projects);
            }
        }
    } catch (error) {
        console.error('Fehler beim Laden der Projekte für manuellen Eintrag:', error);
    }
}

function calculateWorkTime() {
    const startTimeStr = document.getElementById('manualStartTime').value;
    const endTimeStr = document.getElementById('manualEndTime').value;
    const breakStartTimeStr = document.getElementById('manualBreakStartTime').value;
    const breakEndTimeStr = document.getElementById('manualBreakEndTime').value;
    const calculatedHoursDisplay = document.getElementById('calculatedHours');

    if (startTimeStr && endTimeStr && calculatedHoursDisplay) {
        const startDate = new Date(`2000-01-01T${startTimeStr}`);
        const endDate = new Date(`2000-01-01T${endTimeStr}`);
        let workDurationMs = endDate - startDate;
        let breakDurationMs = 0;

        if (breakStartTimeStr && breakEndTimeStr) {
            const breakStartDate = new Date(`2000-01-01T${breakStartTimeStr}`);
            const breakEndDate = new Date(`2000-01-01T${breakEndTimeStr}`);
            if (breakEndDate > breakStartDate) {
                breakDurationMs = breakEndDate - breakStartDate;
            }
        }
        workDurationMs -= breakDurationMs;
        if (workDurationMs < 0) workDurationMs = 0;

        const totalWorkMinutes = Math.round(workDurationMs / (1000 * 60));
        calculatedHoursDisplay.textContent = formatMinutesToHours(totalWorkMinutes);
        return {
            workTimeInMinutes: totalWorkMinutes,
            breakTimeInMinutes: Math.round(breakDurationMs / (1000 * 60))
        };

    } else if (calculatedHoursDisplay) {
        calculatedHoursDisplay.textContent = '00:00';
        return { workTimeInMinutes: 0, breakTimeInMinutes: 0 };
    }
    return { workTimeInMinutes: 0, breakTimeInMinutes: 0 };
}

function validateBreakTime(workTimeInMinutes, breakTimeInMinutes) {
    const workTimeHours = workTimeInMinutes / 60;
    if (workTimeHours > 9 && breakTimeInMinutes < 60) return "Bei > 9 Std. Arbeit: mind. 60 Min. Pause.";
    if (workTimeHours > 7 && workTimeHours <= 9 && breakTimeInMinutes < 30) return "Bei > 7-9 Std. Arbeit: mind. 30 Min. Pause.";
    if (workTimeHours > 5.5 && workTimeHours <= 7 && breakTimeInMinutes < 15) return "Bei > 5.5-7 Std. Arbeit: mind. 15 Min. Pause.";
    return null;
}

async function handleManualEntrySubmit(event) {
    event.preventDefault();
    hideAllMessages();

    const date = document.getElementById('manualDate').value;
    const startTime = document.getElementById('manualStartTime').value;
    const endTime = document.getElementById('manualEndTime').value;
    const breakStartTime = document.getElementById('manualBreakStartTime').value;
    const breakEndTime = document.getElementById('manualBreakEndTime').value;
    const projectId = document.getElementById('manualProject').value || null;

    if (new Date(`2000-01-01T${endTime}`) <= new Date(`2000-01-01T${startTime}`)) {
        showError('⚠️ Endzeit muss nach der Startzeit liegen'); return;
    }
    if ((breakStartTime && !breakEndTime) || (!breakStartTime && breakEndTime)) {
        showError('⚠️ Bitte Start- und Endzeit für Pause angeben oder beide leer lassen.'); return;
    }
    if (breakStartTime && breakEndTime && new Date(`2000-01-01T${breakEndTime}`) <= new Date(`2000-01-01T${breakStartTime}`)) {
        showError('⚠️ Pausen-Endzeit muss nach Pausen-Startzeit liegen.'); return;
    }

    const { workTimeInMinutes, breakTimeInMinutes } = calculateWorkTime();
    const breakValidationError = validateBreakTime(workTimeInMinutes, breakTimeInMinutes);
    if (breakValidationError) {
        showError(`⚠️ Gesetzliche Pausenregelung nicht eingehalten: ${breakValidationError}`); return;
    }

    try {
        const breaksArray = (breakStartTime && breakEndTime) ? [{ start: breakStartTime, end: breakEndTime }] : [];
        const timeEntryData = {
            date: date,
            startTimes: [startTime],
            endTimes: [endTime],
            breaks: breaksArray,
            projectId: projectId
        };
        console.log('📝 Erstelle manuellen Zeiteintrag:', timeEntryData);
        const response = await apiCall('/api/time-entries', { method: 'POST', body: timeEntryData });

        if (response) {
            showSuccess(`✅ Zeiteintrag erfolgreich erstellt! Arbeitszeit: ${formatMinutesToHours(workTimeInMinutes)}`);
            closeModal('manualEntryModal');
            document.getElementById('manualEntryForm').reset();
            calculateWorkTime(); // Reset display
            loadDashboardPageData(true);
        }
    } catch (error) {
        console.error('❌ Fehler beim Erstellen des manuellen Zeiteintrags:', error);
        if (error.message.startsWith('DUPLICATE_ENTRY') || error.message.includes('bereits ein Zeiteintrag')) {
            const selectedDateFormatted = new Date(date).toLocaleDateString('de-DE');
            closeModal('manualEntryModal');
            document.getElementById('duplicateInfoModal').style.display = 'block';
        } else {
            showError('❌ Fehler beim Erstellen des Zeiteintrags: ' + (error.message || "Unbekannt").replace('DUPLICATE_ENTRY|', ''));
        }
    }
}

async function openEditTimeEntryModal(entry) {
    console.log("Öffne Bearbeitungsmodal für Eintrag:", entry);
    if (!entry || typeof entry !== 'object') {
        showError("Ungültige Eintragsdaten für die Bearbeitung."); return;
    }
    currentEditingTimeEntry = entry;

    document.getElementById('editTimeEntryId').value = entry.id;
    document.getElementById('editDate').value = entry.date;

    const timeSlotsContainer = document.getElementById('editTimeSlotsContainer');
    timeSlotsContainer.innerHTML = '';
    editTimeSlotIdCounter = 0;
    if (entry.startTimes && entry.startTimes.length > 0) {
        entry.startTimes.forEach((st, index) => {
            const et = (entry.endTimes && entry.endTimes[index]) ? entry.endTimes[index] : '';
            addEditTimeSlot(st, et); // addEditTimeSlot ist in uiHelpers.js
        });
    } else { addEditTimeSlot(); }

    const breakSlotsContainer = document.getElementById('editBreakSlotsContainer');
    breakSlotsContainer.innerHTML = '';
    editBreakSlotIdCounter = 0;
    if (entry.breaks && entry.breaks.length > 0) {
        entry.breaks.forEach(b => addEditBreakSlot(b.start, b.end));
    }

    const editProjectSelect = document.getElementById('editProject');
    if (window.projects && editProjectSelect) {
        populateProjectDropdown(editProjectSelect, window.projects, entry.project ? entry.project.id : null);
    } else if (editProjectSelect) { // Fallback
        try {
            const projectsResponse = await apiCall('/api/projects/active');
            if (projectsResponse && projectsResponse.projects) {
                window.projects = projectsResponse.projects;
                populateProjectDropdown(editProjectSelect, window.projects, entry.project ? entry.project.id : null);
            }
        } catch (err) { showError("Projekte für Bearbeitung nicht geladen.");}
    }

    document.getElementById('editCalculatedHours').textContent = entry.actualHours || '--:--';
    openModal('editTimeEntryModal');
}

async function handleEditTimeEntrySubmit(event) {
    event.preventDefault();
    hideAllMessages();
    const entryId = document.getElementById('editTimeEntryId').value;
    const date = document.getElementById('editDate').value;
    const projectId = document.getElementById('editProject').value || null;

    const startTimes = Array.from(document.querySelectorAll('#editTimeSlotsContainer input[name="editStartTimes"]'))
        .map(input => input.value).filter(Boolean);
    const endTimes = Array.from(document.querySelectorAll('#editTimeSlotsContainer input[name="editEndTimes"]'))
        .map(input => input.value);

    const breaks = [];
    document.querySelectorAll('#editBreakSlotsContainer .break-slot-container').forEach(slot => {
        const startInput = slot.querySelector('input[name="editBreakStartTimes"]');
        const endInput = slot.querySelector('input[name="editBreakEndTimes"]');
        if (startInput && startInput.value && endInput && endInput.value) {
            breaks.push({ start: startInput.value, end: endInput.value });
        } else if (startInput && startInput.value && (!endInput || !endInput.value)) {
            showError("Für Pause fehlt die Endzeit."); throw new Error("Pause unvollständig");
        } else if ((!startInput || !startInput.value) && endInput && endInput.value) {
            showError("Für Pause fehlt die Startzeit."); throw new Error("Pause unvollständig");
        }
    });

    let validTimes = true;
    const finalEndTimes = [];
    if (startTimes.length > 0) {
        for (let i = 0; i < startTimes.length; i++) {
            const st = startTimes[i];
            const et = endTimes[i] || '';
            if (et && st >= et) {
                showError(`Die Endzeit für Arbeitszeit-Slot ${i + 1} muss nach der Startzeit liegen.`);
                validTimes = false; break;
            }
            finalEndTimes.push(et);
        }
    }
    if (!validTimes) return;

    for(const p of breaks) {
        if (p.start >= p.end) {
            showError(`Die Pausenendzeit muss nach der Pausenstartzeit liegen.`); return;
        }
    }

    const payload = {
        date: date,
        startTimes: startTimes,
        endTimes: finalEndTimes,
        breaks: breaks,
        projectId: projectId ? parseInt(projectId) : null
    };

    console.log("Sende Update für Zeiteintrag:", entryId, "Payload:", payload);
    try {
        await apiCall(`/api/time-entries/${entryId}`, { method: 'PUT', body: payload });
        showSuccess('Zeiteintrag erfolgreich aktualisiert!');
        closeModal('editTimeEntryModal');
        viewTimeEntries();
        loadDashboardPageData(true);
    } catch (error) {
        showError('Fehler beim Aktualisieren des Zeiteintrags: ' + (error.message || "Unbekannt"));
        console.error("Update Fehler Details:", error);
    }
}

// Für Duplikat-Info Modal
function showDuplicateEntryInfo(dateText = 'das gewählte Datum') {
    hideAllMessages();
    const duplicateInfoModal = document.getElementById('duplicateInfoModal');
    if (duplicateInfoModal) {

        openModal('duplicateInfoModal');
    } else {

        showWarning(`📅 Für ${dateText} existiert bereits ein Zeiteintrag!
        Das System erlaubt nur einen Zeiteintrag pro Tag. Optionen:
        • Vorhandenen Eintrag bearbeiten (Einträge anzeigen)
        • Anderes Datum für manuellen Eintrag wählen
        • Live-Zeiterfassung für heute verwenden (falls möglich)`);
        setTimeout(() => { viewTimeEntries(); }, 5000);
    }
}