// * @author EK

const DEBUG_MODE = true;

async function apiCall(url, options = {}) {
    try {
        const token = localStorage.getItem('jwtToken');

        if (DEBUG_MODE) {
            console.log('🔍 API Call:', {
                url: url,
                method: options.method || 'GET',
                hasToken: !!token,
                tokenStart: token ? token.substring(0, 20) + '...' : 'NONE'
            });
        }

        if (!token && !url.includes('/api/auth/login')) {
            console.warn('API Call ohne Token für URL:', url);
        }

        const response = await fetch(url, {
            ...options,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                ...options.headers
            },
            body: options.body ? (typeof options.body === 'string' ? options.body : JSON.stringify(options.body)) : undefined
        });

        if (DEBUG_MODE) {
            console.log('📥 API Response:', {
                status: response.status,
                statusText: response.statusText,
                url: url
            });
        }

        if (response.status === 401) {
            console.log('🔒 Token ungültig - Automatische Weiterleitung zur Anmeldung');
            if (typeof showError === 'function') {
                showError('Sitzung abgelaufen. Sie werden zur Anmeldung weitergeleitet...');
            }
            setTimeout(() => {
                localStorage.clear();
                window.location.href = '/';
            }, 2000);
            return Promise.reject(new Error('Sitzung abgelaufen'));
        }

        if (response.status === 403) {
            console.log('🚫 Zugriff verweigert (403) - Details:', {
                url,
                method: options.method || 'GET',
                userEmail: localStorage.getItem('userEmail'),
                userRoles: localStorage.getItem('userRoles')
            });
            throw new Error(`Zugriff verweigert (403). Möglicherweise fehlen Berechtigungen für: ${options.method || 'GET'} ${url}`);
        }

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status} ${response.statusText}`;
            try {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    if (errorData.message) {
                        errorMessage = errorData.message;
                    } else if (errorData.error) {
                        errorMessage = errorData.error;
                    } else if (typeof errorData === 'string') {
                        errorMessage = errorData;
                    } else if (errorData.errors && Array.isArray(errorData.errors)) {
                        errorMessage = errorData.errors.join(', ');
                    }
                } else {
                    const errorText = await response.text();
                    if (errorText && errorText.trim()) {
                        errorMessage = errorText;
                    }
                }
            } catch (parseError) {
                console.log('⚠️ Konnte Fehlermessage nicht parsen:', parseError);
            }

            if (DEBUG_MODE) {
                console.log('❌ API Error Details:', {
                    status: response.status,
                    originalMessage: errorMessage,
                    url: url
                });
            }
            if (errorMessage.includes('bereits ein Zeiteintrag') || errorMessage.includes('existiert bereits')) {
                throw new Error('DUPLICATE_ENTRY|' + errorMessage);
            }
            throw new Error(errorMessage);
        }

        const contentType = response.headers.get('content-type');
        if (response.status === 204) { // No Content
            return {}; // Leeres Objekt zurückgeben
        }
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            if (DEBUG_MODE) {
                console.log('✅ API Success Data:', data);
            }
            return data;
        } else {
            const text = await response.text();
            if (DEBUG_MODE) {
                console.log('✅ API Success Text:', text);
            }

            try {
                if (text.trim().startsWith('{') && text.trim().endsWith('}')) {
                    return JSON.parse(text);
                }
            } catch (e) {
                return text;
            }
        }
    } catch (error) {
        console.error('🔥 API Call Error:', error);

        throw error;
    }
}