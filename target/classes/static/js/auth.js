// * @author EK
async function logout() {
    try {

        const tokenToInvalidate = localStorage.getItem('jwtToken');
        if (tokenToInvalidate) {

            await apiCall('/api/auth/logout', {
                method: 'POST',
                body: tokenToInvalidate,
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        }
    } catch (error) {
        console.warn('Logout-API-Fehler (ignoriert, da Client-Logout wichtiger):', error);
    } finally {
        localStorage.clear();
        window.location.href = '/';
    }
}

function openChangePasswordModal() {
    const form = document.getElementById('changePasswordForm');
    if (form) form.reset();
    hideAllMessages();
    const modal = document.getElementById('changePasswordModal');
    if (modal) modal.style.display = 'block';
}

async function handleChangePasswordSubmit(event) {
    event.preventDefault();
    hideAllMessages();

    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmNewPassword = document.getElementById('confirmNewPassword').value;

    if (newPassword.length < 6) {
        showError('Das neue Passwort muss mindestens 6 Zeichen lang sein.');
        return;
    }
    if (newPassword !== confirmNewPassword) {
        showError('Die neuen Passwörter stimmen nicht überein.');
        return;
    }

    const changePasswordData = {
        oldPassword: oldPassword,
        newPassword: newPassword
    };

    try {
        console.log('🔑 Ändere Passwort mit Daten:', changePasswordData);
        const response = await apiCall('/api/users/change-password', {
            method: 'PUT',
            body: JSON.stringify(changePasswordData)
        });

        if (response && response.message && response.message.includes("Passwort geändert")) {
            showSuccess('Passwort erfolgreich geändert! Sie werden in Kürze ausgeloggt und können sich neu anmelden.');
            closeModal('changePasswordModal');
            setTimeout(() => {
                logout();
            }, 3000);
        } else {

            showSuccess('Passwortänderung erfolgreich verarbeitet. Bitte neu anmelden.');
            closeModal('changePasswordModal');
            setTimeout(() => {
                logout();
            }, 3000);
        }
    } catch (error) {
        console.error('❌ Fehler beim Ändern des Passworts:', error);
        showError('Fehler beim Ändern des Passworts: ' + (error.message || 'Unbekannter Fehler.'));
    } finally {
        const form = document.getElementById('changePasswordForm');
        if (form) form.reset();
    }
}